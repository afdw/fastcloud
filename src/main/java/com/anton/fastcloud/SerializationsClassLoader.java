package com.anton.fastcloud;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class SerializationsClassLoader extends URLClassLoader {
    public static final SerializationsClassLoader INSTANCE = new SerializationsClassLoader();
    public static final String PREFIX = "com.anton.fastcloud.serializers.";

    public SerializationsClassLoader() {
        super(new URL[0]);
    }

    public static String getSerializerNameFromClass(Class<?> clazz) {
        return PREFIX + clazz.getName().replaceAll("\\.", "_");
    }

    public static Class<?> getClassFromSerializerName(String serializerName) {
        try {
            return Class.forName(serializerName.substring(PREFIX.length()).replaceAll("_", "."));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(PREFIX)) {
            Class<?> clazz = getClassFromSerializerName(name);
            if (clazz != null) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classWriter.visit(
                        Opcodes.V1_7,
                        Opcodes.ACC_PUBLIC,
                        name.replaceAll("\\.", "/"),
                        null,
                        "sun/reflect/MagicAccessorImpl",
                        null
                );
                GeneratorAdapter generatorAdapter = new GeneratorAdapter(
                        Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                        new Method("serialize", Type.VOID_TYPE, new Type[]{Type.getType(ByteBuffer.class), Type.getType(clazz)}),
                        null,
                        null,
                        classWriter
                );
                for (Field field : clazz.getFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    generatorAdapter.loadArg(0);
                    generatorAdapter.loadArg(1);
                    generatorAdapter.getField(Type.getType(clazz), field.getName(), Type.getType(field.getType()));
                    generatorAdapter.invokeStatic(
                            Type.getObjectType(getSerializerNameFromClass(field.getType()).replaceAll("\\.", "/")),
                            new Method("serialize", Type.VOID_TYPE, new Type[]{Type.getType(ByteBuffer.class), Type.getType(field.getType())})
                    );
                }
                generatorAdapter.visitInsn(Opcodes.RETURN);
                generatorAdapter.endMethod();
                classWriter.visitEnd();
                byte[] bytes = classWriter.toByteArray();
                if (false) {
                    try {
                        Files.write(new File("test.class").toPath(), bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return defineClass(name, bytes, 0, bytes.length);
            }
        }
        return super.findClass(name);
    }
}
