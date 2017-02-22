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

    public native void writeBoolean(boolean value);

    public native boolean readBoolean();

    public native void writeByte(byte value);

    public native byte readByte();

    public native void writeShort(short value);

    public native short readShort();

    public native void writeChar(char value);

    public native char readChar();

    public native void writeInt(int value);

    public native int readInt();

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public native void writeLong(long value);

    public native long readLong();

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

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
