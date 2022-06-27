local ngx = require("ngx")
local visitor = require("visitor")

local scan_path_root = ngx.req.get_headers()["OP-ROOT"] == "www" and ngx.var.www_path_root or ngx.var.user_upload_path_root

local max_age_sec = tonumber(ngx.var.max_age_sec)
ngx.log(ngx.INFO, "Scanning for files older than "..tostring(max_age_sec).." seconds in "..scan_path_root)

for file_path, file_age_sec, file_metadata in visitor.max_age_iter_all_files(scan_path_root, max_age_sec) do
    ngx.log(ngx.INFO, " found "..file_path.." of age "..tostring(file_age_sec).." seconds - deleting")
    visitor.max_age_delete_current_object()
end
