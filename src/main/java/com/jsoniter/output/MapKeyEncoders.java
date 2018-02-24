package com.jsoniter.output;

import com.jsoniter.spi.*;

import java.io.IOException;
import java.lang.reflect.Type;

class MapKeyEncoders {

    public static Encoder registerOrGetExisting(Type mapKeyType) {
        String cacheKey = JsoniterSpi.getMapKeyEncoderCacheKey(mapKeyType);
        Encoder mapKeyEncoder = JsoniterSpi.getMapKeyEncoder(cacheKey);
        if (null != mapKeyEncoder) {
            return mapKeyEncoder;
        }
        mapKeyEncoder = createDefaultEncoder(mapKeyType);
        JsoniterSpi.addNewMapEncoder(cacheKey, mapKeyEncoder);
        return mapKeyEncoder;
    }

    private static Encoder createDefaultEncoder(Type mapKeyType) {
        if (mapKeyType == String.class) {
            return new StringKeyEncoder();
        }
        if (mapKeyType == Object.class) {
            return new DynamicKeyEncoder();
        }
        if (mapKeyType instanceof Class) {
            if (Number.class.isAssignableFrom((Class<?>) mapKeyType)) {
                return new NumberKeyEncoder();
            }
        }
        throw new JsonException("can not encode map key type: " + mapKeyType);
    }

    private static class StringKeyEncoder implements Encoder {

        @Override
        public void encode(Object obj, JsonStream stream) throws IOException {
            stream.writeVal(obj);
        }
    }

    private static class NumberKeyEncoder implements Encoder {

        @Override
        public void encode(Object obj, JsonStream stream) throws IOException {
            stream.write('"');
            stream.writeVal(obj);
            stream.write('"');
        }
    }

    private static class DynamicKeyEncoder implements Encoder {

        @Override
        public void encode(Object obj, JsonStream stream) throws IOException {
            Class<?> clazz = obj.getClass();
            if (clazz == Object.class) {
                throw new JsonException("map key type is Object.class, can not be encoded");
            }
            Encoder mapKeyEncoder = registerOrGetExisting(clazz);
            mapKeyEncoder.encode(obj, stream);
        }
    }
}
