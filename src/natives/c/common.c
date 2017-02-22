#include "common.h"

JNIEnv *getJniEnv() {
    JNIEnv *jniEnv;
    (*jniJavaVM)->GetEnv(jniJavaVM, (void **) &jniEnv, JNI_VERSION_1_2);
    return jniEnv;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  jniJavaVM = vm;
  onJniLoadBuffers();
  return JNI_VERSION_1_2;
}
