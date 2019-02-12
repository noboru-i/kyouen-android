package hm.orz.chaos114.android.tumekyouen.app

import android.content.Context
import android.content.DialogInterface
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.databinding.StageSelectDialogBinding

class StageSelectDialog(
        context: Context,
        successListener: OnSuccessListener?,
        cancelListener: DialogInterface.OnCancelListener?
) : ValidationDialog(context) {

    private val binding: StageSelectDialogBinding = StageSelectDialogBinding.inflate(layoutInflater, null, false)

    private val count: Int
        get() {
            if (binding.dialogFirst.isChecked) {
                return 1
            }
            if (binding.dialogLast.isChecked) {
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
        setTitle(R.string.dialog_title_stage_select)
        binding.dialogNumber.selectAll()

        val selectStr = context.getString(R.string.dialog_select)
        setPositiveButton(selectStr, DialogInterface.OnClickListener { _, _ ->
            successListener?.onSuccess(count)
        })
        val cancelStr = context.getString(R.string.dialog_cancel)
        setCancelButton(cancelStr, cancelListener)

        binding.dialogFirst.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.dialogNumber.isEnabled = !isChecked
            binding.dialogLast.isEnabled = !isChecked
        }
        binding.dialogLast.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.dialogNumber.isEnabled = !isChecked
            binding.dialogFirst.isEnabled = !isChecked
        }
    }

    fun setStageNo(stageNo: Int) {
        binding.dialogNumber.setText(Integer.toString(stageNo))
        binding.dialogNumber.selectAll()
    }

    override fun hasError(): Boolean {
        val count = count
        return count == 0
    }

    interface OnSuccessListener {
        fun onSuccess(count: Int)
    }
}
