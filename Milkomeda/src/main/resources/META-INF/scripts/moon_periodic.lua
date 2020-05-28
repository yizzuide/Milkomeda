local key = KEYS[1]
local len = ARGV[1]
if redis.call('exists', key) == 0 then
    redis.call('set', key, 0)
    return 0
end
local current = tonumber(redis.call('get', key))
current = (tonumber(current) + 1) % tonumber(len)
redis.call('set', key, current)
return current