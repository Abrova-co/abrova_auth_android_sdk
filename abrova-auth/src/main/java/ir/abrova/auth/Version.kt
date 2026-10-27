package ir.abrova.auth

/**
 * The SDK version. Keep it equal to VERSION_NAME in gradle.properties (and the fallback in
 * abrova-auth/build.gradle.kts) — a unit test checks it.
 */
internal const val SDK_VERSION = "0.1.1"

/** Sent as `X-Abrova-SDK` with every request, so Abrova can show which SDK versions are in use. */
internal const val SDK_HEADER = "auth-android/$SDK_VERSION"
