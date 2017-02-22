#include <string.h>
#include <stdlib.h>
#include <errno.h>
#define FUSE_USE_VERSION 30
#include <fuse_lowlevel.h>
#include "common.h"
#include "generated/com_anton_fastcloud_bindings_FuseLibNative.h"

// Helpers

struct argsOpts {
    struct fuse_args fuseFuse_args;
    struct fuse_cmdline_opts fuseFuse_cmdline_opts;
};

struct fuse_session *getFuseFuse_sessionFromJniFuse_session(JNIEnv *jniEnv, jobject jniFuse_session) {
    return (struct fuse_session *) (*jniEnv)->GetLongField(
            jniEnv,
            jniFuse_session,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    (*jniEnv)->FindClass(
                            jniEnv,
                            "com/anton/fastcloud/bindings/FuseLibNative$fuse_session"
                    ),
                    "pointer",
                    "J"
            )
    );
}

fuse_req_t getFuseFuse_req_tFromJniFuse_req_t(JNIEnv *jniEnv, jobject jniFuse_req_t) {
    return (fuse_req_t) (*jniEnv)->GetLongField(
            jniEnv,
            jniFuse_req_t,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    (*jniEnv)->FindClass(
                            jniEnv,
                            "com/anton/fastcloud/bindings/FuseLibNative$fuse_req_t"
                    ),
                    "pointer",
                    "J"
            )
    );
}

jobject createJniFuse_req_tFromFuseFuse_req_t(JNIEnv *jniEnv, fuse_req_t fuseFuse_req_t) {
    jclass jniFuse_req_tClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_req_t");
    return (*jniEnv)->NewObject(jniEnv, jniFuse_req_tClass, (*jniEnv)->GetMethodID(jniEnv, jniFuse_req_tClass, "<init>", "(J)V"), fuseFuse_req_t);
}

struct timespec getSystemTimespecFromJniTimespec(JNIEnv *jniEnv, jobject jniTimespec) {
    jclass jniTimespecClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/SystemNative$timespec");
    struct timespec systemTimespec = {
        .tv_sec = (*jniEnv)->GetLongField(
                jniEnv,
                jniTimespec,
                (*jniEnv)->GetFieldID(
                        jniEnv,
                        jniTimespecClass,
                        "tv_sec",
                        "J"
                )
        ),
        .tv_nsec = (*jniEnv)->GetLongField(
                jniEnv,
                jniTimespec,
                (*jniEnv)->GetFieldID(
                        jniEnv,
                        jniTimespecClass,
                        "tv_nsec",
                        "J"
                )
        )
    };
    return systemTimespec;
}

struct stat getSystemStatFromJniStat(JNIEnv *jniEnv, jobject jniStat) {
    jclass jniStatClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/SystemNative$stat");
    struct stat systemStat;
    #define set32Field(name) \
        systemStat.name = (*jniEnv)->GetIntField( \
                jniEnv, \
                jniStat, \
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniStatClass, \
                        #name, \
                        "I" \
                ) \
        );
    #define set64Field(name) \
        systemStat.name = (*jniEnv)->GetLongField( \
                jniEnv, \
                jniStat,\
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniStatClass, \
                        #name, \
                        "J" \
                ) \
        );
    #define setTSField(name) \
        systemStat.name = getSystemTimespecFromJniTimespec( \
                jniEnv, \
                (*jniEnv)->GetObjectField( \
                        jniEnv, \
                        jniStat, \
                        (*jniEnv)->GetFieldID( \
                                jniEnv, \
                                jniStatClass, \
                                #name, \
                                "Lcom/anton/fastcloud/bindings/SystemNative$timespec;" \
                        ) \
                ) \
        );
    set64Field(st_dev)
    set64Field(st_ino)
    set32Field(st_mode)
    set64Field(st_nlink)
    set32Field(st_uid)
    set32Field(st_gid)
    set64Field(st_rdev)
    set64Field(st_size)
    set64Field(st_blksize)
    set64Field(st_blocks)
    setTSField(st_atim)
    setTSField(st_mtim)
    setTSField(st_ctim)
    #undef set32Field
    #undef set64Field
    #undef setTSField
    return systemStat;
}

