local errr = require("error")
local ngx = require("ngx")
local lfs = require("lfs")
local path_hash = require("path_hash")
local visitor = require("visitor")

local function is_failure(conn)
    return conn.err ~= nil
end

local function new_conn(file_path, metadata, target_hostname, retry)
    local conn = {}
    conn.is_failure = is_failure
    conn.file_path = file_path
    conn.metadata = metadata
    conn.target_hostname = target_hostname
    conn.retry = retry
    return conn
end

local function rand_chars(len)
    local rx = {}
    for i = 1,len do
        local r = math.random(48, 48+62)
        if r >= 58 and r <= 64 then
            r = r + 9
        elseif r >= 91 and r <= 96 then
            r = r + 6
        end
        table.insert(rx, r)
    end
    return string.char(unpack(rx))
end

local function http_body_header(metadata)
    local rand_string = rand_chars(39)
    local body_header = {
        "--"..rand_string.."\r\n",
        'Content-Disposition: form-data; name="X"\r\n',
        "Content-Type: application/octet-stream\r\n",
        "Content-Transfer-Encoding: binary\r\n"
    }
    for k, v in pairs(metadata) do
        table.insert(body_header, k..": "..v.."\r\n")
    end
    table.insert(body_header, "\r\n")
    local body_header_len = 0
    for i, v in ipairs(body_header) do
        body_header_len = body_header_len + #v
    end
    return body_header, body_header_len, rand_string
end

local function http_body_footer(rand_string)
    local body_footer = {
        "\r\n--"..rand_string.."--\r\n"
    }
    local body_footer_len = 0
    for i, v in ipairs(body_footer) do
        body_footer_len = body_footer_len + #v
    end
    return body_footer, body_footer_len
end

local function http_request_header(content_len, rand_string, target_hostname, to_port, to_path)
    local request_header = {
        "POST http://"..target_hostname..":"..tostring(to_port).."/UPLOAD"..to_path.." HTTP/1.1\r\n",
        "Content-Length: "..tostring(content_len).."\r\n",
        "Content-Type: multipart/form-data; boundary="..rand_string.."; charset=UTF-8\r\n",
        "Host: "..target_hostname..":"..tostring(to_port).."\r\n",
        "Connection: Keep-Alive\r\n",
        "User-Agent: WebDataStoreOrigin\r\n",
        "\r\n",
    }
    return request_header
end

local function clone_to_remote(file_path, metadata, target_hostname, conn)
    -- send request
    local sock = ngx.socket.tcp()
    conn.sock = sock
    local success, err = sock:connect(target_hostname, 81)
    if not success then
        conn.err = err
        sock:close()
        return conn
    end

    local file_size = lfs.attributes(file_path, "size")

    local body_header, body_header_len, rand_string = http_body_header(metadata)
    local body_footer, body_footer_len = http_body_footer(rand_string)
    local content_len = body_header_len + file_size + body_footer_len
    local to_path, err = path_hash.file_to_uri_path(file_path)
    if to_path == nil then
        conn.err = err
        sock:close()
        return conn
    end
    local request_header = http_request_header(content_len, rand_string, target_hostname, 81, to_path)

    for i, line in ipairs(request_header) do
        local bytes, err = sock:send(line)
        if not bytes then
            conn.err = err
            sock:close()
            return conn
        end
    end
    for i, line in ipairs(body_header) do
        local bytes, err = sock:send(line)
        if not bytes then
            conn.err = err
            sock:close()
            return conn
        end
    end
    local file, err = io.open(file_path, "r");
    if not file then
        conn.err = err
        sock:close()
        return conn
    end
    while true do
        local data = file:read(4096)
        if data == nil then
            file:close()
            break
        end
        local bytes, err = sock:send(data)
        if not bytes then
            conn.err = err
            file:close()
            sock:close()
            return conn
        end
    end
    for i, line in ipairs(body_footer) do
        local bytes, err = sock:send(line)
        if not bytes then
            conn.err = err
            sock:close()
            return conn
        end
    end

    -- read response
    local line = ""
    local status, status_txt
    local content_len, chunked
    while line do
        local line, err, partial = sock:receive()
        if not line then
            conn.err = err
            sock:close()
            return conn
        end
        if not status then
            status, status_txt = line:match("^HTTP/[0-9.]+ (%d+) (%w+)")
        end
        if not content_len and not chunked then
            content_len = line:match("^Content%-Length: (%d+)")
            if not content_len then
                chunked = line:match("^Transfer%-Encoding: chunked")
            end
        end
        if line == "" then
            if content_len then
                local body = sock:receive(tonumber(content_len))
                break
            elseif chunked then
                while true do
                    local chunk_len = sock:receive()
                    if tonumber(chunk_len) == 0 then
                        sock:receive() -- read final \r\n
                        break
                    else
                        local chunk = sock:receive(tonumber(chunk_len))
                    end
                end
                break
            else
                conn.err = "Did not detect content length nor chunked response from response to request: "..request_header[1]
                sock:close()
                return conn
            end
        end
    end

    -- return connection to pool
    sock:setkeepalive()

    return conn
end

local headers = ngx.req.get_headers()
local target_hostname = headers["X-Clone-To-Host"]
if target_hostname == nil then
    return errr.response("Missing host parameter", 400)
end
target_hostname = "172.17.0.10"

local cos = {}
local conns = {}
for file_path, metadata in visitor.iter_all_objects() do
    if metadata["PROP-Global"] == "true" then
        local conn = new_conn(file_path, metadata, target_hostname, 0)
        table.insert(conns, conn)
        local co = ngx.thread.spawn(clone_to_remote, file_path, metadata, target_hostname, conn)
        table.insert(cos, co)
        while #cos >= 1 do
            local success, result = ngx.thread.wait(unpack(cos))
            if not success then
                ngx.log(ngx.ERR, "Unknown error: "..tostring(result))
            end
            local i = 0
            while i < #cos do
                i = i + 1
                local co = cos[i]
                local co_status = coroutine.status(co)
                if co_status == "dead" then
                    table.remove(cos, i)
                    local conn = conns[i]
                    table.remove(conns, i)
                    i = i - 1
                    if conn:is_failure() then
                        ngx.log(ngx.ERR, "Upload of "..conn.file_path.." failed (retry #"..conn.retry..") to "..conn.target_hostname.." - retrying: "..conn.err)
                        if conn.retry == 2 then
                            ngx.log(ngx.ERR, "Reached maximum retries ("..conn.retry..") for upload of "..conn.file_path.." to "..conn.target_hostname.." - giving up")
                            return errr.response("Clone failed: "..conn.err, 503)
                        end
                        local retry_conn = new_conn(conn.file_path, conn.metadata, conn.target_hostname, conn.retry+1)
                        table.insert(conns, retry_conn)
                        co = ngx.thread.spawn(clone_to_remote, conn.file_path, conn.metadata, conn.target_hostname, retry_conn)
                        table.insert(cos, co)
                    else
                        ngx.log(ngx.INFO, "Upload of "..conn.file_path.." succeeded to "..conn.target_hostname)
                    end
                end
            end
        end
    end
end
