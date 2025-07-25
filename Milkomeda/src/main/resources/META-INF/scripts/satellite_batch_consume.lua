local queueName = KEYS[1]
local batchSize = tonumber(ARGV[1])
local timeout = tonumber(ARGV[2])

local messages = {}
local currentSize = redis.call('LLEN', queueName)

if currentSize > 0 then
    local actualSize = math.min(batchSize, currentSize)
    for i = 1, actualSize do
        local message = redis.call('LPOP', queueName)
        table.insert(messages, message)
    end
    return messages
else
    if timeout > 0 then
        redis.call('BLPOP', queueName, timeout/1000)
        local newSize = redis.call('LLEN', queueName)
        local actualSize = math.min(batchSize, newSize)
        for i = 1, actualSize do
            local message = redis.call('LPOP', queueName)
            table.insert(messages, message)
        end
        return messages
    else
        return messages
    end
end