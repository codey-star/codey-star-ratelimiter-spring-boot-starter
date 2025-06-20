-- 1. 参数定义
local rateLimitKey = KEYS[1]
local rate = tonumber(ARGV[1])        -- 限制次数
local windowSize = tonumber(ARGV[2])  -- 窗口大小(秒)
local now = tonumber(ARGV[3])         -- 当前时间戳

local allowed = 1
local ttlResult = 0

-- 2. 移除窗口之前的数据
redis.call('ZREMRANGEBYSCORE', rateLimitKey, 0, now - windowSize)

-- 3. 获取当前窗口内的请求数量
local currValue = redis.call('ZCARD', rateLimitKey)

if currValue >= rate then
    -- 4. 超过限制
    allowed = 0
    -- 5. 获取最早的请求时间
    local earliest = redis.call('ZRANGE', rateLimitKey, 0, 0, 'WITHSCORES')[2]
    if earliest then
        -- 6. 计算还需要等待的时间
        ttlResult = math.ceil(earliest - (now - windowSize))
    end
else
    -- 7. 未超过限制，记录本次请求
    redis.call('ZADD', rateLimitKey, now, now)
    -- 8. 设置过期时间
    redis.call('EXPIRE', rateLimitKey, windowSize)
    allowed = 1
end

return { allowed, ttlResult }
