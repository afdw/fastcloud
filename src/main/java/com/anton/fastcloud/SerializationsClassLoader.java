package com.anton.fastcloud;


import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SerializationsClassLoader extends URLClassLoader {
    public static final SerializationsClassLoader INSTANCE = new SerializationsClassLoader();
    public static final String PREFIX = "com.anton.fastcloud.serializers.";

    public SerializationsClassLoader() {
        super(new URL[0]);
    }

    public static String getSerializerNameFromClass(Class<?> clazz) {
        return PREFIX + clazz.getName()
                .replaceAll("\\.", "_")
                .replaceAll("\\[", "LeftSquareBracket")
                .replaceAll(";", "Semicolon");
    }

    public static Class<?> getClassFromSerializerName(String serializerName) {
        try {
            return Class.forName(serializerName.substring(PREFIX.length())
                    .replaceAll("_", ".")
                    .replaceAll("LeftSquareBracket", "[")
                    .replaceAll("Semicolon", ";")
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(PREFIX)) {
            Class<?> clazz = getClassFromSerializerName(name);
            if (clazz != null && (DataObject.class.isAssignableFrom(clazz) || clazz.isArray())) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classWriter.visit(
                        Opcodes.V1_7,
                        Opcodes.ACC_PUBLIC,
                        name.replaceAll("\\.", "/"),
                        null,
                        "sun/reflect/MagicAccessorImpl",
                        null
                );
                Stream.of("serialize", "deserialize").forEach(methodName -> {
                    GeneratorAdapter generatorAdapter;
                    switch (methodName) {
                        case "serialize":
                            generatorAdapter = new GeneratorAdapter(
                                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                                    new Method(
                                            methodName,
                                            Type.VOID_TYPE,
                                            new Type[] {Type.getType(ByteBuffer.class), Type.getType(clazz)}
                                    ),
                                    null,
                                    null,
                                    classWriter
                            );
                            break;
                        case "deserialize":
                            generatorAdapter = new GeneratorAdapter(
                                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                                    new Method(
                                            methodName,
                                            Type.getType(clazz),
                                            new Type[] {Type.getType(ByteBuffer.class)}
                                    ),
                                    null,
                                    null,
                                    classWriter
                            );
                            break;
                        default:
                            generatorAdapter = null;
                            break;
                    }
                    assert generatorAdapter != null;
                    Map<Class<?>, String> methodNames = new HashMap<>();
                    methodNames.put(boolean.class, "");
                    methodNames.put(byte.class, "");
                    methodNames.put(char.class, "Char");
                    methodNames.put(short.class, "Short");
                    methodNames.put(int.class, "Int");
                    methodNames.put(long.class, "Long");
                    methodNames.put(float.class, "Float");
                    methodNames.put(double.class, "Double");
                    Consumer<Class<?>> generateSerialize = type -> {
                        Class<?> byteBufferFieldType = type;
                        if (type == boolean.class) {
                            byteBufferFieldType = byte.class;
                        }
                        if (type.isPrimitive()) {
                            if (type == boolean.class) {
                                generatorAdapter.cast(Type.BOOLEAN_TYPE, Type.BYTE_TYPE);
                            }
                            generatorAdapter.invokeVirtual(
                                    Type.getType(ByteBuffer.class),
                                    new Method(
                                            "put" + methodNames.get(type),
                                            Type.getType(ByteBuffer.class),
                                            new Type[] {Type.getType(byteBufferFieldType)}
                                    )
                            );
                            generatorAdapter.pop();
                        } else {
                            generatorAdapter.invokeStatic(
                                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                                    new Method(
                                            methodName,
                                            Type.VOID_TYPE,
                                            new Type[] {Type.getType(ByteBuffer.class), Type.getType(type)}
                                    )
                            );
                        }
                    };
                    Consumer<Class<?>> generateDeserialize = type -> {
                        Class<?> byteBufferFieldType = type;
                        if (type == boolean.class) {
                            byteBufferFieldType = byte.class;
                        }
                        if (type.isPrimitive()) {
                            generatorAdapter.invokeVirtual(
                                    Type.getType(ByteBuffer.class),
                                    new Method(
                                            "get" + methodNames.get(type),
                                            Type.getType(byteBufferFieldType),
                                            new Type[0]
                                    )
                            );
                            if (type == boolean.class) {
                                generatorAdapter.cast(Type.BYTE_TYPE, Type.BOOLEAN_TYPE);
                            }
                        } else {
                            generatorAdapter.invokeStatic(
                                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                                    new Method(
                                            methodName,
                                            Type.getType(type),
                                            new Type[] {Type.getType(ByteBuffer.class)}
                                    )
                            );
                        }
                    };
                    Label returnLabel = generatorAdapter.newLabel();
                    Label continueLabel = generatorAdapter.newLabel();
                    switch (methodName) {
                        case "serialize":
                            generatorAdapter.loadArg(0);
                            generatorAdapter.loadArg(1);
                            generatorAdapter.ifNonNull(continueLabel);
                            generatorAdapter.visitLdcInsn(0);
                            generateSerialize.accept(boolean.class);
                            generatorAdapter.goTo(returnLabel);
                            generatorAdapter.mark(continueLabel);
                            generatorAdapter.visitLdcInsn(1);
                            generateSerialize.accept(boolean.class);
                            break;
                        case "deserialize":
                            generatorAdapter.loadArg(0);
                            generateDeserialize.accept(boolean.class);
                            generatorAdapter.ifZCmp(GeneratorAdapter.NE, continueLabel);
                            generatorAdapter.visitInsn(Opcodes.ACONST_NULL);
                            generatorAdapter.goTo(returnLabel);
                            generatorAdapter.mark(continueLabel);
                            break;
                    }
                    if (clazz.isArray()) {
                        Class<?> componentType = clazz.getComponentType();
                        Label endLabel = generatorAdapter.newLabel();
                        Label stepLabel = generatorAdapter.newLabel();
                        int arrayLocal = 0;
                        switch (methodName) {
                            case "serialize":
                                generatorAdapter.loadArg(1);
                                // Stack: array
                                generatorAdapter.arrayLength();
                                // Stack: length of array
                                generatorAdapter.dup();
                                // Stack: length of array, length of array
                                generatorAdapter.loadArg(0);
                                // Stack: length of array, length of array, buffer
                                generatorAdapter.swap(Type.INT_TYPE, Type.getType(ByteBuffer.class));
                                // Stack: length of array, buffer, length of array
                                generateSerialize.accept(int.class);
                                // Stack: length of array
                                break;
                            case "deserialize":
                                arrayLocal = generatorAdapter.newLocal(Type.getType(clazz));
                                generatorAdapter.loadArg(0);
                                // Stack: buffer
                                generateDeserialize.accept(int.class);
                                // Stack: length of array
                                generatorAdapter.dup();
                                // Stack: length of array, length of array
                                generatorAdapter.newArray(Type.getType(componentType));
                                // Stack: length of array, array
                                generatorAdapter.storeLocal(arrayLocal);
                                break;
                        }
                        // Stack: length of array
                        generatorAdapter.visitLdcInsn(0);
                        // Stack: length of array, i (now 0)
                        generatorAdapter.mark(stepLabel);
                        generatorAdapter.dup2();
                        // Stack: length of array, i, length of array, i
                        generatorAdapter.ifICmp(GeneratorAdapter.LE, endLabel);
                        // Stack: length of array, i
                        switch (methodName) {
                            case "serialize":
                                // Stack: length of array, i
                                generatorAdapter.dup();
                                // Stack: length of array, i, i
                                generatorAdapter.loadArg(1);
                                // Stack: length of array, i, i, array
                                generatorAdapter.swap(Type.INT_TYPE, Type.getType(clazz));
                                // Stack: length of array, i, array, i
                                generatorAdapter.arrayLoad(Type.getType(componentType));
                                // Stack: length of array, i, value
                                generatorAdapter.loadArg(0);
                                // Stack: length of array, i, value, buffer
                                generatorAdapter.swap(Type.getType(componentType), Type.getType(ByteBuffer.class));
                                // Stack: length of array, i, buffer, value
                                generateSerialize.accept(componentType);
                                // Stack: length of array, i
                                break;
                            case "deserialize":
                                // Stack: length of array, i
                                generatorAdapter.dup();
                                // Stack: length of array, i, i
                                generatorAdapter.loadLocal(arrayLocal);
                                // Stack: length of array, i, i, array
                                generatorAdapter.swap(Type.INT_TYPE, Type.getType(clazz));
                                // Stack: length of array, i, array, i
                                generatorAdapter.loadArg(0);
                                // Stack: length of array, i, array, i, buffer
                                generateDeserialize.accept(componentType);
                                // Stack: length of array, i, array, i, value
                                generatorAdapter.arrayStore(Type.getType(componentType));
                                // Stack: length of array, i
                                break;
                        }
                        // Stack: length of array, i
                        generatorAdapter.visitLdcInsn(1);
                        // Stack: length of array, i, 1
                        generatorAdapter.math(GeneratorAdapter.ADD, Type.INT_TYPE);
                        // Stack: length of array, i
                        generatorAdapter.goTo(stepLabel);
                        generatorAdapter.mark(endLabel);
                        generatorAdapter.pop2();
                        if (methodName.equals("deserialize")) {
                            generatorAdapter.loadLocal(arrayLocal);
                            // Stack: array
                        }
                    } else {
                        if (methodName.equals("deserialize")) {
                            generatorAdapter.newInstance(Type.getType(clazz));
                            generatorAdapter.dup();
                            generatorAdapter.invokeConstructor(
                                    Type.getType(clazz),
                                    new Method(
                                            "<init>",
                                            Type.VOID_TYPE,
                                            new Type[0]
                                    )
                            );
                        }
                        Stream.concat(Stream.of(clazz.getDeclaredFields()), Stream.of(clazz.getFields()))
                                .distinct()
                                .filter(field -> !Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
                                .forEach(field -> {
                                    Class<?> fieldType = field.getType();
                                    switch (methodName) {
                                        case "serialize":
                                            generatorAdapter.loadArg(0);
                                            generatorAdapter.loadArg(1);
                                            generatorAdapter.getField(Type.getType(clazz), field.getName(), Type.getType(fieldType));
                                            generateSerialize.accept(fieldType);
                                            break;
                                        case "deserialize":
                                            generatorAdapter.dup();
                                            generatorAdapter.loadArg(0);
                                            generateDeserialize.accept(fieldType);
                                            generatorAdapter.putField(Type.getType(clazz), field.getName(), Type.getType(fieldType));
                                            break;
                                    }
                                });
                    }
                    generatorAdapter.mark(returnLabel);
                    generatorAdapter.returnValue();
                    generatorAdapter.endMethod();
                });
                classWriter.visitEnd();
                byte[] bytes = classWriter.toByteArray();
                if (false) {
                    try {
                        Files.write(new File(clazz.getSimpleName() + ".class").toPath(), bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Generated: " + name);
                return defineClass(name, bytes, 0, bytes.length);
            }
        }
        return super.findClass(name);
    }
}
