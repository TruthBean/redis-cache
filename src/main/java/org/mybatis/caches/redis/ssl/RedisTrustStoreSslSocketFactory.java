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
package org.mybatis.caches.redis.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class RedisTrustStoreSslSocketFactory {

    private static final Logger logger = LoggerFactory.getLogger(RedisTrustStoreSslSocketFactory.class);

    private SSLSocketFactory sslSocketFactory;

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * Creates an SSLSocketFactory that trusts all certificates in truststore.
     */
    public RedisTrustStoreSslSocketFactory(String sslKeyStoreType, String sslTrustStoreFile, String sslProtocol, String sslAlgorithm) {
        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(sslKeyStoreType);
        } catch (KeyStoreException e) {
            logger.error("", e);
            return;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sslTrustStoreFile);
            trustStore.load(inputStream, null);
        } catch (FileNotFoundException e) {
            logger.error("", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
        } catch (CertificateException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }

        if (trustStore == null) {
            return;
        }

        SSLContext sslContext = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(sslAlgorithm);
            trustManagerFactory.init(trustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            sslContext = SSLContext.getInstance(sslProtocol);
            sslContext.init(null, trustManagers, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
        } catch (KeyStoreException e) {
            logger.error("", e);
        } catch (KeyManagementException e) {
            logger.error("", e);
        }
        sslSocketFactory = sslContext.getSocketFactory();
    }
}
