package hm.orz.chaos114.android.tumekyouen.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import hm.orz.chaos114.android.tumekyouen.R;

public class StageGetDialog extends ValidationDialog {

    /** ステージ数入力領域 */
    private EditText numberEdit;

    /** 全件チェックボックス */
    private CheckBox allCheckBox;

    /**
     * コンストラクタ。
     *
     * @param context         コンテキスト
     * @param successListener 成功時のリスナー
     * @param cancelListener  キャンセル時のリスナー
     */
    public StageGetDialog(Context context,
                          final OnSuccessListener successListener,
                          OnCancelListener cancelListener) {
        super(context);

        // viewの設定
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.stage_get_dialog, null);
        setView(view);
        numberEdit = (EditText) view.findViewById(R.id.dialog_number);
        allCheckBox = (CheckBox) view.findViewById(R.id.dialog_all_check);

        // パラメータの設定
        setTitle(R.string.dialog_title_stage_get);
        numberEdit.setText("1");
        numberEdit.selectAll();

        // ボタンの設定
        String getStr = context.getString(R.string.dialog_get);
        setPositiveButton(getStr, ((dialog, which) -> {
            if (successListener != null) {
                successListener.onSuccess(getCount());
            }
        }));
        String cancelStr = context.getString(R.string.dialog_cancel);
        setCancelButton(cancelStr, cancelListener);

        // チェックボックスの設定
        allCheckBox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            numberEdit.setEnabled(!isChecked);
        }));
    }

    /**
     * 入力されている数値を返却します。
     * <p>
     * チェックされていた場合は"-1"を返却します。 0以下の数値、または数値以外が入力されていた場合は"0"を返却します。
     *
     * @return 入力されている数値
     */
    private int getCount() {
        boolean checked = allCheckBox.isChecked();
        if (checked) {
            // チェックされていた場合は-1を返却
            return -1;
        }

        String countStr = numberEdit.getText().toString();
        try {
            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                // 0以下が入力されていた場合はエラー
                return 0;
            }
            return count;
        } catch (NumberFormatException e) {
            // 数値に変換出来なかった場合はエラー
            return 0;
        }
    }

    @Override
    protected boolean hasError() {
        int count = getCount();
        return (count == 0);
    }

    /** 成功時のリスナー */
    public interface OnSuccessListener {
        void onSuccess(int count);
    }
}
