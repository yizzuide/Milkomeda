local key = KEYS[1]
-- map string -> table
local phases = cjson.decode(ARGV[1])
local percent = ARGV[2]
if redis.call('exists', key) == 0 then
    redis.call('set', key, 0)
    return 0
end
local current = tonumber(redis.call('get', key))
local index = 0
-- lua table start index 1
for i = 1, #phases, 1 do
    local p = tonumber(phases["p"..i])
    if i > 1 then
        p = p + tonumber(phases["p"..(i - 1)])
    end
    if current < p then
        index = i - 1
        break
    end
end
current = (current + 1) % tonumber(percent)
redis.call('set', key, current)
return index
