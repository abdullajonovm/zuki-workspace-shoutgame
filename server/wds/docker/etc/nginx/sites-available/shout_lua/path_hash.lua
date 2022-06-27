local ngx = require("ngx")

local hash_spec_string = ngx.var.upload_path_hash_spec

local hash_spec = {}
for spec_part in string.gmatch(hash_spec_string, "[^:]+") do
    table.insert(hash_spec, tonumber(spec_part))
end
local hash_spec_sum = 0
for i, len in ipairs(hash_spec) do
    hash_spec_sum = hash_spec_sum + len
end

local _M = {}

function _M.uri_to_file(path_root, uri_path)
    local flat_file_path = string.gsub(string.gsub(uri_path, "/", ""), "[^a-zA-Z0-9_]", "_")

    if #flat_file_path == 0 then
        ngx.log(ngx.WARN, "File name must have at least one character: "..uri_path)
        return nil, nil, "File not found"
    end

    local i = 1
    while hash_spec_sum > #flat_file_path do
        flat_file_path = string.sub(flat_file_path, i, i)..flat_file_path
        i = i + 2
        if i > #flat_file_path then
            i = 1
        end
    end

    local path = {}

    for path_part in string.gmatch(path_root, "[^/]+") do
        table.insert(path, path_part)
    end
    local relative_index = #path + 1

    local idx = 1
    for _,len in ipairs(hash_spec) do
        table.insert(path, string.sub(flat_file_path, idx, idx+len-1))
        idx = idx + len
    end

    for path_part in string.gmatch(uri_path, "[^/]+") do
        table.insert(path, path_part)
    end

    return path, relative_index, nil
end

function _M.file_to_uri_path(file_path, path_root)
    if file_path:sub(1, #path_root) ~= path_root then
        return nil, "unknown path root in "..file_path
    end
    -- e.g. /var/www/a/bc/abc.json
    --           /var/www            a bc            / /
    local skip = #path_root + hash_spec_sum + #hash_spec
    return file_path:sub(skip+1)
end

return _M
