#include <stdlib.h>
#include <mbedtls/sha256.h>
#include "com_nukkitx_natives_sha256_NativeSha256.h"

#define jlong_to_ptr(a) ((void*)(a))

JNIEXPORT jlong JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_init
        (JNIEnv *env, jobject obj) {
    mbedtls_sha256_context* ctx = (mbedtls_sha256_context*) malloc(sizeof(mbedtls_sha256_context));

    mbedtls_sha256_init(ctx);
    mbedtls_sha256_starts_ret(ctx, 0); // 0 for SHA256

    return (jlong) ctx;
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_free
        (JNIEnv *env, jobject obj, jlong ctx) {
    mbedtls_sha256_context *context = jlong_to_ptr(ctx);

    mbedtls_sha256_free(context);
    free(context);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_reset
        (JNIEnv * env, jobject obj, jlong ctx) {
    mbedtls_sha256_context *context = jlong_to_ptr(ctx);

    // Reset hash content
    mbedtls_sha256_starts_ret(context, 0); // 0 for SHA256
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_update__JJI
        (JNIEnv *env, jobject obj, jlong ctx, jlong in, jint len) {
    mbedtls_sha256_context *context =  jlong_to_ptr(ctx);

    mbedtls_sha256_update_ret(context, (unsigned char*) in, len);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_update__J_3BII
        (JNIEnv *env, jobject obj, jlong ctx, jbyteArray in, jint off, jint len) {
    mbedtls_sha256_context *context = jlong_to_ptr(ctx);
   jbyte *input = (jbyte*) (*env)->GetPrimitiveArrayCritical(env, in, JNI_FALSE);

    if (input == NULL) {
        if (len != 0 && (*env)->ExceptionOccurred(env) == NULL)
            (*env)->ThrowNew(env, (* env)->FindClass(env, "java/lang/OutOfMemoryError"), "Out of memory");
        return;
    }

    mbedtls_sha256_update_ret(context, (unsigned char*) (input + off), len);

    (*env)->ReleasePrimitiveArrayCritical(env, in, input, JNI_ABORT);
}

JNIEXPORT jbyteArray JNICALL Java_com_nukkitx_natives_sha256_NativeSha256_digest
        (JNIEnv *env, jobject obj, jlong ctx) {
    mbedtls_sha256_context *context = jlong_to_ptr(ctx);
    jbyteArray jOutput = (*env)->NewByteArray(env, 32);
    jbyte *output = (jbyte*) (*env)->GetByteArrayElements(env, jOutput, JNI_FALSE);

    mbedtls_sha256_finish_ret(context, (unsigned char*) output);

    (*env)->ReleaseByteArrayElements(env, jOutput, output, JNI_OK);

    return jOutput;
}
