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
package org.mybatis.caches.redis.serialize;

import org.mybatis.caches.redis.Constants;

/**
 * @author Rogar·Q
 * serialize factory
 */
public class SerializerFactory {

    private Serializer serializer;

    private SerializerFactory() {
        // prevent instantiation
    }

    public SerializerFactory(Serializer serializer) {
        this.serializer = serializer;
    }

    public SerializerFactory(String serializerType) {
        if (Constants.KRYO.equals(serializerType)) {
            this.serializer = new KryoSerializer();
        } else {
            //if ("jdk".equals(type))
            this.serializer = new JdkSerializer();
        }
    }

    public byte[] serialize(Object object) {
        return serializer.serialize(object);
    }

    public Object deserialize(byte[] bytes) {
        return serializer.deserialize(bytes);
    }
}
