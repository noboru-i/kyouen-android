package hm.orz.chaos114.android.tumekyouen.app

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

abstract class ValidationDialog
/**
 * コンストラクタ。
 *
 * @param context コンテキスト
 */
(context: Context) : AlertDialog(context) {

    // 再表示フラグ
    private var reshow: Boolean = false

    init {
        setCancelable(true)
    }

    /**
     * 入力値のエラーチェックを行います。
     *
     * @return エラーの場合true
     */
    protected abstract fun hasError(): Boolean

    /**
     * 肯定ボタンの設定を行います。
     *
     * @param text     ボタンのラベル
     * @param listener エラーが存在しない場合に呼び出されるリスナー
     */
    fun setPositiveButton(text: CharSequence, listener: DialogInterface.OnClickListener?) {
        setButton(DialogInterface.BUTTON_POSITIVE, text) { dialog, which ->
            if (hasError()) {
                reshow = true
                cancel()
                return@setButton
            }

            // エラーが存在しない場合
            listener?.onClick(dialog, which)
        }
    }

    /**
     * キャンセルボタンの設定を行います。
     *
     * @param text     ボタンのラベル
     * @param listener 再表示されない場合に呼び出されるリスナー
     */
    fun setCancelButton(text: CharSequence,
                        listener: DialogInterface.OnCancelListener?) {
        // キャンセルボタン押下時の設定
        setButton(DialogInterface.BUTTON_NEUTRAL, text) { dialog, which -> cancel() }

        // キャンセル時の設定
        setOnCancelListener { dialog ->
            if (reshow) {
                show()
                return@setOnCancelListener
            }

            // エラーが存在しない場合
            listener?.onCancel(dialog)
        }
    }

    override fun show() {
        reshow = false
        super.show()
    }
}
