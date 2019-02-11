package hm.orz.chaos114.android.tumekyouen.app

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText

import hm.orz.chaos114.android.tumekyouen.R

class StageSelectDialog
/**
 * コンストラクタ。
 *
 * @param context         コンテキスト
 * @param successListener 成功時のリスナー
 * @param cancelListener  キャンセル時のリスナー
 */
(context: Context,
 successListener: OnSuccessListener?,
 cancelListener: DialogInterface.OnCancelListener?) : ValidationDialog(context) {

    // ステージ番号入力領域
    private val numberEdit: EditText

    // 最初へチェックボックス
    private val firstCheckBox: CheckBox

    // 最後へチェックボックス
    private val lastCheckBox: CheckBox

    /**
     * 入力されている数値を返却します。
     *
     *
     * 「最初へ」がチェックされていた場合は"1"を返却します。 「最後へ」がチェックされていた場合は"-1"を返却します。
     * 0以下の数値、または数値以外が入力されていた場合は"0"を返却します。
     *
     * @return 入力されている数値
     */
    private// 0以下が入力されていた場合はエラー
    // 数値に変換出来なかった場合はエラー
    val count: Int
        get() {
            if (firstCheckBox.isChecked) {
                return 1
            }
            if (lastCheckBox.isChecked) {
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
        val view = layoutInflater.inflate(R.layout.stage_select_dialog, null)
        setView(view)
        numberEdit = view.findViewById<View>(R.id.dialog_number) as EditText
        firstCheckBox = view.findViewById<View>(R.id.dialog_first) as CheckBox
        lastCheckBox = view.findViewById<View>(R.id.dialog_last) as CheckBox

        // パラメータの設定
        setTitle(R.string.dialog_title_stage_select)
        numberEdit.selectAll()

        // ボタンの設定
        val selectStr = context.getString(R.string.dialog_select)
        setPositiveButton(selectStr, DialogInterface.OnClickListener { _, _ ->
            successListener?.onSuccess(count)
        })
        val cancelStr = context.getString(R.string.dialog_cancel)
        setCancelButton(cancelStr, cancelListener)

        // チェックボックスの設定
        firstCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            numberEdit.isEnabled = !isChecked
            lastCheckBox.isEnabled = !isChecked
        }
        lastCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            numberEdit.isEnabled = !isChecked
            firstCheckBox.isEnabled = !isChecked
        }
    }

    /**
     * ステージ番号を設定します。
     *
     * @param stageNo ステージ番号
     */
    fun setStageNo(stageNo: Int) {
        numberEdit.setText(Integer.toString(stageNo))
        numberEdit.selectAll()
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
