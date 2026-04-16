#ifndef RKNHARDERING_NATIVE_CURL_PROBE_H
#define RKNHARDERING_NATIVE_CURL_PROBE_H

#include <jni.h>

jobjectArray ExecuteNativeCurlRequest(
    JNIEnv* env,
    jstring url,
    jstring interface_name,
    jobjectArray resolve_rules,
    jint ip_resolve_mode,
    jint timeout_ms,
    jint connect_timeout_ms,
    jstring ca_bundle_path,
    jboolean debug_verbose
);

#endif

