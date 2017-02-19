package com.anton.fastcloud.util;

import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;

public class IOUtils {
    public static XnioWorker createWorker() {
        try {
            return Xnio.getInstance().createWorker(
                    OptionMap.builder()
                            .set(Options.TCP_NODELAY, true)
                            .set(Options.WORKER_TASK_CORE_THREADS, Runtime.getRuntime().availableProcessors())
                            .set(Options.WORKER_TASK_MAX_THREADS, Runtime.getRuntime().availableProcessors())
                            .set(Options.WORKER_NAME, "fastcloud")
                            .getMap()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
