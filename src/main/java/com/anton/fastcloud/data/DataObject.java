package com.anton.fastcloud.data;

import com.google.common.collect.MapMaker;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public abstract class DataObject {
    public UUID id = UUID.randomUUID();

    public static ConcurrentMap<UUID, DataObject> instances = new MapMaker().weakValues().makeMap();
}
