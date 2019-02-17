package hm.orz.chaos114.android.tumekyouen.modules.title

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenService
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class TitleViewModel @Inject constructor(
        private val context: Context,
        private val loginUtil: LoginUtil,
        private val tumeKyouenService: TumeKyouenService,
        private val tumeKyouenRepository: TumeKyouenRepository,
        private val soundManager: SoundManager
) : ViewModel() {

    enum class ConnectStatus {
        BEFORE_CONNECT,
        CONNECTING,
        CONNECTED,
        SYNCING,
    }

    private val twitterAuthClient = TwitterAuthClient()

    private val stageCountModel = MutableLiveData<StageCountModel>()
    private val mutableConnectStatus = MutableLiveData<ConnectStatus>()

    private val disposable: CompositeDisposable = CompositeDisposable()

    val displayStageCount: LiveData<String> = Transformations.map(stageCountModel) { stageCountModel ->
        context.getString(R.string.stage_count,
                stageCountModel.clearStageCount,
                stageCountModel.stageCount)
    }

    val soundResource: LiveData<Drawable> = Transformations.map(soundManager.isPlayable.toFlowable(BackpressureStrategy.BUFFER).toLiveData()) { isPlayable ->
        @DrawableRes val imageRes = if (isPlayable) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
        ContextCompat.getDrawable(context, imageRes)
    }

    val connectButtonEnabled: LiveData<Boolean> = Transformations.map(mutableConnectStatus) { status ->
        status == ConnectStatus.BEFORE_CONNECT
    }

    val connectButtonShow: LiveData<Boolean> = Transformations.map(mutableConnectStatus) { status ->
        status == ConnectStatus.BEFORE_CONNECT || status == ConnectStatus.CONNECTING
    }

    val syncButtonEnabled: LiveData<Boolean> = Transformations.map(mutableConnectStatus) { status ->
        status == ConnectStatus.CONNECTED
    }

    val showLoading: LiveData<Boolean> = Transformations.map(mutableConnectStatus) { status ->
        status == ConnectStatus.CONNECTING
    }

    fun onCreate() {
        val loginInfo = loginUtil.loadLoginInfo()
        Timber.d("loginInfo = %s", loginInfo)
        if (loginInfo != null) {
            mutableConnectStatus.value = ConnectStatus.CONNECTING
            disposable.add(
                    tumeKyouenService.login(loginInfo.token, loginInfo.secret)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { s ->
                                        Timber.d("sucess : %s", s)
                                        mutableConnectStatus.value = ConnectStatus.CONNECTED
                                    },
                                    { throwable ->
                                        Timber.d(throwable, "fail")
                                        mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
                                    })
            )
        } else {
            mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    fun refresh() {
        disposable.add(
                tumeKyouenRepository.selectStageCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { stageCountModel ->
                            this.stageCountModel.value = stageCountModel
                        }
        )
    }

    fun toggleSoundPlayable() {
        soundManager.togglePlayable()
    }

    // FIXME remove activity reference
    fun requestConnectTwitter(activity: Activity) {
        twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                Timber.d("success")
                sendAuthToken(result.data.authToken)
            }

            override fun failure(e: TwitterException) {
                Timber.d("failure")
                // FIXME show alert by View
                AlertDialog.Builder(activity)
                        .setMessage(R.string.alert_error_authenticate_twitter)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
            }
        })
    }

    private fun sendAuthToken(authToken: TwitterAuthToken) {
        disposable.add(
                tumeKyouenService.login(authToken.token, authToken.secret)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    loginUtil.saveLoginInfo(authToken)
                                    mutableConnectStatus.value = ConnectStatus.CONNECTED
                                },
                                {
                                    mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
                                    // FIXME show alert by View
                                    AlertDialog.Builder(context)
                                            .setMessage(R.string.alert_error_authenticate_twitter)
                                            .setPositiveButton(android.R.string.ok, null).show()

                                }
                        )
        )
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
