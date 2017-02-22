package com.anton.fastcloud.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ContinuousBuffer {
    private long address;
    private long capacity;
    private long position;
    private long size;

    public ContinuousBuffer() {
        init();
    }

    public ContinuousBuffer(ByteBuffer[] byteBuffers) {
        this();
        for (ByteBuffer byteBuffer : byteBuffers) {
            writeByteBuffer(byteBuffer);
        }
        rewind();
    }

    private native void init();

    public native void writeInt(int value);

    public native int readInt();

    public native void writeByteBuffer(ByteBuffer byteBuffer);

    public native ByteBuffer readByteBuffer(int writeSize);

    public native void free();

    public void rewind() {
        position = 0;
    }

    public ByteBuffer[] toByteBuffers() {
        rewind();
        List<ByteBuffer> byteBuffersList = new ArrayList<>();
        for (int i = 0; i < size; i += Integer.MAX_VALUE) {
            byteBuffersList.add(readByteBuffer(Math.min((int) (size - i), Integer.MAX_VALUE)));
        }
        return byteBuffersList.toArray(new ByteBuffer[0]);
    }

    @Override
    public String toString() {
        return "ContinuousBuffer{" + "address=" + address + ", capacity=" + capacity + ", position=" + position + ", size=" + size + "}";
    }
}
