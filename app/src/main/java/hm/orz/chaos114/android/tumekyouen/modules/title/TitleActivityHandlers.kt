package hm.orz.chaos114.android.tumekyouen.modules.title

import android.view.View

interface TitleActivityHandlers {
    fun onClickStartButton(view: View)

    fun onClickGetStage(v: View)

    fun onClickCreateStage(v: View)

    fun onClickConnectButton(view: View)

    fun onClickSyncButton(view: View)

    fun switchPlayable(view: View)

    fun onClickPrivacyPolicy(view: View)
}
