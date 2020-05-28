local key = KEYS[1]
-- phase array
local len = ARGV[1]
if redis.call('exists', key) == 0 then
    redis.call('set', key, 0)
    return 0
end
local current =  tonumber(redis.call('get', key) or "0")
local index = (tonumber(current) + 1) % tonumber(len)
redis.call('set', key, index)
return index