package com.anton.fastcloud.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ContinuousBuffer {
    private long pointer;

    public ContinuousBuffer() {
        pointer = init();
    }

    public ContinuousBuffer(long startCapacity) {
        pointer = initWithStartCapacity(startCapacity);
    }

    public ContinuousBuffer(ByteBuffer[] byteBuffers) {
        this();
        for (ByteBuffer byteBuffer : byteBuffers) {
            writeByteBuffer(byteBuffer);
        }
        rewind();
    }

    private static native long init();

    private static native long initWithStartCapacity(long startSize);

    private static native long getAddress(long pointer);

    private static native long getCapacity(long pointer);

    private static native long getPosition(long pointer);

    private static native long getSize(long pointer);

    private static native void rewind(long pointer);

    private static native void writeBoolean(long pointer, boolean value);

    private static native boolean readBoolean(long pointer);

    private static native void writeByte(long pointer, byte value);

    private static native byte readByte(long pointer);

    private static native void writeShort(long pointer, short value);

    private static native short readShort(long pointer);

    private static native void writeChar(long pointer, char value);

    private static native char readChar(long pointer);

    private static native void writeInt(long pointer, int value);

    private static native int readInt(long pointer);

    private static native void writeFloat(long pointer, float value);

    private static native float readFloat(long pointer);

    private static native void writeLong(long pointer, long value);

    private static native long readLong(long pointer);

    private static native void writeDouble(long pointer, double value);

    private static native double readDouble(long pointer);

    private static native void writeBooleanArray(long pointer, boolean[] array);

    private static native boolean[] readBooleanArray(long pointer, int arraySize);

    private static native void writeByteArray(long pointer, byte[] array);

    private static native byte[] readByteArray(long pointer, int arraySize);

    private static native void writeShortArray(long pointer, short[] array);

    private static native short[] readShortArray(long pointer, int arraySize);

    private static native void writeCharArray(long pointer, char[] array);

    private static native char[] readCharArray(long pointer, int arraySize);

    private static native void writeIntArray(long pointer, int[] array);

    private static native int[] readIntArray(long pointer, int arraySize);

    private static native void writeFloatArray(long pointer, float[] array);

    private static native float[] readFloatArray(long pointer, int arraySize);

    private static native void writeLongArray(long pointer, long[] array);

    private static native long[] readLongArray(long pointer, int arraySize);

    private static native void writeDoubleArray(long pointer, double[] array);

    private static native double[] readDoubleArray(long pointer, int arraySize);

    private static native void writeByteBuffer(long pointer, ByteBuffer byteBuffer);

    private static native ByteBuffer readByteBuffer(long pointer, int writeSize);

    private static native void free(long pointer);

    public long getAddress() {
        return getAddress(pointer);
    }

    public long getCapacity() {
        return getCapacity(pointer);
    }

    public long getPosition() {
        return getPosition(pointer);
    }

    public long getSize() {
        return getSize(pointer);
    }

    public void rewind() {
        rewind(pointer);
    }

    public void writeBoolean(boolean value) {
        writeBoolean(pointer, value);
    }

    public boolean readBoolean() {
        return readBoolean(pointer);
    }

    public void writeByte(byte value) {
        writeByte(pointer, value);
    }

    public byte readByte() {
        return readByte(pointer);
    }

    public void writeShort(short value) {
        writeShort(pointer, value);
    }

    public short readShort() {
        return readShort(pointer);
    }

    public void writeChar(char value) {
        writeChar(pointer, value);
    }

    public char readChar() {
        return readChar(pointer);
    }

    public void writeInt(int value) {
        writeInt(pointer, value);
    }

    public int readInt() {
        return readInt(pointer);
    }

    public void writeFloat(float value) {
        writeFloat(pointer, value);
    }

    public float readFloat() {
        return readFloat(pointer);
    }

    public void writeLong(long value) {
        writeLong(pointer, value);
    }

    public long readLong() {
        return readLong(pointer);
    }

    public void writeDouble(double value) {
        writeDouble(pointer, value);
    }

    public double readDouble() {
        return readDouble(pointer);
    }

    public void writeBooleanArray(boolean[] array) {
        writeBooleanArray(pointer, array);
    }

    public boolean[] readBooleanArray(int arraySize) {
        return readBooleanArray(pointer, arraySize);
    }

    public void writeByteArray(byte[] array) {
        writeByteArray(pointer, array);
    }

    public byte[] readByteArray(int arraySize) {
        return readByteArray(pointer, arraySize);
    }

    public void writeShortArray(short[] array) {
        writeShortArray(pointer, array);
    }

    public short[] readShortArray(int arraySize) {
        return readShortArray(pointer, arraySize);
    }

    public void writeCharArray(char[] array) {
        writeCharArray(pointer, array);
    }

    public char[] readCharArray(int arraySize) {
        return readCharArray(pointer, arraySize);
    }

    public void writeIntArray(int[] array) {
        writeIntArray(pointer, array);
    }

    public int[] readIntArray(int arraySize) {
        return readIntArray(pointer, arraySize);
    }

    public void writeFloatArray(float[] array) {
        writeFloatArray(pointer, array);
    }

    public float[] readFloatArray(int arraySize) {
        return readFloatArray(pointer, arraySize);
    }

    public void writeLongArray(long[] array) {
        writeLongArray(pointer, array);
    }

    public long[] readLongArray(int arraySize) {
        return readLongArray(pointer, arraySize);
    }

    public void writeDoubleArray(double[] array) {
        writeDoubleArray(pointer, array);
    }

    public double[] readDoubleArray(int arraySize) {
        return readDoubleArray(pointer, arraySize);
    }

    public void writeByteBuffer(ByteBuffer byteBuffer) {
        writeByteBuffer(pointer, byteBuffer);
    }

    public ByteBuffer readByteBuffer(int writeSize) {
        return readByteBuffer(pointer, writeSize);
    }

    public void free() {
        free(pointer);
    }

    public ByteBuffer[] toByteBuffers() {
        rewind();
        List<ByteBuffer> byteBuffersList = new ArrayList<>();
        for (int i = 0; i < getSize(); i += Integer.MAX_VALUE) {
            byteBuffersList.add(readByteBuffer(Math.min((int) (getSize() - i), Integer.MAX_VALUE)));
        }
        return byteBuffersList.toArray(new ByteBuffer[0]);
    }

    @Override
    public String toString() {
        return "ContinuousBuffer{" + "address=" + getAddress() + ", capacity=" + getCapacity() + ", position=" + getPosition() + ", size=" + getSize() + "}";
    }
}
