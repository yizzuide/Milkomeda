local key = KEYS[1]
local tokenCount = tonumber(redis.call('get', key) or '0')
if tokenCount > 0 then
    tokenCount = tonumber(redis.call('decr', key))
    return tokenCount
else
    return -1;
end