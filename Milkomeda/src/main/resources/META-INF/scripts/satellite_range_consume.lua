local queueName = KEYS[1]
local batchSize = tonumber(ARGV[1])

local messages = redis.call('LRANGE', queueName, 0, batchSize-1)
if #messages > 0 then
    redis.call('LTRIM', queueName, #messages, -1)
end

return messages