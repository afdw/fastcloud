// Naming for funcs, args and vars in C code:
// - jniName
// - fuseType
// - name (for my things)
// Everything in lower camel case

#include <jni.h>

#ifndef FASTCLOUD_COMMON
#define FASTCLOUD_COMMON

static JavaVM *jniJavaVM;

JNIEnv *getJniEnv();

// common event subscribers

void onJniLoadBuffers();

#endif
