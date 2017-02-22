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

#define writeToBuffer(writeSize, writeCode) \
    unsigned char *address = getAddress(jniEnv, jniContinuousBuffer); \
    size_t capacity = getCapacity(jniEnv, jniContinuousBuffer); \
    size_t position = getPosition(jniEnv, jniContinuousBuffer); \
    size_t size = getSize(jniEnv, jniContinuousBuffer); \
    size_t endSize = position + writeSize; \
    if (endSize > size) { \
        size = endSize; \
        if (endSize > capacity) { \
            capacity = GROWING_BUFFER_CAPACITY > endSize ? GROWING_BUFFER_CAPACITY : endSize; \
            setCapacity(jniEnv, jniContinuousBuffer, capacity); \
            unsigned char *newAddress = realloc(address, capacity); \
            if (newAddress != address) { \
                address = newAddress; \
                setAddress(jniEnv, jniContinuousBuffer, address); \
            } \
        } \
        setSize(jniEnv, jniContinuousBuffer, size); \
    } \
    writeCode \
    position += writeSize; \
    setPosition(jniEnv, jniContinuousBuffer, position);

#define readFromBuffer(readSize, readCode) \
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
    readCode \
    position += readSize; \
    setPosition(jniEnv, jniContinuousBuffer, position);

#define primitiveBufferMethods(upperType, jniType) \
    JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_write ## upperType(JNIEnv *jniEnv, jobject jniContinuousBuffer, jniType value) { \
        writeToBuffer( \
                sizeof(jniType), \
                memcpy(address + position, &value, sizeof(jniType)); \
        ) \
    } \
    JNIEXPORT jniType JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_read ## upperType(JNIEnv *jniEnv, jobject jniContinuousBuffer) { \
        jniType value = 0; \
        readFromBuffer( \
                sizeof(jniType), \
                memcpy(&value, address + position, sizeof(jniType)); \
        ) \
        return value; \
    } \
    JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_write ## upperType ## Array(JNIEnv *jniEnv, jobject jniContinuousBuffer, jniType ## Array jniArray) { \
        jint writeSize = (*jniEnv)->GetArrayLength(jniEnv, jniArray) * sizeof(jniType); \
        jniType *array = (*jniEnv)->Get ## upperType ## ArrayElements(jniEnv, jniArray, NULL); \
        writeToBuffer( \
                writeSize, \
                memcpy(address + position, array, writeSize); \
        ) \
        (*jniEnv)->Release ## upperType ## ArrayElements(jniEnv, jniArray, array, JNI_ABORT); \
    } \
    JNIEXPORT jniType ## Array JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_read ## upperType ## Array(JNIEnv *jniEnv, jobject jniContinuousBuffer, jint arraySize) { \
        jint readSize = arraySize * sizeof(jniType); \
        jniType ## Array jniArray = (*jniEnv)->New ## upperType ## Array(jniEnv, arraySize); \
        jniType *array = (*jniEnv)->Get ## upperType ## ArrayElements(jniEnv, jniArray, NULL); \
        readFromBuffer( \
                readSize, \
                memcpy(array, address + position, readSize); \
        ) \
        (*jniEnv)->Release ## upperType ## ArrayElements(jniEnv, jniArray, array, 0); \
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

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_writeByteBuffer(JNIEnv *jniEnv, jobject jniContinuousBuffer, jobject jniByteBuffer) {
    jint writeSize = (*jniEnv)->GetDirectBufferCapacity(jniEnv, jniByteBuffer);
    unsigned char *byteBufferAddress = (*jniEnv)->GetDirectBufferAddress(jniEnv, jniByteBuffer);
    writeToBuffer(
            writeSize,
            memcpy(address + position, byteBufferAddress, writeSize);
    )
}

JNIEXPORT jobject JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_readByteBuffer(JNIEnv *jniEnv, jobject jniContinuousBuffer, jint readSize) {
    unsigned char *byteBufferAddress = malloc(readSize);
    writeToBuffer(
            readSize,
            memcpy(byteBufferAddress, address + position, readSize);
    )
    return (*jniEnv)->NewDirectByteBuffer(jniEnv, byteBufferAddress, readSize);
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_buffer_ContinuousBuffer_free(JNIEnv *jniEnv, jobject jniContinuousBuffer) {
    free(getAddress(jniEnv, jniContinuousBuffer));
}

#undef writeToBuffer
#undef readFromBuffer