struct fuse_entry_param *getFuseFuse_entry_paramFromJniFuse_entry_param(JNIEnv *jniEnv, jobject jniFuse_entry_param) {
    jclass jniFuse_entry_paramClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_entry_param");
    struct fuse_entry_param *fuseFuse_entry_param = malloc(sizeof(struct fuse_entry_param));
    fuseFuse_entry_param->ino = (*jniEnv)->GetLongField(
            jniEnv,
            jniFuse_entry_param,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    jniFuse_entry_paramClass,
                    "ino",
                    "J"
            )
    );
    fuseFuse_entry_param->generation = (*jniEnv)->GetLongField(
            jniEnv,
            jniFuse_entry_param,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    jniFuse_entry_paramClass,
                    "generation",
                    "J"
            )
    );
    fuseFuse_entry_param->attr = getSystemStatFromJniStat(
            jniEnv,
            (*jniEnv)->GetObjectField(
                    jniEnv,
                    jniFuse_entry_param,
                    (*jniEnv)->GetFieldID(
                            jniEnv,
                            jniFuse_entry_paramClass,
                            "attr",
                            "Lcom/anton/fastcloud/bindings/SystemNative$stat;"
                    )
            )
    );
    fuseFuse_entry_param->attr_timeout = (*jniEnv)->GetDoubleField(
            jniEnv,
            jniFuse_entry_param,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    jniFuse_entry_paramClass,
                    "attr_timeout",
                    "D"
            )
    );
    fuseFuse_entry_param->entry_timeout = (*jniEnv)->GetDoubleField(
            jniEnv,
            jniFuse_entry_param,
            (*jniEnv)->GetFieldID(
                    jniEnv,
                    jniFuse_entry_paramClass,
                    "entry_timeout",
                    "D"
            )
    );
    return fuseFuse_entry_param;
}

struct fuse_file_info *getFuseFuse_file_infoFromJniFuse_file_info(JNIEnv *jniEnv, jobject jniFuse_file_info) {
    jclass jniFuse_file_infoClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_file_info");
    struct fuse_file_info *fuseFuse_file_info = malloc(sizeof(struct fuse_file_info));
    #define set01Field(name) \
        fuseFuse_file_info->name = (*jniEnv)->GetBooleanField( \
                jniEnv, \
                jniFuse_file_info, \
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniFuse_file_infoClass, \
                        #name, \
                        "Z" \
                ) \
        );
    #define set32Field(name) \
        fuseFuse_file_info->name = (*jniEnv)->GetIntField( \
                jniEnv, \
                jniFuse_file_info, \
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniFuse_file_infoClass, \
                        #name, \
                        "I" \
                ) \
        );
    #define set64Field(name) \
        fuseFuse_file_info->name = (*jniEnv)->GetLongField( \
                jniEnv, \
                jniFuse_file_info,\
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniFuse_file_infoClass, \
                        #name, \
                        "J" \
                ) \
        );
    set32Field(flags)
    set01Field(writepage)
    set01Field(direct_io)
    set01Field(keep_cache)
    set01Field(flush)
    set01Field(nonseekable)
    set01Field(flock_release)
    set64Field(fh)
    set64Field(lock_owner)
    set32Field(poll_events)
    #undef set01Field
    #undef set32Field
    #undef set64Field
    return fuseFuse_file_info;
}

jobject createJniFuse_file_infoFromFuseFuse_file_info(JNIEnv *jniEnv, struct fuse_file_info *fuseFuse_file_info) {
    jclass jniFuse_file_infoClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_file_info");
    return (*jniEnv)->NewObject(
            jniEnv,
            jniFuse_file_infoClass,
            (*jniEnv)->GetMethodID(
                    jniEnv,
                    jniFuse_file_infoClass,
                    "<init>",
                    "(IZZZZZZJJI)V"
            ),
            fuseFuse_file_info->flags,
            fuseFuse_file_info->writepage,
            fuseFuse_file_info->direct_io,
            fuseFuse_file_info->keep_cache,
            fuseFuse_file_info->flush,
            fuseFuse_file_info->nonseekable,
            fuseFuse_file_info->flock_release,
            fuseFuse_file_info->fh,
            fuseFuse_file_info->lock_owner,
            fuseFuse_file_info->poll_events
    );
}

