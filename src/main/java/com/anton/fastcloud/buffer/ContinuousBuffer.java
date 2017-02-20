package com.anton.fastcloud.buffer;

import com.google.common.collect.Iterables;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ContinuousBuffer {
    private final List<ByteBuffer> buffers = new ArrayList<>();
    private EnumState state;

    public ContinuousBuffer() {
        buffers.add(ByteBufferPool.take());
        state = EnumState.WRITE;
    }

    public ContinuousBuffer(ByteBuffer[] byteBuffers) {
        buffers.addAll(Arrays.asList(byteBuffers));
        state = EnumState.READ;
    }

    private void checkState(EnumState expectedState) {
        if (state != expectedState) {
            throw new IllegalStateException("Expected to be in " + expectedState + ", but were in " + state);
        }
    }

    public void writeBoolean(boolean n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 1) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.put((byte) (n ? 1 : 0));
    }

    public boolean readBoolean() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        boolean n = buffer.get() != 0;
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeByte(byte n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 1) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.put(n);
    }

    public byte readByte() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        byte n = buffer.get();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeShort(short n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 2) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putShort(n);
    }

    public short readShort() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        short n = buffer.getShort();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeChar(char n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 2) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putChar(n);
    }

    public char readChar() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        char n = buffer.getChar();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeInt(int n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putInt(n);
    }

    public int readInt() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        int n = buffer.getInt();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeFloat(float n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putFloat(n);
    }

    public float readFloat() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        float n = buffer.getFloat();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeLong(long n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 8) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putLong(n);
    }

    public long readLong() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        long n = buffer.getLong();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeDouble(double n) {
        checkState(EnumState.WRITE);
        ByteBuffer buffer = Iterables.getLast(buffers);
        if (buffer.remaining() < 4) {
            buffer.limit(buffer.position());
            buffer = ByteBufferPool.take();
            buffers.add(buffer);
        }
        buffer.putDouble(n);
    }

    public double readDouble() {
        checkState(EnumState.READ);
        ByteBuffer buffer = buffers.get(0);
        double n = buffer.getDouble();
        if (!buffer.hasRemaining()) {
            buffers.remove(buffer);
            ByteBufferPool.release(buffer);
            if (buffers.isEmpty()) {
                state = EnumState.INVALID;
            }
        }
        return n;
    }

    public void writeByteArray(byte[] n) {
        checkState(EnumState.WRITE);
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
        checkState(EnumState.WRITE);
        ByteBuffer[] byteBuffers = buffers.toArray(new ByteBuffer[0]);
        buffers.clear();
        Stream.of(byteBuffers).forEach(ByteBuffer::rewind);
        state = EnumState.INVALID;
        return byteBuffers;
    }

    private enum EnumState {
        WRITE,
        READ,
        INVALID
    }
}
