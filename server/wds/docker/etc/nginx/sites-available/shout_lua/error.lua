local _M = {}

function _M.response(errmsg, status)
    ngx.log(ngx.ERR, "Sending error to client: "..errmsg)
    ngx.status = status and status or ngx.HTTP_BAD_REQUEST
    ngx.header["Content-Type"] = "text/html"
    ngx.say("ERROR: "..errmsg)
    -- Allow other NGINX directives to run
    return ngx.exit(ngx.HTTP_OK)
end

return _M
