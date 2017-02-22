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
    public void writeAndReadByte() {
        Random random = new Random();
        byte oldValue = (byte) random.nextInt();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        oldBuffer.writeByte(oldValue);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        oldBuffer.free();
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        byte newValue = newBuffer.readByte();
        newBuffer.free();
        Assert.assertEquals(oldValue, newValue);
    }

    @Test
    public void writeAndReadInt() {
        Random random = new Random();
        int oldValue = random.nextInt();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        oldBuffer.writeInt(oldValue);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        oldBuffer.free();
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        int newValue = newBuffer.readInt();
        newBuffer.free();
        Assert.assertEquals(oldValue, newValue);
    }

    @Test
    public void writeAndReadLong() {
        Random random = new Random();
        long oldValue = random.nextLong();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        oldBuffer.writeLong(oldValue);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        oldBuffer.free();
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        long newValue = newBuffer.readLong();
        newBuffer.free();
        Assert.assertEquals(oldValue, newValue);
    }

    @Test
    public void writeAndReadDouble() {
        Random random = new Random();
        double oldValue = random.nextDouble();
        ContinuousBuffer oldBuffer = new ContinuousBuffer();
        oldBuffer.writeDouble(oldValue);
        ByteBuffer[] byteBuffers = oldBuffer.toByteBuffers();
        oldBuffer.free();
        ContinuousBuffer newBuffer = new ContinuousBuffer(byteBuffers);
        double newValue = newBuffer.readDouble();
        newBuffer.free();
        System.out.println(oldValue);
        System.out.println(newValue);
        Assert.assertEquals(oldValue, newValue, 0);
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
