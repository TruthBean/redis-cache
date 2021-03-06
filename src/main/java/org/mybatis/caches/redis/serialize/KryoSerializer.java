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


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.ExternalizableSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Serialize with Kryo, which is faster and space consuming.
 *
 * @author Lei Jiang(ladd.cn@gmail.com)
 */
public final class KryoSerializer implements Serializer {

    private static Kryo kryo;
    private static Output output;
    private static Input input;
    /**
     * classes have been occurred
     */
    private static HashSet<Class> beanClassToSerialize;

    static {
        kryo = new Kryo();
        output = new Output(200, -1);
        input = new Input();
        beanClassToSerialize = new HashSet<Class>();
    }

    KryoSerializer() {
        // prevent instantiation
    }

    @Override
    public byte[] serialize(Object object) {
        output.clear();
        if (!beanClassToSerialize.contains(object.getClass())) {
            //A new class occurs

            try {
                //register default kryo serializer for object class first
                kryo.register(object.getClass());
                kryo.writeClassAndObject(output, object);
            } catch (Exception e) {
                // if default kryo serializer fails, register  javaSerializer or externalizableSerializer as a fallback
                if (object instanceof Serializable) {
                    kryo.register(object.getClass(), new JavaSerializer());
                } else if (object instanceof ExternalizableSerializer) {
                    kryo.register(object.getClass(), new ExternalizableSerializer());
                }
                kryo.writeClassAndObject(output, object);
            } finally {
                beanClassToSerialize.add(object.getClass());
            }
        } else {
            //For class ever occurred, serialize directly
            kryo.writeClassAndObject(output, object);
        }
        return output.toBytes();
    }

    @Override
    public Object deserialize(byte[] bytes) {
        input.setBuffer(bytes);
        return kryo.readClassAndObject(input);
    }
}