struct argsOpts getArgsOptsFromJniArgs(JNIEnv *jniEnv, jobjectArray jniArgs) {
    int argc = (*jniEnv)->GetArrayLength(jniEnv, jniArgs);
    char **argv = malloc(sizeof(char *) * argc);
    for (int i = 0; i < argc; i++) {
        jstring jniArg = (*jniEnv)->GetObjectArrayElement(jniEnv, jniArgs, i);
        const char *arg = (*jniEnv)->GetStringUTFChars(jniEnv, jniArg, NULL);
        argv[i] = strdup(arg);
        (*jniEnv)->ReleaseStringUTFChars(jniEnv, jniArg, arg);
    }
    struct argsOpts argsOpts = {
        .fuseFuse_args = FUSE_ARGS_INIT(argc, argv)
    };
    fuse_parse_cmdline(&argsOpts.fuseFuse_args, &argsOpts.fuseFuse_cmdline_opts);
    return argsOpts;
}

struct opsUserdata {
    jobject jniOps;
    jobject jniUserdata;
};

// Fuse ops

#define callOpsHandler(jniEnv, jniOps, name, signature, ...) \
    (*jniEnv)->CallVoidMethod( \
            jniEnv, \
            jniOps, \
            (*jniEnv)->GetMethodID( \
                    jniEnv, \
                    (*jniEnv)->FindClass( \
                            jniEnv, \
                            "com/anton/fastcloud/bindings/FuseLibNative$fuse_lowlevel_ops" \
                    ), \
                    name, \
                    signature \
            ), \
            __VA_ARGS__ \
    );

void op_init(void *userdata, struct fuse_conn_info *conn) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) userdata;
    jclass jniFuse_conn_infoClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_conn_info");
    jobject jniFuse_conn_info = (*jniEnv)->NewObject(
            jniEnv,
            jniFuse_conn_infoClass,
            (*jniEnv)->GetMethodID(
                    jniEnv,
                    jniFuse_conn_infoClass,
                    "<init>",
                    "(IIIIIIIIII)V"
            ),
            conn->proto_major,
            conn->proto_minor,
            conn->max_write,
            conn->max_read,
            conn->max_readahead,
            conn->capable,
            conn->want,
            conn->max_background,
            conn->congestion_threshold,
            conn->time_gran
    );
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "init",
            "(Ljava/lang/Object;Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_conn_info;)V",
            opsUserdata->jniUserdata,
            jniFuse_conn_info
    );
    #define setField(name) \
        conn->name = (*jniEnv)->GetIntField( \
                jniEnv, \
                jniFuse_conn_info, \
                (*jniEnv)->GetFieldID( \
                        jniEnv, \
                        jniFuse_conn_infoClass, \
                        #name, \
                        "I" \
                ) \
        );
    setField(max_write)
    setField(max_read)
    setField(max_readahead)
    setField(capable)
    setField(want)
    setField(max_background)
    setField(congestion_threshold)
    setField(time_gran)
    #undef setField
}

void op_destroy(void *userdata) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) userdata;
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "destroy",
            "(Ljava/lang/Object;)V",
            opsUserdata->jniUserdata
    );
}

void op_lookup(fuse_req_t req, fuse_ino_t parent, const char *name) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(req);
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "lookup",
            "(Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_req_t;JLjava/lang/String;)V",
            createJniFuse_req_tFromFuseFuse_req_t(jniEnv, req),
            parent,
            (*jniEnv)->NewStringUTF(jniEnv, name)
    );
}

void op_getattr(fuse_req_t req, fuse_ino_t ino, struct fuse_file_info *fi) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(req);
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "getattr",
            "(Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_req_t;JLcom/anton/fastcloud/bindings/FuseLibNative$fuse_file_info;)V",
            createJniFuse_req_tFromFuseFuse_req_t(jniEnv, req),
            ino,
            fi == NULL ? NULL : createJniFuse_file_infoFromFuseFuse_file_info(jniEnv, fi)
    );
}

void op_open(fuse_req_t req, fuse_ino_t ino, struct fuse_file_info *fi) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(req);
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "open",
            "(Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_req_t;JLcom/anton/fastcloud/bindings/FuseLibNative$fuse_file_info;)V",
            createJniFuse_req_tFromFuseFuse_req_t(jniEnv, req),
            ino,
            fi == NULL ? NULL : createJniFuse_file_infoFromFuseFuse_file_info(jniEnv, fi)
    );
}

