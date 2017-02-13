package com.anton.fastcloud.serializers;

import com.anton.fastcloud.ISerializer;

import java.nio.ByteBuffer;
import java.util.UUID;

public class java_util_UUID implements ISerializer<UUID> {
    public static final java_util_UUID instance = new java_util_UUID();

    private java_util_UUID() {
    }

    @Override
    public void serialize(ByteBuffer buffer, UUID object) {
        buffer.putLong(object.getMostSignificantBits());
        buffer.putLong(object.getLeastSignificantBits());
    }

    @Override
    public UUID deserialize(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
