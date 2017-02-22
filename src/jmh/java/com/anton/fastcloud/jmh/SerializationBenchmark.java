package com.anton.fastcloud.jmh;

import com.anton.fastcloud.buffer.ByteBufferPool;
import com.anton.fastcloud.data.DataObject;
import com.anton.fastcloud.serialization.ISerializer;
import com.anton.fastcloud.serialization.SerializersClassLoader;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;

@State(Scope.Benchmark)
public class SerializationBenchmark {
    private static Test testOld = new Test(345);
    private static ISerializer<Test> serializer;

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public int serialize() {
        serializer = SerializersClassLoader.getSerializer(Test.class);
        ByteBuffer buffer = ByteBufferPool.take();
        serializer.serialize(buffer, testOld);
        buffer.rewind();
        Test testNew = serializer.deserialize(buffer);
        ByteBufferPool.release(buffer);
        return testOld.test;
    }

    public static class Test extends DataObject {
        public int test;

        public Test() {
        }

        public Test(int test) {
            this.test = test;
        }
    }
}
