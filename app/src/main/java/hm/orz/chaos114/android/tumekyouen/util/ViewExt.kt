package hm.orz.chaos114.android.tumekyouen.util

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

data class StringResource(
    val resId: Int,
    val args: Array<Any>? = null
)

fun View.setupAlertDialog(
    lifecycleOwner: LifecycleOwner,
    alertDialogEvent: LiveData<Event<Int>>
) {
    alertDialogEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            showAlertDialog(context.getString(it))
        }
    })
}

fun View.showAlertDialog(alertText: String) {
    AlertDialog.Builder(context)
        .setMessage(alertText)
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

fun View.setupToast(
    lifecycleOwner: LifecycleOwner,
    toastEvent: LiveData<Event<StringResource>>
) {
    toastEvent.observe(lifecycleOwner, Observer { event ->
        event.getContentIfNotHandled()?.let {
            if (it.args == null) {
                showToast(context.getString(it.resId))
            } else {
                showToast(context.getString(it.resId, *it.args))
            }
        }
    })
}

fun View.showToast(toastText: String) {
    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
}
