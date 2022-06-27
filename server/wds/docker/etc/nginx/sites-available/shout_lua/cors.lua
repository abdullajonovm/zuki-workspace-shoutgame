local _M = {}

function _M.handle_cors_response_headers()
    local origin = ngx.req.get_headers()['Origin']
    if origin ~= nil and string.match(origin, '^https?://(.*)$') ~= nil then
        local method = ngx.req.get_method() 
        if method == 'GET' or method == 'POST' then
            ngx.header['Access-Control-Allow-Origin'] = origin
            ngx.header['Access-Control-Allow-Credentials'] = 'true'
        elseif method == 'OPTIONS' then
            ngx.header['Access-Control-Allow-Origin'] = origin
            ngx.header['Access-Control-Allow-Credentials'] = 'true'
            ngx.header['Access-Control-Max-Age'] = '1728000'
            ngx.header['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'
            ngx.header['Access-Control-Allow-Headers'] = 'Authorization,Content-Type,Accept,Origin,User-Agent,DNT,Cache-Control,X-Mx-ReqToken,Keep-Alive,X-Requested-With,If-Modified-Since'
            ngx.header['Content-Length'] = '0'
            ngx.header['Content-Type'] = 'text/plain charset=UTF-8'
        end
    end
end

return _M
