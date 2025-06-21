-- 1. 参数定义
local bucket_key = KEYS[1]                   -- 当前桶中的水量 key
local last_leak_key = KEYS[2]                -- 上次漏水时间 key
local capacity = tonumber(ARGV[1])           -- 漏桶容量
local leak_rate = tonumber(ARGV[2])          -- 每秒漏水速度（处理速度）
local water_added = tonumber(ARGV[3])        -- 本次请求加水量（通常是 1）
local now = redis.call('TIME')[1]            -- 获取当前时间戳（秒）

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
local ttl = math.max(1, math.floor(capacity / leak_rate * 2))    -- 设置TTL为排空时间的2倍
if ttl <= 0 then
    ttl = 1  -- 设置最小过期时间为 1 秒
end
redis.call("setex", bucket_key, ttl, final_water)
redis.call("setex", last_leak_key, ttl, now)

-- 6. 返回结果
-- allowed: 是否允许
-- final_water: 最终水量

return { allowed and 1 or 0, final_water }
