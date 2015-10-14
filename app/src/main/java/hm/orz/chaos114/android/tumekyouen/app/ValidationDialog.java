package hm.orz.chaos114.android.tumekyouen.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public abstract class ValidationDialog extends AlertDialog {

    /** 再表示フラグ */
    private boolean reshow;

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     */
    public ValidationDialog(Context context) {
        super(context);
        setCancelable(true);
    }

    /**
     * 入力値のエラーチェックを行います。
     *
     * @return エラーの場合true
     */
    protected abstract boolean hasError();

    /**
     * 肯定ボタンの設定を行います。
     *
     * @param text     ボタンのラベル
     * @param listener エラーが存在しない場合に呼び出されるリスナー
     */
    public void setPositiveButton(CharSequence text,
                                  final OnClickListener listener) {
        setButton(DialogInterface.BUTTON_POSITIVE, text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (hasError()) {
                            reshow = true;
                            cancel();
                            return;
                        }

                        // エラーが存在しない場合
                        if (listener != null) {
                            listener.onClick(dialog, which);
                        }
                    }
                });
    }

    /**
     * キャンセルボタンの設定を行います。
     *
     * @param text     ボタンのラベル
     * @param listener 再表示されない場合に呼び出されるリスナー
     */
    public void setCancelButton(CharSequence text,
                                final OnCancelListener listener) {
        // キャンセルボタン押下時の設定
        setButton(DialogInterface.BUTTON_NEUTRAL, text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                });

        // キャンセル時の設定
        setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (reshow) {
                    show();
                    return;
                }

                // エラーが存在しない場合
                if (listener != null) {
                    listener.onCancel(dialog);
                }
            }
        });
    }

    @Override
    public void show() {
        reshow = false;
        super.show();
    }
}
