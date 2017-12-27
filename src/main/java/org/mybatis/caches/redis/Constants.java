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

/**
 * @author Rogar·Q
 */
public class Constants {
    private Constants() {
    }

    public static final String CLUSTER = "cluster";

    public static final String SIMPLE = "simple";

    public static final String SENTINEL = "sentinel";

    public static final String COLON = ":";

    public static final String COMMA = ",";

    public static final String KRYO = "kryo";

    public static final String JDK = "jdk";
}
