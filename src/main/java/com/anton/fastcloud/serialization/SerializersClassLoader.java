package com.anton.fastcloud.serialization;


import com.anton.fastcloud.data.DataObject;
import com.anton.fastcloud.serializers.java_lang_String;
import com.anton.fastcloud.serializers.java_util_UUID;
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
import java.util.UUID;
import java.util.stream.Stream;

public class SerializersClassLoader extends URLClassLoader {
    private static final SerializersClassLoader instance = new SerializersClassLoader();

    private static final String PREFIX = "com.anton.fastcloud.serializers.";

    private static final String INSTANCE_FIELD = "instance";

    private static final Map<Class<?>, String> METHOD_NAMES = new HashMap<>();

    static {
        METHOD_NAMES.put(boolean.class, "");
        METHOD_NAMES.put(byte.class, "");
        METHOD_NAMES.put(char.class, "Char");
        METHOD_NAMES.put(short.class, "Short");
        METHOD_NAMES.put(int.class, "Int");
        METHOD_NAMES.put(long.class, "Long");
        METHOD_NAMES.put(float.class, "Float");
        METHOD_NAMES.put(double.class, "Double");
    }

    private static final boolean DEBUG = false;

    private final Map<Class, ISerializer<?>> serializers = new HashMap<>();

    private SerializersClassLoader() {
        super(new URL[0]);
        serializers.put(UUID.class, java_util_UUID.instance);
        serializers.put(String.class, java_lang_String.instance);
    }

    private static String getSerializerNameFromClass(Class<?> clazz) {
        return PREFIX + clazz.getName()
                .replaceAll("\\.", "_")
                .replaceAll("\\[", "LeftSquareBracket")
                .replaceAll(";", "Semicolon");
    }

