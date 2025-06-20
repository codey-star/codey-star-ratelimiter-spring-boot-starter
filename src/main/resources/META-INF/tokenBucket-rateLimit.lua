-- 1. 参数定义
local tokens_key = KEYS[1]                   -- 存储当前令牌数量的 key
local timestamp_key = KEYS[2]                -- 存储上次更新时间的 key
local rate = tonumber(ARGV[1])               -- 令牌生成速率（每秒多少个）
local capacity = tonumber(ARGV[2])           -- 桶的最大容量（最多存多少个令牌）
local requested = tonumber(ARGV[3])          -- 当前请求需要的令牌数
local now = redis.call('TIME')[1]            -- 当前时间戳（秒）

-- 2. 获取当前令牌数和上次更新时间
local current_tokens = tonumber(redis.call("get", tokens_key)) or capacity
local last_time = tonumber(redis.call("get", timestamp_key)) or now

-- 3. 计算新增令牌数
local time_passed = math.max(0, now - last_time)
local new_tokens = math.min(capacity, current_tokens + (time_passed * rate))

-- 4. 判断是否可以通过请求
local allowed = new_tokens >= requested
local final_tokens = new_tokens
if allowed then
    final_tokens = new_tokens - requested
end

-- 5. 更新Redis
local ttl = math.floor(capacity/rate * 2)    -- 设置TTL为填充时间的2倍
redis.call("setex", tokens_key, ttl, final_tokens)
redis.call("setex", timestamp_key, ttl, now)

return { allowed and 1 or 0, final_tokens }
