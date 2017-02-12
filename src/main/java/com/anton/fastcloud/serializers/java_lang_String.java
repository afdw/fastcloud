package com.anton.fastcloud.serializers;

import java.nio.ByteBuffer;

public class java_lang_String {
    public static void serialize(ByteBuffer buffer, String object) {
//        System.out.println(buffer);
//        System.out.println(object);
//        buffer.putInt(object.length());
//        buffer.put(object.getBytes());
    }

    public static String deserialize(ByteBuffer buffer) {
        byte bytes[] = new byte[buffer.getInt()];
        buffer.get(bytes);
        return new String(bytes);
    }
}
