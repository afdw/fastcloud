package com.anton.fastcloud.test;

import com.anton.fastcloud.buffer.ContinuousBuffer;
import com.anton.fastcloud.natives.Natives;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class TestContinuousBuffer {
    static {
        Natives.init();
    }

    @Test
    public void writeAndReadManyInts() {
        int count = 1024 * 1024;
        Random random = new Random();
        int[] oldArray = IntStream.range(0, count).map(i -> random.nextInt()).toArray();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        Arrays.stream(oldArray).forEach(oldBuffer::writeInt);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        oldBuffer.free();
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        int[] newArray = IntStream.range(0, count).map(i -> newBuffer.readInt()).toArray();
        newBuffer.free();
        Assert.assertArrayEquals(oldArray, newArray);
    }
}