void op_read(fuse_req_t req, fuse_ino_t ino, size_t size, off_t off, struct fuse_file_info *fi) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(req);
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "read",
            "(Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_req_t;JJJLcom/anton/fastcloud/bindings/FuseLibNative$fuse_file_info;)V",
            createJniFuse_req_tFromFuseFuse_req_t(jniEnv, req),
            ino,
            size,
            off,
            createJniFuse_file_infoFromFuseFuse_file_info(jniEnv, fi)
    );
}

void op_readdir(fuse_req_t req, fuse_ino_t ino, size_t size, off_t off, struct fuse_file_info *fi) {
    JNIEnv *jniEnv = getJniEnv();
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(req);
    callOpsHandler(
            jniEnv,
            opsUserdata->jniOps,
            "readdir",
            "(Lcom/anton/fastcloud/bindings/FuseLibNative$fuse_req_t;JJJLcom/anton/fastcloud/bindings/FuseLibNative$fuse_file_info;)V",
            createJniFuse_req_tFromFuseFuse_req_t(jniEnv, req),
            ino,
            size,
            off,
            createJniFuse_file_infoFromFuseFuse_file_info(jniEnv, fi)
    );
}

// JNI

JNIEXPORT jobject JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1session_1new(JNIEnv *jniEnv, jclass jniClass, jobjectArray jniArgs, jobject jniOps, jobject jniUserdata) {
    struct argsOpts argsOpts = getArgsOptsFromJniArgs(jniEnv, jniArgs);
    struct opsUserdata *opsUserdata = malloc(sizeof(struct opsUserdata));
    opsUserdata->jniOps = (*jniEnv)->NewGlobalRef(jniEnv, jniOps); // TODO: destroy ref
    opsUserdata->jniUserdata = (*jniEnv)->NewGlobalRef(jniEnv, jniUserdata); // TODO: destroy ref
    struct fuse_lowlevel_ops *fuseFuse_lowlevel_ops = malloc(sizeof(struct fuse_lowlevel_ops));
    memset(fuseFuse_lowlevel_ops, 0, sizeof(struct fuse_lowlevel_ops));
    jmethodID jniHasMethodId = (*jniEnv)->GetMethodID(
            jniEnv,
            (*jniEnv)->FindClass(
                    jniEnv,
                    "com/anton/fastcloud/bindings/FuseLibNative$fuse_lowlevel_ops"
            ),
            "hasMethod",
            "(Ljava/lang/String;)Z"
    );
    #define method(name) \
        if ((*jniEnv)->CallBooleanMethod(jniEnv, jniOps, jniHasMethodId, (*jniEnv)->NewStringUTF(jniEnv, #name))) { \
                fuseFuse_lowlevel_ops->name = op_ ## name; \
        }
    method(init)
    method(destroy)
    method(lookup)
    method(getattr)
    method(open)
    method(read)
    method(readdir)
    #undef method
    struct fuse_session *fuseFuse_session = fuse_session_new(&argsOpts.fuseFuse_args, fuseFuse_lowlevel_ops, sizeof(struct fuse_lowlevel_ops), opsUserdata);
    jclass jniFuse_sessionClass = (*jniEnv)->FindClass(jniEnv, "com/anton/fastcloud/bindings/FuseLibNative$fuse_session");
    return (*jniEnv)->NewObject(jniEnv, jniFuse_sessionClass, (*jniEnv)->GetMethodID(jniEnv, jniFuse_sessionClass, "<init>", "(J)V"), fuseFuse_session);
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1set_1signal_1handlers(JNIEnv *jniEnv, jclass jniClass, jobject jniSe) {
    return fuse_set_signal_handlers(getFuseFuse_sessionFromJniFuse_session(jniEnv, jniSe));
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1session_1mount(JNIEnv *jniEnv, jclass jniClass, jobject jniSe, jobjectArray jniArgs) {
    struct argsOpts argsOpts = getArgsOptsFromJniArgs(jniEnv, jniArgs);
    return fuse_session_mount(getFuseFuse_sessionFromJniFuse_session(jniEnv, jniSe), argsOpts.fuseFuse_cmdline_opts.mountpoint);
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1session_1loop(JNIEnv *jniEnv, jclass jniClass, jobject jniSe) {
    return fuse_session_loop(getFuseFuse_sessionFromJniFuse_session(jniEnv, jniSe));
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1session_1unmount(JNIEnv *jniEnv, jclass jniClass, jobject jniSe) {
    fuse_session_unmount(getFuseFuse_sessionFromJniFuse_session(jniEnv, jniSe));
}

JNIEXPORT jobject JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1req_1userdata(JNIEnv *jniEnv, jclass jniClass, jobject jniReq) {
    struct opsUserdata *opsUserdata = (struct opsUserdata *) fuse_req_userdata(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq));
    return opsUserdata->jniUserdata;
}

JNIEXPORT jlong JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1add_1direntry(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jbyteArray jniBuf, jlong jniBufsize, jstring jniName, jobject jniStbuf, jlong jniOff) {
    char *buf = NULL;
    if (jniBuf != NULL) {
        buf = (*jniEnv)->GetByteArrayElements(jniEnv, jniBuf, NULL);
    }
    const char *name = (*jniEnv)->GetStringUTFChars(jniEnv, jniName, NULL);
    struct stat *systemStatPointer = NULL;
    if (jniStbuf != NULL) {
        struct stat systemStat = getSystemStatFromJniStat(jniEnv, jniStbuf);
        systemStatPointer = malloc(sizeof(struct stat));
        memcpy(systemStatPointer, &systemStat, sizeof(struct stat));
    }
    long result = fuse_add_direntry(
            getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq),
            buf,
            jniBufsize,
           	strdup(name),
           	systemStatPointer,
           	jniOff
    );
    if (jniBuf != NULL) {
        (*jniEnv)->ReleaseByteArrayElements(jniEnv, jniBuf, buf, 0);
    }
    if (jniStbuf != NULL) {
        free(systemStatPointer);
    }
    (*jniEnv)->ReleaseStringUTFChars(jniEnv, jniName, name);
    return result;
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1err(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jint jniErr) {
    return fuse_reply_err(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), jniErr);
}

JNIEXPORT void JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1none(JNIEnv *jniEnv, jclass jniClass, jobject jniReq) {
    fuse_reply_none(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq));
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1entry(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jobject jniE) {
    return fuse_reply_entry(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), getFuseFuse_entry_paramFromJniFuse_entry_param(jniEnv, jniE));
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1attr(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jobject jniAttr, jdouble jniAttr_timeout) {
    struct stat systemStat = getSystemStatFromJniStat(jniEnv, jniAttr);
    struct stat *systemStatPointer = malloc(sizeof(struct stat));
    memcpy(systemStatPointer, &systemStat, sizeof(struct stat));
    int result = fuse_reply_attr(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), systemStatPointer, jniAttr_timeout);
    free(systemStatPointer);
    return result;
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1open(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jobject jniFi) {
    return fuse_reply_open(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), getFuseFuse_file_infoFromJniFuse_file_info(jniEnv, jniFi));
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1buf(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jbyteArray jniBuf, jlong jniSize) {
    char *buf = NULL;
    if (jniBuf != NULL) {
        buf = (*jniEnv)->GetByteArrayElements(jniEnv, jniBuf, NULL);
    }
    int result = fuse_reply_buf(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), buf, jniSize);
    if (jniBuf != NULL) {
        (*jniEnv)->ReleaseByteArrayElements(jniEnv, jniBuf, buf, JNI_ABORT);
    }
    return result;
}

JNIEXPORT jint JNICALL Java_com_anton_fastcloud_bindings_FuseLibNative_fuse_1reply_1bufNio(JNIEnv *jniEnv, jclass jniClass, jobject jniReq, jobject jniBuf, jlong jniSize) {
    char *buf = NULL;
    if (jniBuf != NULL) {
        buf = (*jniEnv)->GetDirectBufferAddress(jniEnv, jniBuf);
    }
    int result = fuse_reply_buf(getFuseFuse_req_tFromJniFuse_req_t(jniEnv, jniReq), buf, jniSize);
    return result;
}
