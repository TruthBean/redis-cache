/**
 *    Copyright 2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.caches.redis;

import org.apache.ibatis.cache.Cache;
import org.mybatis.caches.redis.serialize.SerializerFactory;
import org.mybatis.caches.redis.ssl.RedisBasicHostnameVerifier;
import org.mybatis.caches.redis.ssl.RedisTrustStoreSslSocketFactory;
import redis.clients.jedis.*;

import javax.net.ssl.SSLParameters;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Cache adapter for Redis.
 *
 * @author Eduardo Macarron
 */
public final class RedisCache implements Cache {

    private final ReadWriteLock readWriteLock = new DummyReadWriteLock();

    private String id;

    private static JedisPool pool;

    private static JedisSentinelPool jedisSentinelPool;

    private static JedisCluster jedisCluster;

    private final RedisConfig redisConfig;

    private static boolean isSetPassword = true;

    private static boolean isCluster = false;

    private SerializerFactory serializerFactory;

    public RedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        redisConfig = RedisConfigurationBuilder.getInstance().parseConfiguration();
        this.id = redisConfig.getKeyPre() + id;

        if (null == redisConfig.getPassword() || redisConfig.getPassword().length() <= 0) {
            isSetPassword = false;
        }

        this.serializerFactory = new SerializerFactory(redisConfig.getSerializerType());
        config();
    }

    private void config() {

        if (null == redisConfig.getConnectionMode()) {
            redisConfig.setConnectionMode(Constants.SIMPLE);
        }

        if (Constants.CLUSTER.equals(redisConfig.getConnectionMode())) {
            isCluster = true;
        }

        if (Constants.SENTINEL.equals(redisConfig.getConnectionMode())) {
            if (null == redisConfig.getSentinelNodes() || !redisConfig.getSentinelNodes().contains(Constants.COLON)) {
                throw new IllegalArgumentException("sentinelNodes set error! format:[HOST:PORT,HOST:PORT,...]");
            }
            Set<String> sentinelNodes = new HashSet<String>(Arrays.asList(redisConfig.getSentinelNodes().split(",")));
            if (isSetPassword) {
                jedisSentinelPool = new JedisSentinelPool(redisConfig.getSentinelMasterName(), sentinelNodes, redisConfig,
                        redisConfig.getConnectionTimeout(), redisConfig.getSoTimeout(), redisConfig.getPassword(),
                        redisConfig.getDatabase(), redisConfig.getClientName());
            } else {
                jedisSentinelPool = new JedisSentinelPool(redisConfig.getSentinelMasterName(), sentinelNodes, redisConfig,
                        redisConfig.getSoTimeout());
            }

        } else if (Constants.CLUSTER.equals(redisConfig.getConnectionMode())) {
            Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
            if (null == redisConfig.getClusterNodes() || !redisConfig.getClusterNodes().contains(Constants.COLON)
                    || !redisConfig.getClusterNodes().contains(",")) {
                throw new IllegalArgumentException("clusterNodes set error! format:[HOST:PORT,HOST:PORT,...]");
            }

            for (String hostPort : redisConfig.getClusterNodes().split(Constants.COMMA)) {
                String[] hp = hostPort.split(Constants.COLON);
                HostAndPort hostAndPort = new HostAndPort(hp[0], Integer.valueOf(hp[1]));
                hostAndPorts.add(hostAndPort);
            }

            if (isSetPassword) {
                jedisCluster = new JedisCluster(hostAndPorts,redisConfig.getConnectionTimeout(),redisConfig.getSoTimeout(),
                        redisConfig.getMaxAttempts(),redisConfig.getPassword(),redisConfig);
            } else {
                jedisCluster = new JedisCluster(hostAndPorts, redisConfig.getConnectionTimeout(), redisConfig.getSoTimeout(),
                        redisConfig.getMaxAttempts(), redisConfig);
            }

        } else {
            if (redisConfig.isSsl()) {
                RedisTrustStoreSslSocketFactory factory = new RedisTrustStoreSslSocketFactory(redisConfig.getSslKeyStoreType(),
                        redisConfig.getSslTrustStoreFile(), redisConfig.getSslProtocol(), redisConfig.getSslAlgorithm());

                pool = new JedisPool(redisConfig, redisConfig.getHost(), redisConfig.getPort(),
                        redisConfig.getConnectionTimeout(), redisConfig.getSoTimeout(), redisConfig.getPassword(),
                        redisConfig.getDatabase(), redisConfig.getClientName(), redisConfig.isSsl(),
                        factory.getSslSocketFactory(), new SSLParameters(), new RedisBasicHostnameVerifier());
            } else {
                // Socket timeout is same as timeout
                pool = new JedisPool(redisConfig, redisConfig.getHost(), redisConfig.getPort(),
                        redisConfig.getConnectionTimeout(), redisConfig.getPassword(),
                        redisConfig.getDatabase(), redisConfig.getClientName());
            }
        }
    }

    /**
     * TODO Review this is UNUSED
     */
    private Object execute(RedisCallback callback) {
        Jedis jedis = getResource();
        try {
            return callback.doWithRedis(jedis);
        } finally {
            jedis.close();
        }
    }

    private Jedis getResource() {
        if (Constants.SENTINEL.equals(redisConfig.getConnectionMode())) {
            return jedisSentinelPool.getResource();
        } else {
            return pool.getResource();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getSize() {
        if (isCluster) {
            Map<byte[], byte[]> result = jedisCluster.hgetAll(id.getBytes());
            return result.size();
        }
        return (Integer) execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                Map<byte[], byte[]> result = jedis.hgetAll(id.getBytes());
                return result.size();
            }
        });
    }

    @Override
    public void putObject(final Object key, final Object value) {
        if (isCluster) {
            jedisCluster.hset(id.getBytes(), key.toString().getBytes(), serializerFactory.serialize(value));
            return;
        }
        execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                jedis.hset(id.getBytes(), key.toString().getBytes(), serializerFactory.serialize(value));
                int expireTime = redisConfig.getRedisCacheExpireTime();
                if (expireTime > 0) {
                    jedis.expire(id.getBytes(), expireTime);
                }
                return null;
            }
        });
    }

    @Override
    public Object getObject(final Object key) {
        if (isCluster) {
            return serializerFactory.deserialize(jedisCluster.hget(id.getBytes(), key.toString().getBytes()));
        }
        return execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                return serializerFactory.deserialize(jedis.hget(id.getBytes(), key.toString().getBytes()));
            }
        });
    }

    @Override
    public Object removeObject(final Object key) {
        if (isCluster) {
            return jedisCluster.hdel(id, key.toString());
        }
        return execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                return jedis.hdel(id, key.toString());
            }
        });
    }

    @Override
    public void clear() {
        if (isCluster) {
            jedisCluster.del(id);
            return;
        }
        execute(new RedisCallback() {
            @Override
            public Object doWithRedis(Jedis jedis) {
                jedis.del(id);
                return null;
            }
        });

    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        return "Redis {" + id + "}";
    }

}
