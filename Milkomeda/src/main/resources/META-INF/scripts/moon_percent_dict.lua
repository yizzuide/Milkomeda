local key = KEYS[1]
-- map -> table
local phases = cjson.decode(ARGV[1])
local len = tonumber(ARGV[2])
local percent = tonumber(ARGV[3])
if redis.call('exists', key) == 0 then
    redis.call('set', key, 0)
    return 0
end
local current = tonumber(redis.call('get', key))
current = (current + 1) % percent
local index = 0
-- lua table start index 1
for i = 1, len, 1 do
    local p = tonumber(phases["p"..i])
    if i > 1 then
        p = p + tonumber(phases["p"..(i - 1)])
    end
    if current < p then
        index = i - 1
        break
    end
end
redis.call('set', key, current)
return index
