package com.anton.fastcloud;

import java.nio.ByteBuffer;

public interface ISerializer<T> {
    void serialize(ByteBuffer buffer, T object);

    T deserialize(ByteBuffer buffer);
}
