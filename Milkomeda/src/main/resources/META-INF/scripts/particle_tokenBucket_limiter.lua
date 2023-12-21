local key = KEYS[1]
local capacity = tonumber(ARGV[1])
-- the number of tokens required by the request
local requested = tonumber(ARGV[2])
-- put tokens per second
local rate = tonumber(ARGV[3])
local now = tonumber(ARGV[4])

-- key of bucket last update time
local timestamp_key = key..'_update_time'
-- add full tokens need time
local fill_time = capacity / rate
-- tokens expire time
local ttl = math.floor(fill_time * 2)

-- get left token count
local last_tokens = tonumber(redis.call("get", key))
-- init full tokens in bucket when is empty or has not been used for a long time
if last_tokens == nil then
    last_tokens = capacity
end
-- get or init last timestamp
local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
    last_refreshed = 0
end

-- calc pass time
local delta = math.max(0, now - last_refreshed)
-- fill up tokens rate at pass time
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))
-- whether the number of tokens in the token bucket meets the number of tokens required by the request
local allowed = filled_tokens >= requested
-- using for calc left token count
local left_tokens = filled_tokens
-- if return tokens is -1, then limit
local tokens = -1
if allowed then
    left_tokens = filled_tokens - requested
    tokens = left_tokens
end
-- record left tokens and last time
if ttl > 0 then
    redis.call('set', key, left_tokens, 'EX', ttl)
    redis.call('set', timestamp_key, now, 'EX', ttl)
end
return tokens