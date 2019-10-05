#include <stdlib.h>
#include <mbedtls/aes.h>
#include "com_nukkitx_natives_aes_NativeAes.h"

#define jlong_to_ptr(a) ((void*)(a))

struct aes_cipher_context {
    int mode;
    mbedtls_aes_context cipher;
    unsigned char *iv;
};

JNIEXPORT jlong JNICALL Java_com_nukkitx_natives_aes_NativeAes_init
        (JNIEnv * env, jclass class, jboolean encryption, jbyteArray key, jbyteArray iv) {

    jsize keyLength = (*env)->GetArrayLength(env, key);
    jbyte *keyBytes = (*env)->GetByteArrayElements(env, key, JNI_FALSE);
    jbyte *ivBytes = (*env)->GetByteArrayElements(env, iv, JNI_FALSE);

    struct aes_cipher_context *context = malloc(sizeof(struct aes_cipher_context));
    context->iv = (unsigned char*) ivBytes;
    context->mode = encryption ? JNI_TRUE : JNI_FALSE;

    mbedtls_aes_init(&context->cipher);

    mbedtls_aes_setkey_enc(&context->cipher, (unsigned char*) keyBytes, keyLength * 8);

    (*env)->ReleaseByteArrayElements(env, key, keyBytes, JNI_ABORT); // Don't copy back contents
    (*env)->ReleaseByteArrayElements(env, key, ivBytes, JNI_ABORT);

    return (jlong) context;
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_aes_NativeAes_free
(JNIEnv * env, jclass class, jlong ctx) {
    struct aes_cipher_context *context = jlong_to_ptr(ctx);

    mbedtls_aes_free(&context->cipher);
    free(context->iv);
    free(context);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_aes_NativeAes_cipherBytesToBytes
(JNIEnv * env, jobject obj, jlong ctx, jbyteArray inArr, jint inOff, jbyteArray outArr, jint outOff, jint len) {
    struct aes_cipher_context *context = jlong_to_ptr(ctx);

    unsigned char* input = (*env)->GetPrimitiveArrayCritical(env, inArr, JNI_FALSE);
    unsigned char* output = (*env)->GetPrimitiveArrayCritical(env, outArr, JNI_FALSE);

    mbedtls_aes_crypt_cfb8(&context->cipher, context->mode, len, context->iv, input + inOff, output + outOff);

    (*env)->ReleasePrimitiveArrayCritical(env, inArr, input, JNI_ABORT);
    (*env)->ReleasePrimitiveArrayCritical(env, outArr, output, JNI_OK);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_aes_NativeAes_cipherBytesToBuffer
(JNIEnv * env, jobject obj, jlong ctx, jbyteArray inArr, jint inOff, jlong outAddress, jint len) {
    struct aes_cipher_context *context = jlong_to_ptr(ctx);

    unsigned char* input = (*env)->GetPrimitiveArrayCritical(env, inArr, JNI_FALSE);

    mbedtls_aes_crypt_cfb8(&context->cipher, context->mode, len, context->iv, input + inOff,
            (unsigned char*) outAddress);

    (*env)->ReleasePrimitiveArrayCritical(env, inArr, input, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_aes_NativeAes_cipherBufferToBytes
(JNIEnv * env, jobject obj, jlong ctx, jlong inAddr, jbyteArray outArr, jint outOff, jint len) {
    struct aes_cipher_context *context = jlong_to_ptr(ctx);

    unsigned char* output = (*env)->GetPrimitiveArrayCritical(env, outArr, NULL);

    mbedtls_aes_crypt_cfb8(&context->cipher, context->mode, len, context->iv, (unsigned char*) inAddr, output + outOff);

    (*env)->ReleasePrimitiveArrayCritical(env, outArr, output, JNI_OK);
}

JNIEXPORT void JNICALL Java_com_nukkitx_natives_aes_NativeAes_cipherBufferToBuffer
(JNIEnv * env, jobject obj, jlong ctx, jlong inAddr, jlong outAddr, jint len) {
    struct aes_cipher_context *context = jlong_to_ptr(ctx);

    mbedtls_aes_crypt_cfb8(&context->cipher, context->mode, len, context->iv, (unsigned char*) inAddr,
            (unsigned char*) outAddr);
}