package opensignal.util

import android.util.Log

object SecureLog {
    private const val TAG = "OpenSignal"

    fun w(message: String) {
        Log.w(TAG, message)
    }
}
