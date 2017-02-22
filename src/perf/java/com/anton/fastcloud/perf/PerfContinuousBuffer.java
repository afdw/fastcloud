package com.anton.fastcloud.perf;

import com.anton.fastcloud.buffer.ContinuousBuffer;
import com.anton.fastcloud.natives.Natives;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;

@State(Scope.Benchmark)
public class PerfContinuousBuffer {
    static {
        Natives.init();
    }

    private int[] arr = new int[1024 * 1024];

    @Setup
    public void setup() {
        for(int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void test() {
        ContinuousBuffer buffer = new ContinuousBuffer(arr.length);
        buffer.writeIntArray(arr);
        buffer.rewind();
        if (!Arrays.equals(arr, buffer.readIntArray(arr.length))) {
            throw new AssertionError();
        }
        buffer.free();
    }
}
