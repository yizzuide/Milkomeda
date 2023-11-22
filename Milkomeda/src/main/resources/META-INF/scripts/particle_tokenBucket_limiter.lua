local key = KEYS[1]
local bucketCapacity = tonumber(ARGV[1])
-- number of tokens added each time
local addToken = tonumber(ARGV[2])
-- token addition interval with second unit
local addInterval = tonumber(ARGV[3])
local currentMills = tonumber(ARGV[4])

-- key of bucket last update time
local lastTimeKey = key..'_update_time'

local tokenCount = redis.call('get', key)
-- maximum time required for bucket reset
local resetTime = math.ceil(bucketCapacity / addToken) * addInterval;
-- has tokens
if tokenCount then
    local lastTime = tonumber(redis.call('get', lastTimeKey))
    -- bucket tokens recovery multiple
    local multiple = math.floor((currentMills - lastTime) / addInterval)
    local recoveryTokenCount = multiple * addToken
    -- must not over the capacity size
    tokenCount = math.min(bucketCapacity, tokenCount + recoveryTokenCount) - 1
    if tokenCount < 0 then
        return -1
    end
    -- reset expire time
    redis.call('set', key, tokenCount, 'EX', resetTime)
    redis.call('set', lastTimeKey, lastTime + multiple * addInterval, 'EX', resetTime)
    return tokenCount
else
    -- first time, full tokens in bucket
    tokenCount = bucketCapacity -1
    -- set expire time
    redis.call('set', key, tokenCount, 'EX', resetTime)
    redis.call('set', lastTimeKey, currentMills, 'EX', resetTime)
    return tokenCount
end





