require "md5"

local m = md5.init()
local success
success = m:update("asdf")
success = m:update("asdf")
hex = m:final()

local expected = "6a204bd89f3c8348afd5c77c717a097a"
if hex ~= expected then
    print("FAIL: expected "..expected.." got "..hex)
    return 1
end

m:reset()
success = m:update("qwerqwerqwer")
hex = m:final()

local expected = "795c08a0cf9d0bdc6d806a976ac09716"
if hex ~= expected then
    print("FAIL: expected "..expected.." got "..hex)
    return 1
end

print "ALL TESTS PASS"
return 0
