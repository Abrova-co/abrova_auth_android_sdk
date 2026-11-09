package ir.abrova.auth.ui

import ir.abrova.auth.AbrovaAuthException

/** The sign-in screen's texts: English, and each language in [LANGUAGES]. */
class AbrovaSignInStrings private constructor(
    /** The language whose texts are shown: one the screen has (see [LANGUAGES]), English for any other. */
    val languageCode: String,
    private val table: Map<String, String>,
    /** Whether the language reads right to left. */
    val rtl: Boolean,
) {
    operator fun get(key: String): String = table[key] ?: EN[key] ?: key

    /** The user-facing message for an error code; never the server's English hint. */
    fun error(code: String): String = table["error.$code"] ?: EN["error.$code"] ?: get("error.other")

    companion object {
        /** The texts of [languageCode] when the screen has them, English for everything else. */
        @JvmStatic
        fun of(languageCode: String?): AbrovaSignInStrings {
            val code = (languageCode ?: "").trim().lowercase().split('-', '_').first()
            return LANGUAGES[code] ?: LANGUAGES.getValue("en")
        }

        /** The error code of [e]; `unknown` (shown as `error.other`) for anything that is not an Abrova Auth error. */
        @JvmStatic
        fun errorCodeOf(e: Throwable): String = (e as? AbrovaAuthException)?.code ?: "unknown"

        private val EN = mapOf(
        "title" to "Sign in",
        "titleTo" to "Sign in to {app}",
        "signUpTitle" to "Create an account",
        "forgotTitle" to "Reset your password",
        "magicTitle" to "Sign in with a link",
        "telegramTitle" to "Sign in with Telegram",
        "email" to "E-mail",
        "password" to "Password",
        "phone" to "Phone number",
        "phoneHint" to "+98 912 345 6789",
        "code" to "Code",
        "signIn" to "Sign in",
        "signUp" to "Create account",
        "noAccount" to "No account? Create one",
        "haveAccount" to "Have an account? Sign in",
        "forgot" to "Forgot password?",
        "sendReset" to "Send reset link",
        "sendLink" to "Send sign-in link",
        "sendCode" to "Send code",
        "verify" to "Verify",
        "or" to "or",
        "withEmailLink" to "E-mail me a sign-in link",
        "withTelegram" to "Continue with Telegram",
        "withGoogle" to "Continue with Google",
        "withApple" to "Continue with Apple",
        "guest" to "Continue as guest",
        "back" to "Back",
        "close" to "Close",
        "retry" to "Try again",
        "checkEmail" to "Check your e-mail",
        "resetSent" to "If {email} has an account, a link to choose a new password is on its way.",
        "linkSent" to "We sent a sign-in link to {email}. Open it on this device.",
        "verifySent" to "We sent a confirmation link to {email}. Confirm your e-mail, then sign in.",
        "codeSent" to "Enter the code Telegram sent you.",
        "botFirst" to "Open {bot} in Telegram once and share your phone number, then try again.",
        "noMethods" to "Sign-in is not available right now.",
        "required" to "Required",
        "badEmail" to "Enter a valid e-mail",
        "error.INVALID_CREDENTIALS" to "Wrong e-mail or password.",
        "error.EMAIL_TAKEN" to "This e-mail already has an account. Sign in instead.",
        "error.EMAIL_NOT_VERIFIED" to "Confirm your e-mail first. Check your inbox.",
        "error.WEAK_PASSWORD" to "Choose a longer password that is not a common one.",
        "error.INVALID_INPUT" to "Check what you entered.",
        "error.TOKEN_INVALID" to "This code is wrong or has expired.",
        "error.RATE_LIMITED" to "Too many tries. Wait a little and try again.",
        "error.USER_DISABLED" to "This account is disabled.",
        "error.METHOD_DISABLED" to "This sign-in method is turned off.",
        "error.PRODUCT_DISABLED" to "Sign-in is not available right now.",
        "error.MAU_LIMIT" to "Sign-in is not available right now.",
        "error.NETWORK_ERROR" to "No connection. Check your internet and try again.",
        "error.PROVIDER_ALREADY_LINKED" to "This account is linked to another user.",
        "error.INVALID_ID_TOKEN" to "Sign-in could not be confirmed. Try again.",
        "error.VERIFY_NOT_SENT" to "Your account was created, but the confirmation e-mail could not be sent. Try again later with “Forgot password?”.",
        "error.other" to "Something went wrong. Try again.",
        )

        private val FA = mapOf(
        "title" to "ورود",
        "titleTo" to "ورود به {app}",
        "signUpTitle" to "ساخت حساب",
        "forgotTitle" to "بازیابی رمز",
        "magicTitle" to "ورود با لینک",
        "telegramTitle" to "ورود با تلگرام",
        "email" to "ایمیل",
        "password" to "رمز",
        "phone" to "شماره موبایل",
        "phoneHint" to "+98 912 345 6789",
        "code" to "کد",
        "signIn" to "ورود",
        "signUp" to "ساخت حساب",
        "noAccount" to "حساب ندارید؟ بسازید",
        "haveAccount" to "حساب دارید؟ وارد شوید",
        "forgot" to "رمز را فراموش کرده‌اید؟",
        "sendReset" to "ارسال لینک بازیابی",
        "sendLink" to "ارسال لینک ورود",
        "sendCode" to "ارسال کد",
        "verify" to "تأیید",
        "or" to "یا",
        "withEmailLink" to "لینک ورود را ایمیل کن",
        "withTelegram" to "ادامه با تلگرام",
        "withGoogle" to "ادامه با گوگل",
        "withApple" to "ادامه با اپل",
        "guest" to "ادامه به‌عنوان مهمان",
        "back" to "بازگشت",
        "close" to "بستن",
        "retry" to "دوباره",
        "checkEmail" to "ایمیل خود را ببینید",
        "resetSent" to "اگر {email} حساب داشته باشد، لینک انتخاب رمز تازه برایش فرستاده شد.",
        "linkSent" to "لینک ورود به {email} فرستاده شد. آن را روی همین دستگاه باز کنید.",
        "verifySent" to "لینک تأیید به {email} فرستاده شد. ایمیل را تأیید کنید، سپس وارد شوید.",
        "codeSent" to "کدی را که تلگرام فرستاد وارد کنید.",
        "botFirst" to "یک بار {bot} را در تلگرام باز کنید و شماره خود را بفرستید، سپس دوباره امتحان کنید.",
        "noMethods" to "ورود فعلاً در دسترس نیست.",
        "required" to "لازم است",
        "badEmail" to "یک ایمیل درست وارد کنید",
        "error.INVALID_CREDENTIALS" to "ایمیل یا رمز اشتباه است.",
        "error.EMAIL_TAKEN" to "این ایمیل حساب دارد. وارد شوید.",
        "error.EMAIL_NOT_VERIFIED" to "اول ایمیل خود را تأیید کنید. صندوق ایمیل را ببینید.",
        "error.WEAK_PASSWORD" to "رمز بلندتری انتخاب کنید که رایج نباشد.",
        "error.INVALID_INPUT" to "آنچه وارد کردید را بررسی کنید.",
        "error.TOKEN_INVALID" to "این کد اشتباه است یا منقضی شده.",
        "error.RATE_LIMITED" to "تلاش‌ها زیاد بود. کمی صبر کنید و دوباره امتحان کنید.",
        "error.USER_DISABLED" to "این حساب غیرفعال است.",
        "error.METHOD_DISABLED" to "این روش ورود خاموش است.",
        "error.PRODUCT_DISABLED" to "ورود فعلاً در دسترس نیست.",
        "error.MAU_LIMIT" to "ورود فعلاً در دسترس نیست.",
        "error.NETWORK_ERROR" to "اتصال برقرار نیست. اینترنت را بررسی کنید و دوباره امتحان کنید.",
        "error.PROVIDER_ALREADY_LINKED" to "این حساب به کاربر دیگری وصل است.",
        "error.INVALID_ID_TOKEN" to "ورود تأیید نشد. دوباره امتحان کنید.",
        "error.VERIFY_NOT_SENT" to "حساب ساخته شد، اما ایمیل تأیید فرستاده نشد. بعداً با «رمز را فراموش کرده‌اید؟» دوباره امتحان کنید.",
        "error.other" to "مشکلی پیش آمد. دوباره امتحان کنید.",
        )

        /** The languages of the screen, by the part of a language code before "-" or "_" ("en-US" → "en"). */
        private val LANGUAGES: Map<String, AbrovaSignInStrings> = mapOf(
            "en" to AbrovaSignInStrings("en", EN, rtl = false),
            "fa" to AbrovaSignInStrings("fa", FA, rtl = true),
        )
    }
}
