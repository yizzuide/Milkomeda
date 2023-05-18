local key = KEYS[1]
-- expire with second unit
local expire = tonumber(ARGV[1])
local currentMill = tonumber(ARGV[2])
-- max limit count
local limitCount = tonumber(ARGV[3])
-- window start time
local windowStartMill = currentMill - expire * 1000
-- get count between start and current time
local current = redis.call('zcount', key, windowStartMill, currentMill)
-- if current count over max limit count, return current count
if current and tonumber(current) >= limitCount then
    return tonumber(current)
end

-- clear member before window start time
redis.call('ZREMRANGEBYSCORE', key, 0, windowStartMill)

-- add and return current count
redis.call('zadd', key, tostring(currentMill), currentMill)
redis.call('expire', key, expire)
return tonumber(current)