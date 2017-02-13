package com.anton.fastcloud.serializers;

import com.anton.fastcloud.INonStaticSerializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class java_util_UUID implements INonStaticSerializer {
    public static void serialize(ByteBuffer buffer, UUID object) {
        buffer.putLong(object.getMostSignificantBits());
        buffer.putLong(object.getLeastSignificantBits());
    }

    public static UUID deserialize(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    @Override
    public void serializeNonStatic(ByteBuffer buffer, Object object) {
        serialize(buffer, (UUID) object);
    }

    @Override
    public Object deserializeNonStatic(ByteBuffer buffer) {
        return deserialize(buffer);
    }
}
