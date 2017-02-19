package com.anton.fastcloud.serializers;

import com.anton.fastcloud.serialization.ISerializer;

import java.nio.ByteBuffer;

public class java_lang_String implements ISerializer<String> {
    public static final java_lang_String instance = new java_lang_String();

    private java_lang_String() {
    }

    @Override
    public void serialize(ByteBuffer buffer, String object) {
        buffer.putInt(object.length());
        buffer.put(object.getBytes());
    }

    @Override
    public String deserialize(ByteBuffer buffer) {
        byte bytes[] = new byte[buffer.getInt()];
        buffer.get(bytes);
        return new String(bytes);
    }
}
