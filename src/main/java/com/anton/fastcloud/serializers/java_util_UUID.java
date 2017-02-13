package com.anton.fastcloud.serializers;

import java.nio.ByteBuffer;
import java.util.UUID;

public class java_util_UUID {
    public static void serialize(ByteBuffer buffer, UUID object) {
        buffer.putLong(object.getMostSignificantBits());
        buffer.putLong(object.getLeastSignificantBits());
    }

    public static UUID deserialize(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
