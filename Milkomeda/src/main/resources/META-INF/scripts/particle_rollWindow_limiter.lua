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
    -- level: LOG_DEBUG, LOG_VERBOSE, LOG_NOTICE and LOG_WARNING
    redis.log(redis.LOG_DEBUG, "The current number of window is"..current..", which exceeds the limit of "..limitCount.."!")
    return tonumber(current)
end

-- clear member before window start time
redis.call('zremrangebyscore', key, 0, windowStartMill)

-- add and return current count
redis.call('zadd', key, tostring(currentMill), currentMill)
redis.call('expire', key, expire)
return tonumber(current)