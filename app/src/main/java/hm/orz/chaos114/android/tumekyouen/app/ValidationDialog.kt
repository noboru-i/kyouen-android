package hm.orz.chaos114.android.tumekyouen.app

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

abstract class ValidationDialog(
        context: Context
) : AlertDialog(context) {

    private var reshow: Boolean = false

    init {
        setCancelable(true)
    }

    protected abstract fun hasError(): Boolean

    fun setPositiveButton(text: CharSequence, listener: DialogInterface.OnClickListener?) {
        setButton(DialogInterface.BUTTON_POSITIVE, text) { dialog, which ->
            if (hasError()) {
                reshow = true
                cancel()
                return@setButton
            }

            listener?.onClick(dialog, which)
        }
    }

    fun setCancelButton(text: CharSequence,
                        listener: DialogInterface.OnCancelListener?) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text) { dialog, which -> cancel() }

        setOnCancelListener { dialog ->
            if (reshow) {
                show()
                return@setOnCancelListener
            }

            listener?.onCancel(dialog)
        }
    }

    override fun show() {
        reshow = false
        super.show()
    }
}
