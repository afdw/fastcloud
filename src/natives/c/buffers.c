#include <stdlib.h>
#include <string.h>
#include "common.h"
#include "generated/com_anton_fastcloud_buffer_ContinuousBuffer.h"

#define START_BUFFER_CAPACITY 1024
#define GROWING_BUFFER_CAPACITY capacity * 2

jclass jniContinuousBufferClass;
jfieldID jniAddressFieldId;
jfieldID jniCapacityFieldId;
jfieldID jniPositionFieldId;
jfieldID jniSizeFieldId;
jclass jniArrayIndexOutOfBoundsExceptionClass;

void onJniLoadBuffers() {
    JNIEnv *env = getJniEnv();
    jniContinuousBufferClass = (*env)->FindClass(env, "com/anton/fastcloud/buffer/ContinuousBuffer");
    jniAddressFieldId = (*env)->GetFieldID(env, jniContinuousBufferClass, "address", "J");
    jniCapacityFieldId = (*env)->GetFieldID(env, jniContinuousBufferClass, "capacity", "J");
    jniPositionFieldId = (*env)->GetFieldID(env, jniContinuousBufferClass, "position", "J");
    jniSizeFieldId = (*env)->GetFieldID(env, jniContinuousBufferClass, "size", "J");
    jniArrayIndexOutOfBoundsExceptionClass = (*env)->FindClass(env, "java/lang/ArrayIndexOutOfBoundsException");
}

unsigned char *getAddress(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    return (unsigned char *) (*jniEnv)->GetLongField(jniEnv, jniContinuousBuffer, jniAddressFieldId);
}

void setAddress(JNIEnv *jniEnv, jobject jniContinuousBuffer, unsigned char *new) {
    (*jniEnv)->SetLongField(jniEnv, jniContinuousBuffer, jniAddressFieldId, (long) new);
}

size_t getCapacity(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    return (size_t) (*jniEnv)->GetLongField(jniEnv, jniContinuousBuffer, jniCapacityFieldId);
}

void setCapacity(JNIEnv *jniEnv, jobject jniContinuousBuffer, size_t new) {
    (*jniEnv)->SetLongField(jniEnv, jniContinuousBuffer, jniCapacityFieldId, (long) new);
}

size_t getPosition(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    return (size_t) (*jniEnv)->GetLongField(jniEnv, jniContinuousBuffer, jniPositionFieldId);
}

void setPosition(JNIEnv *jniEnv, jobject jniContinuousBuffer, size_t new) {
    (*jniEnv)->SetLongField(jniEnv, jniContinuousBuffer, jniPositionFieldId, (long) new);
}

size_t getSize(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    return (size_t) (*jniEnv)->GetLongField(jniEnv, jniContinuousBuffer, jniSizeFieldId);
}

void setSize(JNIEnv *jniEnv, jobject jniContinuousBuffer, size_t new) {
    (*jniEnv)->SetLongField(jniEnv, jniContinuousBuffer, jniSizeFieldId, (long) new);
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_init(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    size_t capacity = START_BUFFER_CAPACITY;
    setAddress(jniEnv, jniContinuousBuffer, malloc(capacity));
    setCapacity(jniEnv, jniContinuousBuffer, capacity);
    setPosition(jniEnv, jniContinuousBuffer, 0);
    setSize(jniEnv, jniContinuousBuffer, 0);
}

#define writeToBuffer(readSize, readCode) \
    unsigned char *address = getAddress(jniEnv, jniContinuousBuffer); \
    size_t capacity = getCapacity(jniEnv, jniContinuousBuffer); \
    size_t position = getPosition(jniEnv, jniContinuousBuffer); \
    size_t size = getSize(jniEnv, jniContinuousBuffer); \
    size_t endSize = position + readSize; \
    if (endSize > size) { \
        size = endSize; \
        if (endSize > capacity) { \
            capacity = GROWING_BUFFER_CAPACITY > endSize ? GROWING_BUFFER_CAPACITY : endSize; \
            setCapacity(jniEnv, jniContinuousBuffer, capacity); \
            address = realloc(address, capacity); \
            setAddress(jniEnv, jniContinuousBuffer, address); \
        } \
        setSize(jniEnv, jniContinuousBuffer, size); \
    } \
    readCode \
    setPosition(jniEnv, jniContinuousBuffer, position);

#define readFromBuffer(readSize, writeCode) \
    unsigned char *address = getAddress(jniEnv, jniContinuousBuffer); \
    size_t position = getPosition(jniEnv, jniContinuousBuffer); \
    size_t size = getSize(jniEnv, jniContinuousBuffer); \
    size_t endSize = position + readSize; \
    if (endSize > size) { \
        char *message = malloc(4096); \
        sprintf(message, "position (%lu) + write size (%lu) > size (%lu)", position, readSize, size); \
        (*jniEnv)->ThrowNew(jniEnv, jniArrayIndexOutOfBoundsExceptionClass, message); \
        free(message); \
    } \
    writeCode \
    setPosition(jniEnv, jniContinuousBuffer, position);

#define primitiveBufferMethods(upperType, jniType) \
    JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_write ## upperType(JNIEnv *jniEnv, jobject jniContinuousBuffer, jniType value) { \
        writeToBuffer( \
                sizeof(jniType), \
                for (unsigned char i = 0; i < sizeof(jniType); i++) { address[position++] = value >> (i * 8); } \
        ) \
    } \
    JNIEXPORT jniType JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_read ## upperType(JNIEnv *jniEnv, jobject jniContinuousBuffer) { \
        jniType value = 0; \
        readFromBuffer( \
                sizeof(jniType), \
                for (unsigned char i = 0; i < sizeof(jniType); i++) { value |= (jniType) address[position++] << (i * 8); } \
        ) \
        return value; \
    } \

primitiveBufferMethods(Boolean, jboolean)
primitiveBufferMethods(Byte, jbyte)
primitiveBufferMethods(Short, jshort)
primitiveBufferMethods(Char, jchar)
primitiveBufferMethods(Int, jint)
primitiveBufferMethods(Long, jlong)

#undef primitiveBufferMethods

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_writeByteBuffer(JNIEnv *jniEnv, jobject jniContinuousBuffer, jobject jniByteBuffer) {
    int writeSize = (*jniEnv)->GetDirectBufferCapacity(jniEnv, jniByteBuffer);
    unsigned char *byteBufferAddress = (*jniEnv)->GetDirectBufferAddress(jniEnv, jniByteBuffer);
    writeToBuffer(
            writeSize,
            memcpy(address + position, byteBufferAddress, writeSize); \
            position += writeSize;
    )
}

JNIEXPORT jobject JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_readByteBuffer(JNIEnv *jniEnv, jobject jniContinuousBuffer, jint readSize) {
    unsigned char *byteBufferAddress = malloc(readSize);
    writeToBuffer(
            readSize,
            memcpy(byteBufferAddress, address + position, readSize); \
            position += readSize;
    )
    return (*jniEnv)->NewDirectByteBuffer(jniEnv, byteBufferAddress, readSize);
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_free(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    free(getAddress(jniEnv, jniContinuousBuffer));
}

#undef writeToBuffer
#undef readFromBuffer
