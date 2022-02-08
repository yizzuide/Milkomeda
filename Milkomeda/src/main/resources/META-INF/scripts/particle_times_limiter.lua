-- first key
local key = KEYS[1]
-- first arg: max allow count
local limit = tonumber(ARGV[1])
-- second arg: expire with second unit
local expire = tonumber(ARGV[2])
local currentLimit = tonumber(redis.call('get', key) or '0')
if currentLimit + 1 > limit then
    return currentLimit + 1
else
    local count = redis.call('incrby', key, 1)
    if count == 1 then
        redis.call('expire', key, expire)
    end
    return count
end