    private static Class<?> getClassFromSerializerName(String serializerName) {
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

    private static void generateSerialize(String methodName, GeneratorAdapter generatorAdapter, Class<?> type) {
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
                            "put" + METHOD_NAMES.get(type),
                            Type.getType(ByteBuffer.class),
                            new Type[] {Type.getType(byteBufferFieldType)}
                    )
            );
            generatorAdapter.pop();
        } else {
            generatorAdapter.getStatic(
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                    INSTANCE_FIELD,
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/"))
            );
            generatorAdapter.dupX2();
            generatorAdapter.pop();
            generatorAdapter.invokeVirtual(
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                    new Method(
                            methodName,
                            Type.VOID_TYPE,
                            new Type[] {Type.getType(ByteBuffer.class), Type.getType(Object.class)}
                    )
            );
        }
    }

    private static void generateDeserialize(String methodName, GeneratorAdapter generatorAdapter, Class<?> type) {
        Class<?> byteBufferFieldType = type;
        if (type == boolean.class) {
            byteBufferFieldType = byte.class;
        }
        if (type.isPrimitive()) {
            generatorAdapter.invokeVirtual(
                    Type.getType(ByteBuffer.class),
                    new Method(
                            "get" + METHOD_NAMES.get(type),
                            Type.getType(byteBufferFieldType),
                            new Type[0]
                    )
            );
            if (type == boolean.class) {
                generatorAdapter.cast(Type.BYTE_TYPE, Type.BOOLEAN_TYPE);
            }
        } else {
            generatorAdapter.getStatic(
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                    INSTANCE_FIELD,
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/"))
            );
            generatorAdapter.swap();
            generatorAdapter.invokeVirtual(
                    Type.getObjectType(getSerializerNameFromClass(type).replaceAll("\\.", "/")),
                    new Method(
                            methodName,
                            Type.getType(Object.class),
                            new Type[] {Type.getType(ByteBuffer.class)}
                    )
            );
        }
    }

    private static void generateMethodBody(Class<?> clazz, String methodName, GeneratorAdapter generatorAdapter) {
        Label returnLabel = generatorAdapter.newLabel();
        Label continueLabel = generatorAdapter.newLabel();
        switch (methodName) {
            case "serialize":
                generatorAdapter.loadArg(0);
                generatorAdapter.loadArg(1);
                generatorAdapter.ifNonNull(continueLabel);
                generatorAdapter.visitLdcInsn(0);
                generateSerialize(methodName, generatorAdapter, boolean.class);
                generatorAdapter.goTo(returnLabel);
                generatorAdapter.mark(continueLabel);
                generatorAdapter.visitLdcInsn(1);
                generateSerialize(methodName, generatorAdapter, boolean.class);
                break;
            case "deserialize":
                generatorAdapter.loadArg(0);
                generateDeserialize(methodName, generatorAdapter, boolean.class);
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
                    generatorAdapter.checkCast(Type.getType(clazz));
                    // Stack: array
                    generatorAdapter.arrayLength();
                    // Stack: length of array
                    generatorAdapter.dup();
                    // Stack: length of array, length of array
                    generatorAdapter.loadArg(0);
                    // Stack: length of array, length of array, buffer
                    generatorAdapter.swap(Type.INT_TYPE, Type.getType(ByteBuffer.class));
                    // Stack: length of array, buffer, length of array
                    generateSerialize(methodName, generatorAdapter, int.class);
                    // Stack: length of array
                    break;
                case "deserialize":
                    arrayLocal = generatorAdapter.newLocal(Type.getType(clazz));
                    generatorAdapter.loadArg(0);
                    // Stack: buffer
                    generateDeserialize(methodName, generatorAdapter, int.class);
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
                    generateSerialize(methodName, generatorAdapter, componentType);
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
                    generateDeserialize(methodName, generatorAdapter, componentType);
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
                    .filter(field ->
                            !Modifier.isStatic(field.getModifiers()) &&
                                    Modifier.isPublic(field.getModifiers()) &&
                                    !Modifier.isTransient(field.getModifiers())
                    )
                    .forEach(field -> {
                        Class<?> fieldType = field.getType();
                        switch (methodName) {
                            case "serialize":
                                generatorAdapter.loadArg(0);
                                generatorAdapter.loadArg(1);
                                generatorAdapter.checkCast(Type.getType(clazz));
                                generatorAdapter.getField(Type.getType(clazz), field.getName(), Type.getType(fieldType));
                                generateSerialize(methodName, generatorAdapter, fieldType);
                                break;
                            case "deserialize":
                                generatorAdapter.dup();
                                generatorAdapter.loadArg(0);
                                generateDeserialize(methodName, generatorAdapter, fieldType);
                                generatorAdapter.putField(Type.getType(clazz), field.getName(), Type.getType(fieldType));
                                break;
                        }
                    });
        }
        generatorAdapter.mark(returnLabel);
    }

    private void generateSerializerClass(String name, Class<?> clazz, ClassWriter classWriter) {
        classWriter.visit(
                Opcodes.V1_7,
                Opcodes.ACC_PUBLIC,
                name.replaceAll("\\.", "/"),
                null,
                "sun/reflect/MagicAccessorImpl",
                new String[] {ISerializer.class.getName().replaceAll("\\.", "/")}
        );
        classWriter.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                INSTANCE_FIELD,
                "L" + name.replaceAll("\\.", "/") + ";",
                null,
                null
        );
        classWriter.visitEnd();
        {
            GeneratorAdapter generatorAdapter = new GeneratorAdapter(
                    Opcodes.ACC_STATIC,
                    new Method(
                            "<clinit>",
                            Type.VOID_TYPE,
                            new Type[] {}
                    ),
                    null,
                    null,
                    classWriter
            );
            generatorAdapter.newInstance(Type.getObjectType(name.replaceAll("\\.", "/")));
            generatorAdapter.dup();
            generatorAdapter.invokeConstructor(
                    Type.getObjectType(name.replaceAll("\\.", "/")),
                    new Method(
                            "<init>",
                            Type.VOID_TYPE,
                            new Type[] {}
                    )
            );
            generatorAdapter.putStatic(
                    Type.getObjectType(name.replaceAll("\\.", "/")),
                    INSTANCE_FIELD,
                    Type.getObjectType(name.replaceAll("\\.", "/"))
            );
            generatorAdapter.returnValue();
            generatorAdapter.endMethod();
        }
        {
            GeneratorAdapter generatorAdapter = new GeneratorAdapter(
                    Opcodes.ACC_PRIVATE,
                    new Method(
                            "<init>",
                            Type.VOID_TYPE,
                            new Type[] {}
                    ),
                    null,
                    null,
                    classWriter
            );
            generatorAdapter.loadThis();
            generatorAdapter.invokeConstructor(
                    Type.getType(Object.class),
                    new Method(
                            "<init>",
                            Type.VOID_TYPE,
                            new Type[] {}
                    )
            );
            generatorAdapter.returnValue();
            generatorAdapter.endMethod();
        }
        Stream.of("serialize", "deserialize").forEach(methodName -> {
            GeneratorAdapter generatorAdapter;
            switch (methodName) {
                case "serialize":
                    generatorAdapter = new GeneratorAdapter(
                            Opcodes.ACC_PUBLIC,
                            new Method(
                                    methodName,
                                    Type.VOID_TYPE,
                                    new Type[] {Type.getType(ByteBuffer.class), Type.getType(Object.class)}
                            ),
                            null,
                            null,
                            classWriter
                    );
                    break;
                case "deserialize":
                    generatorAdapter = new GeneratorAdapter(
                            Opcodes.ACC_PUBLIC,
                            new Method(
                                    methodName,
                                    Type.getType(Object.class),
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
            generateMethodBody(clazz, methodName, generatorAdapter);
            generatorAdapter.returnValue();
            generatorAdapter.endMethod();
        });
        classWriter.visitEnd();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(PREFIX)) {
            Class<?> clazz = getClassFromSerializerName(name);
            if (clazz != null && (DataObject.class.isAssignableFrom(clazz) || clazz.isArray())) {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                generateSerializerClass(name, clazz, classWriter);
                byte[] bytes = classWriter.toByteArray();
                if (DEBUG) {
                    try {
                        Files.write(new File(clazz.getSimpleName() + ".class").toPath(), bytes);
                        System.out.println("Generated: " + name);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return defineClass(name, bytes, 0, bytes.length);
            }
        }
        return super.findClass(name);
    }

    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public static <T> ISerializer<T> getSerializer(Class<T> clazz) {
        ISerializer<T> serializer = (ISerializer<T>) SerializersClassLoader.instance.serializers.get(clazz);
        if (serializer == null) {
            try {
                Class<ISerializer<T>> generatedClass = (Class<ISerializer<T>>) SerializersClassLoader.instance.loadClass(
                        SerializersClassLoader.getSerializerNameFromClass(clazz)
                );
                serializer = (ISerializer<T>) generatedClass.getField(INSTANCE_FIELD).get(null);
                SerializersClassLoader.instance.serializers.put(clazz, serializer);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return serializer;
    }
}
