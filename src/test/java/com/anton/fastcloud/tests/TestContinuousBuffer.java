package com.anton.fastcloud.tests;

import com.anton.fastcloud.buffer.ByteBufferPool;
import com.anton.fastcloud.buffer.ContinuousBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestContinuousBuffer {
    @Test
    public void writeAndReadManyInts() {
        int count = 1024 * 1024;
        Random random = new Random();
        int[] oldArray = IntStream.range(0, count).map(i -> random.nextInt()).toArray();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        Arrays.stream(oldArray).forEach(oldBuffer::writeInt);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        Assert.assertEquals(count / ByteBufferPool.BUFFER_SIZE * 4, byteBuffers.length);
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        int[] newArray = IntStream.range(0, count).map(i -> newBuffer.readInt()).toArray();
        Stream.of(newBuffer.toByteBuffers()).forEach(ByteBufferPool::release);
        Assert.assertArrayEquals(oldArray, newArray);
    }
}
