#include <stdlib.h>
#include <libdeflate.h>
#include "zlib_processor.h"

#define jlong_to_ptr(a) ((void*)(a))

struct zlib_context {
    struct libdeflate_compressor *compressor[13];
    struct libdeflate_decompressor *decompressor;
    size_t (*compress_ptr)(struct libdeflate_compressor *compressor, const void *in, size_t in_nbytes, void *out, size_t out_nbytes_avail);
    enum libdeflate_result (*decompress_ptr)(struct libdeflate_decompressor *decompressor, const void *in, size_t in_nbytes, void *out, size_t out_nbytes_avail, size_t *actual_in_nbytes_ret, size_t *actual_out_nbytes_ret);
};

void throwException(JNIEnv *env, const char *type, const char *msg) {
    // We don't cache these, since they will only occur rarely.
    jclass class = (*env)->FindClass(env, type);

    if (class != 0) {
        (*env)->ThrowNew(env, class, msg);
    }
}

JNIEXPORT jlong JNICALL Java_org_cloudburstmc_natives_zlib_LibdeflateZlibProcessor_init
  (JNIEnv *env, jclass class, jboolean nowrap) {
    struct zlib_context *context = malloc(sizeof(struct zlib_context));
    for (int level = 0; level < 13; level++) {
        context->compressor[level] = libdeflate_alloc_compressor(level);
    }
    context->decompressor = libdeflate_alloc_decompressor();

    if (nowrap) {
        context->compress_ptr = libdeflate_deflate_compress;
        context->decompress_ptr = libdeflate_deflate_decompress_ex;
    } else {
        context->compress_ptr = libdeflate_zlib_compress;
        context->decompress_ptr = libdeflate_zlib_decompress_ex;
    }

    return (jlong) context;
}

JNIEXPORT void JNICALL Java_org_cloudburstmc_natives_zlib_LibdeflateZlibProcessor_free
        (JNIEnv *env, jclass class, jlong ctx) {
    struct zlib_context *context = jlong_to_ptr(ctx);

    for (int level = 0; level < 13; level++) {
        libdeflate_free_compressor(context->compressor[level]);
    }
    libdeflate_free_decompressor(context->decompressor);
    free(context);
}

JNIEXPORT jint JNICALL Java_org_cloudburstmc_natives_zlib_LibdeflateZlibProcessor_deflate
        (JNIEnv *env, jclass class, jlong ctx, jlong in_addrs, jint in_len, jlong out_addrs, jint out_len, jint level) {
    struct zlib_context *context = jlong_to_ptr(ctx);

    return (jint) context->compress_ptr(context->compressor[level], (void *) in_addrs, in_len, (void *) out_addrs, out_len);
}

JNIEXPORT jint JNICALL Java_org_cloudburstmc_natives_zlib_LibdeflateZlibProcessor_inflate
        (JNIEnv *env, jclass class, jlong ctx, jlong in_addrs, jint in_len, jlong out_addrs, jint out_len) {
    struct zlib_context *context = jlong_to_ptr(ctx);

    size_t bytes_read = 0;
    size_t bytes_written = 0;

    enum libdeflate_result result = context->decompress_ptr(context->decompressor, (void *) in_addrs, in_len, (void *) out_addrs, out_len, &bytes_read, &bytes_written);

    switch (result) {
        case LIBDEFLATE_SUCCESS:
            return (jint) bytes_written;
        case LIBDEFLATE_BAD_DATA:
            throwException(env, "java/util/zip/DataFormatException", "input data is corrupted");
            return 0;
        case LIBDEFLATE_SHORT_OUTPUT:
            throwException(env, "java/util/zip/DataFormatException", "decompressed data is shorter than expected size");
            return 0;
        case LIBDEFLATE_INSUFFICIENT_SPACE:
            return -1; // TODO: Fall back to Cloudflare Zlib
        default:
            throwException(env, "java/util/zip/DataFormatException", "unknown libdeflate error");
            return 0;
    }
}