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

//    private void addBufferIfNeeded(int position) {
//        if (position / ByteBufferPool.BUFFER_SIZE >= buffers.size()) {
//            buffers.add(ByteBufferPool.take());
//        }
//    }
//
//    private ByteBuffer getCurrentBuffer() {
//        int index = position / ByteBufferPool.BUFFER_SIZE;
//        addBufferIfNeeded(index);
//        return buffers.get(index);
//    }
//
//    public void writeByte(byte n) {
//        ByteBuffer buffer = getCurrentBuffer();
//        buffer.position(position);
//        addBufferIfNeeded(position + 1);
//        buffer.put(n);
//        position = buffer.position();
//    }

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

    public ByteBuffer[] toByteBuffers() {
        ByteBuffer[] byteBuffers = buffers.toArray(new ByteBuffer[0]);
        buffers.clear();
        Stream.of(byteBuffers).forEach(ByteBuffer::rewind);
        return byteBuffers;
    }
}
