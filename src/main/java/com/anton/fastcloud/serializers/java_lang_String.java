package com.anton.fastcloud.serializers;

import com.anton.fastcloud.INonStaticSerializer;

import java.nio.ByteBuffer;

public class java_lang_String implements INonStaticSerializer {
    public static void serialize(ByteBuffer buffer, String object) {
        buffer.putInt(object.length());
        buffer.put(object.getBytes());
    }

    public static String deserialize(ByteBuffer buffer) {
        byte bytes[] = new byte[buffer.getInt()];
        buffer.get(bytes);
        return new String(bytes);
    }

    @Override
    public void serializeNonStatic(ByteBuffer buffer, Object object) {
        serialize(buffer, (String) object);
    }

    @Override
    public Object deserializeNonStatic(ByteBuffer buffer) {
        return deserialize(buffer);
    }
}
