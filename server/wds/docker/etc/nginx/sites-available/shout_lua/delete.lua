local errr = require("error")
local lfs = require("lfs")
local path_hash = require("path_hash")

local success
local err

local delete_path_root = ngx.req.get_headers()["OP-ROOT"] == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root

local path, relative_path_idx, err = path_hash.uri_to_file(delete_path_root, ngx.var.op_uri_path)
if not path then
    return errr.response(err)
end

local file_path = "/"..table.concat(path, "/")
success, err = os.remove(file_path)
if not success then
    return errr.response("Failure removing "..file_path..": "..err)
end

-- ngx.header["Content-Type"] = "text/html"
-- ngx.say("<b>Success</b>")

