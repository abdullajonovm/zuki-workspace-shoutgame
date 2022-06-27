local errr = require("error")
local path_hash = require("path_hash")
local lfs = require("lfs")
local pickle = require("simplepickle")
local cors = require("cors")

local is_metadata_only = ngx.var.is_getmeta ~= '0'

local path, relative_path_idx, err = path_hash.uri_to_file(ngx.var.www_path_root, ngx.var.op_uri_path)
local filename = path and path[#path] or nil
if not path or filename:match("^__meta.db$") or filename:match("%.__new__$") then
    ngx.status = 404
    ngx.var.get_err = err
    return is_metadata_only and ngx.exit(ngx.HTTP_OK) or ngx.exec("@GET_ERR")
end
local path_string = "/"..table.concat(path, "/")
local path_attr, err = lfs.attributes(path_string, "mode")
if not path_attr or path_attr ~= 'file' then
    ngx.status = 404
    ngx.var.get_err = "File not found"
    return is_metadata_only and ngx.exit(ngx.HTTP_OK) or ngx.exec("@GET_ERR")
end

local uri_path = "/"..table.concat(path, "/", relative_path_idx)
ngx.var.op_uri_path = uri_path

local success

path[#path] = "__meta.db"
local db_path = "/"..table.concat(path, "/")
local db, err = io.open(db_path, "r")
if not db then
    ngx.status = 500
    ngx.var.get_err = "GET failed to open hash db "..db_path..": "..tostring(err)
    return is_metadata_only and ngx.exit(ngx.HTTP_OK) or ngx.exec("@GET_ERR")
end

local db_string = db:read("*a")
local db_dict = pickle.unpickle(db_string)
local file_metadata_string = db_dict[filename]
local file_metadata
if file_metadata_string == nil then
    ngx.log(ngx.ALERT, "Missing metadata for "..path_string)
    file_metadata = {}
else
    file_metadata = pickle.unpickle(file_metadata_string)
end

for k,v in pairs(file_metadata) do
    if is_metadata_only then
        if k == 'META-MD5' then
            -- Due to a chicken and egg problem of nobody following the HTTP standard with
            -- respect to properly using Transfer-Encoding to signal on-the-fly compression,
            -- NGINX simply strips the standard ETag header when compressing on-the-fly. Since
            -- we control the WDS server we return the ETag as WDS-MD5 and rely on a
            -- Last-Modified header to guide proxies in between the server and client
            ngx.header["WDS-MD5"] = v
        else
            ngx.header[k] = v
        end
    else
        local rhdr_key = string.match(k, "^RHDR%-(.*)$")
        if k == 'META-MD5' then
            ngx.header["WDS-MD5"] = v
        elseif rhdr_key then
            ngx.header[rhdr_key] = v
        end
    end
end

db:close()

if is_metadata_only then
    ngx.status = ngx.HTTP_OK
    ngx.header["Content-Type"] = nil
    ngx.header["Content-Length"] = 0
    return ngx.exit(ngx.HTTP_OK)
end

cors.handle_cors_response_headers()

-- Reading metadata and reading the object contents as separate operations like
-- this runs the risk of returning metadata to a client that does not match the
-- data returned. But allowing NGINX to retrieve the file should be quite a bit
-- faster, and at time of writing the metadata that is most sensitive to change
-- relative to its data is the Last-Modified and Last-Touched headers. Because we
-- read metadata first and data second, a mismatch involving these two headers
-- would appear to the client as if the object had not yet changed which would be
-- fairly benign.
-- If this becomes a problem, read-lock the directory and use a log-phase lua
-- hook to release the lock.
return ngx.exec("@GET_OK")
