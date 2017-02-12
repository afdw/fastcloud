package com.anton.fastcloud;

import com.google.common.collect.MapMaker;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public abstract class DataObject {
    public static UUID id;

    public static ConcurrentMap<UUID, DataObject> instances = new MapMaker().weakValues().makeMap();
}
