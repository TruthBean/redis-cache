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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;

/**
 * Very basic hostname verifier implementation for testing. NOT recommended
 * for production.
 *
 * @author UNKOWN
 */
public class RedisBasicHostnameVerifier implements HostnameVerifier {

      private static final String COMMON_NAME_RDN_PREFIX = "CN=";

      @Override
      public boolean verify(String hostname, SSLSession session) {
        X509Certificate peerCertificate;
        try {
          peerCertificate = (X509Certificate) session.getPeerCertificates()[0];
        } catch (SSLPeerUnverifiedException e) {
          throw new IllegalStateException("The session does not contain a peer X.509 certificate.");
        }
        String peerCertificateCN = getCommonName(peerCertificate);
        return hostname.equals(peerCertificateCN);
      }

      private String getCommonName(X509Certificate peerCertificate) {
        String subjectDN = peerCertificate.getSubjectDN().getName();
        String[] dnComponents = subjectDN.split(",");
        for (String dnComponent : dnComponents) {
          if (dnComponent.startsWith(COMMON_NAME_RDN_PREFIX)) {
            return dnComponent.substring(COMMON_NAME_RDN_PREFIX.length());
          }
        }
        throw new IllegalArgumentException("The certificate has no common name.");
      }

}
