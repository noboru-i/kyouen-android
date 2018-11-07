package hm.orz.chaos114.android.tumekyouen.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import hm.orz.chaos114.android.tumekyouen.R;

public class StageSelectDialog extends ValidationDialog {

    // ステージ番号入力領域
    private EditText numberEdit;

    // 最初へチェックボックス
    private CheckBox firstCheckBox;

    // 最後へチェックボックス
    private CheckBox lastCheckBox;

    /**
     * コンストラクタ。
     *
     * @param context         コンテキスト
     * @param successListener 成功時のリスナー
     * @param cancelListener  キャンセル時のリスナー
     */
    public StageSelectDialog(Context context,
                             final OnSuccessListener successListener,
                             OnCancelListener cancelListener) {
        super(context);

        // viewの設定
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.stage_select_dialog, null);
        setView(view);
        numberEdit = (EditText) view.findViewById(R.id.dialog_number);
        firstCheckBox = (CheckBox) view.findViewById(R.id.dialog_first);
        lastCheckBox = (CheckBox) view.findViewById(R.id.dialog_last);

        // パラメータの設定
        setTitle(R.string.dialog_title_stage_select);
        numberEdit.selectAll();

        // ボタンの設定
        String selectStr = context.getString(R.string.dialog_select);
        setPositiveButton(selectStr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (successListener != null) {
                    successListener.onSuccess(getCount());
                }
            }
        });
        String cancelStr = context.getString(R.string.dialog_cancel);
        setCancelButton(cancelStr, cancelListener);

        // チェックボックスの設定
        firstCheckBox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            numberEdit.setEnabled(!isChecked);
            lastCheckBox.setEnabled(!isChecked);
        }));
        lastCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            numberEdit.setEnabled(!isChecked);
            firstCheckBox.setEnabled(!isChecked);
        });
    }

    /**
     * ステージ番号を設定します。
     *
     * @param stageNo ステージ番号
     */
    public void setStageNo(int stageNo) {
        numberEdit.setText(Integer.toString(stageNo));
        numberEdit.selectAll();
    }

    /**
     * 入力されている数値を返却します。
     * <p/>
     * 「最初へ」がチェックされていた場合は"1"を返却します。 「最後へ」がチェックされていた場合は"-1"を返却します。
     * 0以下の数値、または数値以外が入力されていた場合は"0"を返却します。
     *
     * @return 入力されている数値
     */
    private int getCount() {
        if (firstCheckBox.isChecked()) {
            return 1;
        }
        if (lastCheckBox.isChecked()) {
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

    // 成功時のリスナー
    public interface OnSuccessListener {
        void onSuccess(int count);
    }
}
