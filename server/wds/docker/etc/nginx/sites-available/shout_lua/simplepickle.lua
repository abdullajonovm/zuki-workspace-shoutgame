-- Copyright (c) 2014 SHOUT TV

local tsize_maxlen = 5
local klens_maxlen = 4
local vlens_maxlen = 4

local _M = {}

function _M.pickle(target, ...)
    local values_to_note = {}
    for i = 1,select('#',...) do
        values_to_note[select(i,...)] = 1
    end
    local noted_value_indices = {}
    local byte_index = 1
    local result = {string.rep('0',tsize_maxlen)}
    byte_index = byte_index + #result[#result]
    local tsize = 0
    for k, v in pairs(target) do
        local ks = tostring(k)
        local klens = string.format('%0'..tostring(klens_maxlen)..'d', #ks)
        if #klens > klens_maxlen then
            return nil, 'key size is too long'
        end
        table.insert(result, klens)
        byte_index = byte_index + #result[#result]
        table.insert(result, ks)
        byte_index = byte_index + #result[#result]
        local vs = tostring(v)
        local vlens = string.format('%0'..tostring(vlens_maxlen)..'d', #vs)
        if #vlens > vlens_maxlen then
            return nil, 'value size is too long'
        end
        table.insert(result, vlens)
        byte_index = byte_index + #result[#result]
        if values_to_note[k] ~= nil then
            table.insert(noted_value_indices, byte_index)
        end
        table.insert(result, vs)
        byte_index = byte_index + #result[#result]
        tsize = tsize + 1
    end
    local tsizes = string.format('%0'..tsize_maxlen..'d', tsize)
    if #tsizes > tsize_maxlen then
        return nil, 'table size is too long'
    end
    result[1] = tsizes
    return table.concat(result), noted_value_indices
end

function _M.unpickle(source)
    if #source < 4 then
        return nil, "Invalid source string"
    end
    local i = 1
    local tsizes = string.sub(source, i, i+tsize_maxlen-1)
    local tsize = tonumber(tsizes)
    i = i + tsize_maxlen
    local result = {}
    local idx = 1
    while idx <= tsize do
        local klens = string.sub(source, i, i+klens_maxlen-1)
        local klen = tonumber(klens)
        i = i + klens_maxlen
        local ks = string.sub(source, i, i+klen-1)
        i = i + klen
        local vlens = string.sub(source, i, i+vlens_maxlen-1)
        local vlen = tonumber(vlens)
        i = i + vlens_maxlen
        local vs = string.sub(source, i, i+vlen-1)
        i = i + vlen
        idx = idx + 1
        result[ks] = vs
    end
    return result
end

return _M
