#include <stdlib.h>
#include <string.h>
#include "common.h"
#include "generated/com_anton_fastcloud_buffer_ContinuousBuffer.h"

#define START_BUFFER_CAPACITY 1024
#define GROWING_BUFFER_CAPACITY continuousBufferMetadata->capacity * 2

struct continuousBufferMetadata {
    unsigned char *address;
    size_t capacity;
    size_t position;
    size_t size;
};

jclass jniArrayIndexOutOfBoundsExceptionClass;

void onJniLoadBuffers() {
    JNIEnv *jniEnv = getJniEnv();
    jniArrayIndexOutOfBoundsExceptionClass = (*jniEnv)->FindClass(jniEnv, "java/lang/ArrayIndexOutOfBoundsException");
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_init(JNIEnv *jniEnv, jclass jniClass) {
    return Java_com_anton_fastcloud_buffer_ContinuousBuffer_initWithStartCapacity(jniEnv, jniClass, START_BUFFER_CAPACITY);
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_initWithStartCapacity(JNIEnv *jniEnv, jclass jniClass, jlong startSize) {
    struct continuousBufferMetadata *continuousBufferMetadata = malloc(sizeof(struct continuousBufferMetadata));
    continuousBufferMetadata->address = malloc(startSize);
    continuousBufferMetadata->capacity = startSize;
    continuousBufferMetadata->position = 0;
    continuousBufferMetadata->size = 0;
    return (jlong) continuousBufferMetadata;
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_getAddress(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    return (jlong) continuousBufferMetadata->address;
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_getCapacity(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    return (jlong) continuousBufferMetadata->capacity;
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_getPosition(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    return (jlong) continuousBufferMetadata->position;
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_getSize(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    return (jlong) continuousBufferMetadata->size;
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_rewind(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    continuousBufferMetadata->position = 0;
}

#define writeToBuffer(writeSize, writeCode) \
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer; \
    size_t endSize = continuousBufferMetadata->position + writeSize; \
    if (endSize > continuousBufferMetadata->size) { \
        continuousBufferMetadata->size = endSize; \
        if (endSize > continuousBufferMetadata->capacity) { \
            continuousBufferMetadata->capacity = GROWING_BUFFER_CAPACITY > endSize ? GROWING_BUFFER_CAPACITY : endSize; \
            unsigned char *newAddress = realloc(continuousBufferMetadata->address, continuousBufferMetadata->capacity); \
            if (newAddress != continuousBufferMetadata->address) { \
                continuousBufferMetadata->address = newAddress; \
            } \
        } \
    } \
    unsigned char *dst = continuousBufferMetadata->address + continuousBufferMetadata->position; \
    writeCode \
    continuousBufferMetadata->position += writeSize;

#define readFromBuffer(readSize, readCode) \
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer; \
    size_t endSize = continuousBufferMetadata->position + readSize; \
    if (endSize > continuousBufferMetadata->size) { \
        char *message = malloc(4096); \
        sprintf(message, "position (%lu) + write size (%lu) > size (%lu)", continuousBufferMetadata->position, readSize, continuousBufferMetadata->size); \
        (*jniEnv)->ThrowNew(jniEnv, jniArrayIndexOutOfBoundsExceptionClass, message); \
        free(message); \
    } \
    unsigned char *src = continuousBufferMetadata->address + continuousBufferMetadata->position; \
    readCode \
    continuousBufferMetadata->position += readSize;

#define primitiveBufferMethods(upperType, jniType) \
    JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_write ## upperType(JNIEnv *jniEnv, jclass jniClass, jlong pointer, jniType value) { \
        writeToBuffer( \
                sizeof(jniType), \
                memcpy(dst, &value, sizeof(jniType)); \
        ) \
    } \
    JNIEXPORT jniType JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_read ## upperType(JNIEnv *jniEnv, jclass jniClass, jlong pointer) { \
        jniType value = 0; \
        readFromBuffer( \
                sizeof(jniType), \
                memcpy(&value, src, sizeof(jniType)); \
        ) \
        return value; \
    } \
    JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_write ## upperType ## Array(JNIEnv *jniEnv, jclass jniClass, jlong pointer, jniType ## Array jniArray) { \
        jint writeSize = (*jniEnv)->GetArrayLength(jniEnv, jniArray) * sizeof(jniType); \
        jniType *array = (*jniEnv)->GetPrimitiveArrayCritical(jniEnv, jniArray, NULL); \
        writeToBuffer( \
                writeSize, \
                memcpy(dst, array, writeSize); \
        ) \
        (*jniEnv)->ReleasePrimitiveArrayCritical(jniEnv, jniArray, array, JNI_ABORT); \
    } \
    JNIEXPORT jniType ## Array JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_read ## upperType ## Array(JNIEnv *jniEnv, jclass jniClass, jlong pointer, jint arraySize) { \
        jint readSize = arraySize * sizeof(jniType); \
        jniType ## Array jniArray = (*jniEnv)->New ## upperType ## Array(jniEnv, arraySize); \
        jniType *array = (*jniEnv)->GetPrimitiveArrayCritical(jniEnv, jniArray, NULL); \
        readFromBuffer( \
                readSize, \
                memcpy(array, src, readSize); \
        ) \
        (*jniEnv)->ReleasePrimitiveArrayCritical(jniEnv, jniArray, array, 0); \
        return jniArray; \
    }

primitiveBufferMethods(Boolean, jboolean)
primitiveBufferMethods(Byte, jbyte)
primitiveBufferMethods(Short, jshort)
primitiveBufferMethods(Char, jchar)
primitiveBufferMethods(Int, jint)
primitiveBufferMethods(Float, jfloat)
primitiveBufferMethods(Long, jlong)
primitiveBufferMethods(Double, jdouble)

#undef primitiveBufferMethods

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_writeByteBuffer(JNIEnv *jniEnv, jclass jniClass, jlong pointer, jobject jniByteBuffer) {
    jint writeSize = (*jniEnv)->GetDirectBufferCapacity(jniEnv, jniByteBuffer);
    unsigned char *byteBufferAddress = (*jniEnv)->GetDirectBufferAddress(jniEnv, jniByteBuffer);
    writeToBuffer(
            writeSize,
            memcpy(dst, byteBufferAddress, writeSize);
    )
}

JNIEXPORT jobject JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_readByteBuffer(JNIEnv *jniEnv, jclass jniClass, jlong pointer, jint readSize) {
    unsigned char *byteBufferAddress = malloc(readSize);
    readFromBuffer(
            readSize,
            memcpy(byteBufferAddress, src, readSize);
    )
    return (*jniEnv)->NewDirectByteBuffer(jniEnv, byteBufferAddress, readSize);
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_free(JNIEnv *jniEnv, jclass jniClass, jlong pointer) {
    struct continuousBufferMetadata *continuousBufferMetadata = (struct continuousBufferMetadata *) pointer;
    free(continuousBufferMetadata->address);
    free(continuousBufferMetadata);
}

#undef writeToBuffer
#undef readFromBuffer
