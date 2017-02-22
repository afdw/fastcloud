package com.anton.fastcloud.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class ByteBufferPool {
    public static final int BUFFER_SIZE = 1024 * 1024 * 2;
    private static final Queue<ByteBuffer> buffers = new ArrayDeque<>();

    public static ByteBuffer take() {
        synchronized (buffers) {
            if (buffers.isEmpty()) {
                return ByteBuffer.allocateDirect(BUFFER_SIZE);
            } else {
                assert buffers.peek().position() == 0;
                return buffers.poll();
            }
        }
    }

    public static void release(ByteBuffer buffer) {
        synchronized (buffers) {
            buffer.rewind();
            buffer.limit(buffer.capacity());
            buffers.add(buffer);
        }
    }
}
