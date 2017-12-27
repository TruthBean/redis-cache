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

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * @author UNKOWN
 */
public class RedisConfig extends JedisPoolConfig {

    /**
     * simple,
     * sentinel,
     * cluster
     */
    private String connectionMode = "simple";
    private String sentinelMasterName = "master";
    private String sentinelNodes = "127.0.0.1:26379,127.0.0.1:26380";
    private String clusterNodes = "127.0.0.1:6379,127.0.0.1:6380,127.0.0.1:6381";
    private int maxAttempts = 5;

    private String host = Protocol.DEFAULT_HOST;
    private int port = Protocol.DEFAULT_PORT;

    private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
    private int soTimeout = Protocol.DEFAULT_TIMEOUT;

    private String password;

    private int database = Protocol.DEFAULT_DATABASE;
    private String clientName;

    private String serializerType;

    private boolean ssl;
    private String sslKeyStoreType;
    private String sslTrustStoreFile;
    private String sslProtocol;
    private String sslAlgorithm;

    /**
     * should delete data by redis itself
     */
    private int redisCacheExpireTime;

    /**
     * redis prefix key
     */
    private String keyPre = "mybatis:cache:";

    public String getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getSentinelMasterName() {
        return sentinelMasterName;
    }

    public void setSentinelMasterName(String sentinelMasterName) {
        this.sentinelMasterName = sentinelMasterName;
    }

    public String getSentinelNodes() {
        return sentinelNodes;
    }

    public void setSentinelNodes(String sentinelNodes) {
        this.sentinelNodes = sentinelNodes;
    }

    public String getClusterNodes() {
        return clusterNodes;
    }

    public void setClusterNodes(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (host == null || "".equals(host)) {
            host = Protocol.DEFAULT_HOST;
        }
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if ("".equals(password)) {
            password = null;
        }
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        if ("".equals(clientName)) {
            clientName = null;
        }
        this.clientName = clientName;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getRedisCacheExpireTime() {
        return redisCacheExpireTime;
    }

    public void setRedisCacheExpireTime(int redisCacheExpireTime) {
        this.redisCacheExpireTime = redisCacheExpireTime;
    }

    public String getKeyPre() {
        return keyPre;
    }

    public void setKeyPre(String keyPre) {
        if (keyPre != null && !"".equals(keyPre)) {
            this.keyPre = keyPre;
        }
    }

    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    public void setSslKeyStoreType(String sslKeyStoreType) {
        this.sslKeyStoreType = sslKeyStoreType;
    }

    public String getSslTrustStoreFile() {
        return sslTrustStoreFile;
    }

    public void setSslTrustStoreFile(String sslTrustStoreFile) {
        this.sslTrustStoreFile = sslTrustStoreFile;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getSslAlgorithm() {
        return sslAlgorithm;
    }

    public void setSslAlgorithm(String sslAlgorithm) {
        this.sslAlgorithm = sslAlgorithm;
    }

    public String getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(String serializerType) {
        this.serializerType = serializerType;
    }
}
