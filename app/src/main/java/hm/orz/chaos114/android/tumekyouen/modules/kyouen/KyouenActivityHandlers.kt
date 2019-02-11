package hm.orz.chaos114.android.tumekyouen.modules.kyouen

import android.view.View

/**
 * KyouenActivityのHandlerクラス。
 */
interface KyouenActivityHandlers {
    fun onClickCheckKyouen(view: View)

    fun onClickMoveStage(v: View)

    fun showSelectStageDialog(view: View)
}
