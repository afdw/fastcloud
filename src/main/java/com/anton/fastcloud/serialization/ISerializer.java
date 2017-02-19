package com.anton.fastcloud.serialization;

import java.nio.ByteBuffer;

public interface ISerializer<T> {
    void serialize(ByteBuffer buffer, T object);

    T deserialize(ByteBuffer buffer);
}
