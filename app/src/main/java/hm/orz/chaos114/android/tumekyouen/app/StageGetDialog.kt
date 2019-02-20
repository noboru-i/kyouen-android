package hm.orz.chaos114.android.tumekyouen.app

import android.content.Context
import android.content.DialogInterface
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.databinding.StageGetDialogBinding

class StageGetDialog(
    context: Context,
    successListener: OnSuccessListener?,
    cancelListener: DialogInterface.OnCancelListener
) : ValidationDialog(context) {

    private val binding = StageGetDialogBinding.inflate(layoutInflater, null, false)

    private val count: Int
        get() {
            val checked = binding.dialogAllCheck.isChecked
            if (checked) {
                return -1
            }

            val countStr = binding.dialogNumber.text.toString()
            try {
                val count = Integer.parseInt(countStr)
                return if (count <= 0) {
                    0
                } else count
            } catch (e: NumberFormatException) {
                return 0
            }
        }

    init {
        setView(binding.root)

        setTitle(R.string.dialog_title_stage_get)
        binding.dialogNumber.setText("1")
        binding.dialogNumber.selectAll()

        val getStr = context.getString(R.string.dialog_get)
        setPositiveButton(getStr, DialogInterface.OnClickListener { _, _ ->
            successListener?.onSuccess(count)
        })
        val cancelStr = context.getString(R.string.dialog_cancel)
        setCancelButton(cancelStr, cancelListener)

        binding.dialogAllCheck.setOnCheckedChangeListener { _, isChecked ->
            binding.dialogNumber.isEnabled = !isChecked
        }
    }

    override fun hasError(): Boolean {
        return count == 0
    }

    interface OnSuccessListener {
        fun onSuccess(count: Int)
    }
}
