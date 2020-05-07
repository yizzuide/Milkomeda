-- first key
local key = KEYS[1]
-- first arg: max allow count
local limit = tonumber(ARGV[1])
-- second arg: expire with second unit
local expire = tonumber(ARGV[2])
local curentLimit = tonumber(redis.call('get', key) or "0")
if curentLimit + 1 > limit then
    return curentLimit + 1
else
    local count = redis.call("INCRBY", key, 1)
    redis.call("EXPIRE", key, expire)
    return count
end