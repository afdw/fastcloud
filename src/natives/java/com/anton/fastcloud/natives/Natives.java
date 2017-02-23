package com.anton.fastcloud.natives;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Natives {
    private static boolean inited = false;
    private static final String[] LIB_NAMES = {"libnatives.so"};

    public static void init() {
        if (inited) {
            return;
        }
        try {
            Path tempDirPath = Files.createTempDirectory("fastcloud-natives");
            for (String libName : LIB_NAMES) {
                Path libPath = tempDirPath.resolve(libName);
                Files.copy(Natives.class.getResourceAsStream("/" + libName), libPath);
                System.load(libPath.toString());
                if (!libPath.toFile().delete()) {
                    throw new IOException("Can't remove temp files");
                }
            }
            if (!tempDirPath.toFile().delete()) {
                throw new IOException("Can't remove temp files");
            }
            inited = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
