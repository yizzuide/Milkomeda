local key = KEYS[1]
local bucketCapacity = tonumber(ARGV[1])
local waterRate = tonumber(ARGV[2])
local currentMills = tonumber(ARGV[3])

local lastTimeKey = key..'_update_time'
local waterCount = redis.call('get', key)
if waterCount then
    local lastTime = tonumber(redis.call('get', lastTimeKey))
    -- calc leave water in the past time
    waterCount = math.max(0, waterCount - ((currentMills - lastTime) / 1000) * waterRate)
    if (waterCount + 1) >= bucketCapacity then
        return -1
    end
    waterCount = waterCount + 1
    redis.call('set', key, waterCount)
    redis.call('set', lastTimeKey, currentMills)
    return waterCount
else
    -- first time, put 1 request in bucket
    waterCount = 1
    redis.call('set', key, waterCount)
    -- record last time
    redis.call('set', lastTimeKey, currentMills)
    return waterCount;
end


