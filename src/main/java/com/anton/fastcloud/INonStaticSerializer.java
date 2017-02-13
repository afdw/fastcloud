package com.anton.fastcloud;

import java.nio.ByteBuffer;

public interface INonStaticSerializer {
    void serializeNonStatic(ByteBuffer buffer, Object object);

    Object deserializeNonStatic(ByteBuffer buffer);
}
