package com.anton.fastcloud.buffer;

import com.google.common.collect.Iterables;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ContinuousBuffer {
    private final List<ByteBuffer> buffers = new ArrayList<>();

    public ContinuousBuffer() {
        buffers.add(ByteBufferPool.take());
    }

    public ContinuousBuffer(ByteBuffer[] byteBuffers) {
        buffers.addAll(Arrays.asList(byteBuffers));
    }

    public void writeBoolean(boolean n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 1) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.put((byte) (n ? 1 : 0));
    }

    public boolean readBoolean() {
        ByteBuffer buffer = buffers.get(0);
        boolean n = buffer.get() != 0;
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeByte(byte n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 1) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.put(n);
    }

    public byte readByte() {
        ByteBuffer buffer = buffers.get(0);
        byte n = buffer.get();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeShort(short n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 2) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putShort(n);
    }

    public short readShort() {
        ByteBuffer buffer = buffers.get(0);
        short n = buffer.getShort();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeChar(char n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 2) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putChar(n);
    }

    public char readChar() {
        ByteBuffer buffer = buffers.get(0);
        char n = buffer.getChar();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeInt(int n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putInt(n);
    }

    public int readInt() {
        ByteBuffer buffer = buffers.get(0);
        int n = buffer.getInt();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeFloat(float n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putFloat(n);
    }

    public float readFloat() {
        ByteBuffer buffer = buffers.get(0);
        float n = buffer.getFloat();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeLong(long n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 8) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putLong(n);
    }

    public long readLong() {
        ByteBuffer buffer = buffers.get(0);
        long n = buffer.getLong();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeDouble(double n) {
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putDouble(n);
    }

    public double readDouble() {
        ByteBuffer buffer = buffers.get(0);
        double n = buffer.getDouble();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
        }
        return n;
    }

    public void writeByteArray(byte[] n) {
        // TODO: optimize
        for (byte b : n) {
            writeByte(b);
        }
    }

    public byte[] readByteArray(int size) {
        // TODO: optimize
        byte[] n = new byte[size];
        for (int i = 0; i < size; i++) {
            n[i] = readByte();
        }
        return n;
    }

    public ByteBuffer[] toByteBuffers() {
        ByteBuffer[] byteBuffers = buffers.toArray(new ByteBuffer[0]);
        buffers.clear();
        Stream.of(byteBuffers).forEach(ByteBuffer::rewind);
        return byteBuffers;
    }
}
