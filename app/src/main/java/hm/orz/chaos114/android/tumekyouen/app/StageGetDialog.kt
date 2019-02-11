package hm.orz.chaos114.android.tumekyouen.app

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import hm.orz.chaos114.android.tumekyouen.R

class StageGetDialog(
        context: Context,
        successListener: OnSuccessListener?,
        cancelListener: DialogInterface.OnCancelListener
) : ValidationDialog(context) {

    // ステージ数入力領域
    private val numberEdit: EditText

    // 全件チェックボックス
    private val allCheckBox: CheckBox

    /**
     * 入力されている数値を返却します。
     *
     *
     * チェックされていた場合は"-1"を返却します。 0以下の数値、または数値以外が入力されていた場合は"0"を返却します。
     *
     * @return 入力されている数値
     */
    private val count: Int
        get() {
            val checked = allCheckBox.isChecked
            if (checked) {
                // チェックされていた場合は-1を返却
                return -1
            }

            val countStr = numberEdit.text.toString()
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

        // viewの設定
        val layoutInflater = layoutInflater
        val view = layoutInflater.inflate(R.layout.stage_get_dialog, null)
        setView(view)
        numberEdit = view.findViewById<View>(R.id.dialog_number) as EditText
        allCheckBox = view.findViewById<View>(R.id.dialog_all_check) as CheckBox

        // パラメータの設定
        setTitle(R.string.dialog_title_stage_get)
        numberEdit.setText("1")
        numberEdit.selectAll()

        // ボタンの設定
        val getStr = context.getString(R.string.dialog_get)
        setPositiveButton(getStr, DialogInterface.OnClickListener { _, _ ->
            successListener?.onSuccess(count)
        })
        val cancelStr = context.getString(R.string.dialog_cancel)
        setCancelButton(cancelStr, cancelListener)

        // チェックボックスの設定
        allCheckBox.setOnCheckedChangeListener { buttonView, isChecked -> numberEdit.isEnabled = !isChecked }
    }

    override fun hasError(): Boolean {
        val count = count
        return count == 0
    }

    // 成功時のリスナー
    interface OnSuccessListener {
        fun onSuccess(count: Int)
    }
}
