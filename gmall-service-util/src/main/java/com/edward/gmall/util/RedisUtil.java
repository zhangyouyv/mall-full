package com.edward.gmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private JedisPool jedisPool;

    public void initPool(String host, int port, int database) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(10 * 1000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, host, port, 20 * 1000);
    }

    public Jedis getJedis() {
        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();
        } catch (RuntimeException e) {
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);//获取连接失败时，应该返回给pool,否则每次发生异常将导致一个jedis对象没有被回收。
            }
        }
        return jedis;
    }

}
