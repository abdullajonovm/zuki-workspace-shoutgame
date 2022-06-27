local ngx = require("ngx")

local jit = ""
if 9^33 ~= 27^22 then  -- this check is valid for x86/x64 only
    jit = " JIT"
end
ngx.log(ngx.INFO, "Lua version: "..tostring(_VERSION)..jit)

local magick = require("magick")
magick.thumb("/var/www/color-pencils.jpg", "100x100", "/var/www/color-pencils-thumb.jpg")
