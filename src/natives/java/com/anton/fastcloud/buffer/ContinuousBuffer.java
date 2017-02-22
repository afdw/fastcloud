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

    public native void writeFloat(float value);

    public native float readFloat();

    public native void writeLong(long value);

    public native long readLong();

    public native void writeDouble(double value);

    public native double readDouble();

    public native void writeBooleanArray(boolean[] array);

    public native boolean[] readBooleanArray(int arraySize);

    public native void writeByteArray(byte[] array);

    public native byte[] readByteArray(int arraySize);

    public native void writeShortArray(short[] array);

    public native short[] readShortArray(int arraySize);

    public native void writeCharArray(char[] array);

    public native char[] readCharArray(int arraySize);

    public native void writeIntArray(int[] array);

    public native int[] readIntArray(int arraySize);

    public native void writeFloatArray(float[] array);

    public native float[] readFloatArray(int arraySize);

    public native void writeLongArray(long[] array);

    public native long[] readLongArray(int arraySize);

    public native void writeDoubleArray(double[] array);

    public native double[] readDoubleArray(int arraySize);

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
