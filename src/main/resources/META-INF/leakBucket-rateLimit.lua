-- 1. 参数定义
local bucket_key = KEYS[1]                    -- 漏桶容量key
local last_leak_key = KEYS[2]                -- 上次漏水时间key
local capacity = tonumber(ARGV[1])           -- 漏桶容量
local leak_rate = tonumber(ARGV[2])          -- 漏水速率(每秒)
local water_added = tonumber(ARGV[3])        -- 这次请求加水量
local now = redis.call('TIME')[1]            -- 当前时间戳

-- 2. 获取当前水量和上次漏水时间
local current_water = tonumber(redis.call("get", bucket_key)) or 0
local last_leak_time = tonumber(redis.call("get", last_leak_key)) or now

-- 3. 计算漏掉的水量
local time_passed = math.max(0, now - last_leak_time)
local leaked_water = time_passed * leak_rate
local current_water = math.max(0, current_water - leaked_water)

-- 4. 判断是否可以加水
local allowed = (current_water + water_added) <= capacity
local final_water = current_water
if allowed then
    final_water = current_water + water_added
end

-- 5. 更新Redis
local ttl = math.floor(capacity/leak_rate * 2)    -- 设置TTL为排空时间的2倍
redis.call("setex", bucket_key, ttl, final_water)
redis.call("setex", last_leak_key, ttl, now)

-- 6. 返回结果
-- allowed: 是否允许
-- final_water: 最终水量

return { allowed and 1 or 0, final_water }
