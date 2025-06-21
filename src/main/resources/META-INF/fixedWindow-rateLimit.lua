-- 1. 参数定义
local rateLimitKey = KEYS[1]
local rate = tonumber(ARGV[1])
local rateInterval = tonumber(ARGV[2])

-- 2. 尝试初始化 key（仅当不存在时）
redis.call('SET', rateLimitKey, 0, 'NX', 'PX', rateInterval * 1000)

-- 3. 自增当前值
local currValue = redis.call('INCR', rateLimitKey)

-- 4. 判断是否允许访问
local allowed = 0
local ttlResult = 0

if currValue <= rate then
    allowed = 1
else
    -- 5. 获取剩余 TTL
    ttlResult = redis.call('TTL', rateLimitKey)
end

return { allowed, ttlResult }