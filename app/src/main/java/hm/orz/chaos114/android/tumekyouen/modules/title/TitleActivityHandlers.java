package hm.orz.chaos114.android.tumekyouen.modules.title;

import android.view.View;
import android.widget.Button;

/**
 * TitleActivityのHandlerクラス。
 */
public interface TitleActivityHandlers {
    void onClickStartButton(View view);
    void onClickGetStage(View v);
    void onClickCreateStage(View v);
    void onClickConnectButton(View view);
    void onClickSyncButton(View view);
    void switchPlayable(View view);
}
