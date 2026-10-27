package ir.abrova.auth

/**
 * An error from Abrova Auth.
 *
 * Branch on [code] (see [AbrovaAuthErrorCode]), never on [message]: the message
 * is an English hint for developers, the code is stable.
 *
 * ```kotlin
 * try {
 *     AbrovaAuth.shared.linkGoogle(idToken, rawNonce)
 * } catch (e: AbrovaAuthException) {
 *     if (e.code == AbrovaAuthErrorCode.PROVIDER_ALREADY_LINKED) {
 *         // This Google account already belongs to another user.
 *     }
 * }
 * ```
 *
 * @property httpStatus The HTTP status, or 0 when the request never got an answer.
 * @property retryAfterSec For [AbrovaAuthErrorCode.RATE_LIMITED]: seconds to wait.
 * @property fields For [AbrovaAuthErrorCode.INVALID_INPUT]: refused fields and why.
 */
class AbrovaAuthException(
    val code: String,
    message: String,
    val httpStatus: Int = 0,
    val retryAfterSec: Int? = null,
    val fields: Map<String, String>? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    val isProviderAlreadyLinked: Boolean get() = code == AbrovaAuthErrorCode.PROVIDER_ALREADY_LINKED

    /** The session cannot be refreshed any more: the user has to sign in again. */
    internal val endsSession: Boolean
        get() = code == AbrovaAuthErrorCode.SESSION_REVOKED ||
            code == AbrovaAuthErrorCode.SESSION_EXPIRED ||
            code == AbrovaAuthErrorCode.TOKEN_INVALID ||
            code == AbrovaAuthErrorCode.USER_DISABLED

    override fun toString(): String = "AbrovaAuthException($code, $httpStatus): $message"

    internal companion object {
        fun notSignedIn() = AbrovaAuthException(AbrovaAuthErrorCode.NOT_SIGNED_IN, "No user is signed in")
        fun notConfigured() = AbrovaAuthException(
            AbrovaAuthErrorCode.SDK_NOT_CONFIGURED,
            "Call AbrovaAuth.shared.configure(context, apiKey) first",
        )
    }
}

/** Error codes. Server codes keep their server spelling. */
object AbrovaAuthErrorCode {
    const val INVALID_INPUT = "INVALID_INPUT"
    const val INVALID_API_KEY = "INVALID_API_KEY"
    const val METHOD_DISABLED = "METHOD_DISABLED"
    const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    const val EMAIL_TAKEN = "EMAIL_TAKEN"
    const val EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED"
    const val WEAK_PASSWORD = "WEAK_PASSWORD"
    const val INVALID_ID_TOKEN = "INVALID_ID_TOKEN"
    const val NONCE_REUSED = "NONCE_REUSED"
    const val PROVIDER_ALREADY_LINKED = "PROVIDER_ALREADY_LINKED"
    const val TOKEN_INVALID = "TOKEN_INVALID"
    const val SESSION_EXPIRED = "SESSION_EXPIRED"
    const val SESSION_REVOKED = "SESSION_REVOKED"
    const val USER_DISABLED = "USER_DISABLED"
    const val UNAUTHENTICATED = "UNAUTHENTICATED"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val LIMIT_REACHED = "LIMIT_REACHED"
    const val PRODUCT_DISABLED = "PRODUCT_DISABLED"
    const val MAU_LIMIT = "MAU_LIMIT"
    const val NOT_CONFIGURED = "NOT_CONFIGURED"
    const val INTERNAL = "INTERNAL"

    /** [AbrovaAuth.configure] was not called. */
    const val SDK_NOT_CONFIGURED = "SDK_NOT_CONFIGURED"
    /** The call needs a signed-in user and there is none. */
    const val NOT_SIGNED_IN = "NOT_SIGNED_IN"
    /** No connection, a timeout, or the server could not be reached. */
    const val NETWORK_ERROR = "NETWORK_ERROR"
    /** The server answered something the SDK could not read. */
    const val INVALID_RESPONSE = "INVALID_RESPONSE"
}
