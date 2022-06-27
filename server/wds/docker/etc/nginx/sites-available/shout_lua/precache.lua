ngx.req.discard_body()
ngx.location.capture("/PURGE/"..ngx.var.op_uri_path)
local res = ngx.location.capture("/GET/"..ngx.var.op_uri_path)
ngx.status = res.status
ngx.header["WDS-MD5"] = res.header["WDS-MD5"]
