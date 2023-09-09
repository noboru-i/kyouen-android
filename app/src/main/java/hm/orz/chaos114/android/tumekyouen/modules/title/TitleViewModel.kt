package hm.orz.chaos114.android.tumekyouen.modules.title

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.toLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.internal.bind.util.ISO8601Utils
//import com.twitter.sdk.android.core.Callback
//import com.twitter.sdk.android.core.Result
//import com.twitter.sdk.android.core.TwitterAuthToken
//import com.twitter.sdk.android.core.TwitterException
//import com.twitter.sdk.android.core.TwitterSession
//import com.twitter.sdk.android.core.identity.TwitterAuthClient
import hm.orz.chaos114.android.tumekyouen.R
import hm.orz.chaos114.android.tumekyouen.model.StageCountModel
import hm.orz.chaos114.android.tumekyouen.network.TumeKyouenV2Service
import hm.orz.chaos114.android.tumekyouen.network.models.ClearedStage
import hm.orz.chaos114.android.tumekyouen.network.models.LoginParam
import hm.orz.chaos114.android.tumekyouen.repository.TumeKyouenRepository
import hm.orz.chaos114.android.tumekyouen.usecase.InsertDataTask
import hm.orz.chaos114.android.tumekyouen.util.Event
import hm.orz.chaos114.android.tumekyouen.util.LoginUtil
import hm.orz.chaos114.android.tumekyouen.util.SoundManager
import hm.orz.chaos114.android.tumekyouen.util.StringResource
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class TitleViewModel @Inject constructor(
    private val context: Context,
    private val loginUtil: LoginUtil,
    private val tumeKyouenV2Service: TumeKyouenV2Service,
    private val tumeKyouenRepository: TumeKyouenRepository,
    private val soundManager: SoundManager,
    private val insertDataTask: InsertDataTask
) : ViewModel() {

    enum class ConnectStatus {
        BEFORE_CONNECT,
        CONNECTING,
        CONNECTED,
        SYNCING,
    }

//    private val twitterAuthClient = TwitterAuthClient()

    private lateinit var auth: FirebaseAuth

    private val stageCountModel = MutableLiveData<StageCountModel>()
    private val mutableConnectStatus = MutableLiveData<ConnectStatus>()

    private val disposable: CompositeDisposable = CompositeDisposable()

    val displayStageCount: LiveData<String> =
        stageCountModel.map { stageCountModel ->
            context.getString(
                R.string.stage_count,
                stageCountModel.clearStageCount,
                stageCountModel.stageCount
            )
        }

    val soundResource: LiveData<Drawable> =
        soundManager.isPlayable.toFlowable(BackpressureStrategy.BUFFER).toLiveData().map { isPlayable ->
            @DrawableRes val imageRes =
                if (isPlayable) R.drawable.ic_volume_up_black else R.drawable.ic_volume_off_black
            ContextCompat.getDrawable(context, imageRes)!!
        }

    val connectButtonEnabled: LiveData<Boolean> =
        mutableConnectStatus.map { status ->
            status == ConnectStatus.BEFORE_CONNECT
        }

    val connectButtonShow: LiveData<Boolean> = mutableConnectStatus.map { status ->
        status == ConnectStatus.BEFORE_CONNECT || status == ConnectStatus.CONNECTING
    }

    val syncButtonEnabled: LiveData<Boolean> = mutableConnectStatus.map { status ->
        status == ConnectStatus.CONNECTED
    }

    val showLoading: LiveData<Boolean> = mutableConnectStatus.map { status ->
        status == ConnectStatus.CONNECTING || status == ConnectStatus.SYNCING
    }

    private val _isRunningInsertTask = MutableLiveData<Boolean>()
    val isRunningInsertTask: LiveData<Boolean>
        get() = _isRunningInsertTask

    private val _alertMessage = MutableLiveData<Event<Int>>()
    val alertMessage: LiveData<Event<Int>>
        get() = _alertMessage

    private val _toastMessage = MutableLiveData<Event<StringResource>>()
    val toastMessage: LiveData<Event<StringResource>>
        get() = _toastMessage

    fun onCreate() {
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // already logged in
            mutableConnectStatus.value = ConnectStatus.CONNECTED
            return
        }

        // migration authentication
//        val loginInfo = loginUtil.loadLoginInfo()
//        if (loginInfo == null) {
//            mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
//            return
//        }

        mutableConnectStatus.value = ConnectStatus.CONNECTING
//        sendAuthToken(TwitterAuthToken(loginInfo.token, loginInfo.secret))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    fun refresh() {
        _isRunningInsertTask.value = insertDataTask.running
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
        mutableConnectStatus.value = ConnectStatus.CONNECTING
//        twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
//            override fun success(result: Result<TwitterSession>) {
//                Timber.d("success")
//                sendAuthToken(result.data.authToken)
//            }
//
//            override fun failure(e: TwitterException) {
//                Timber.d("failure")
//                _alertMessage.value = Event(R.string.alert_error_authenticate_twitter)
//                mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
//            }
//        })
    }

    fun requestSync() {
        mutableConnectStatus.value = ConnectStatus.SYNCING
        GlobalScope.launch {
            val clearedStages = tumeKyouenRepository.selectAllClearStage()
                .subscribeOn(Schedulers.io())
                .map { stages ->
                    stages.map { s ->
                        ClearedStage(
                            stageNo = s.stageNo.toLong(),
                            clearDate = ISO8601Utils.format(s.clearDate)
                        )
                    }
                }
                .blockingGet()
            val response = tumeKyouenV2Service.postSync(clearedStages)
            response.body()?.let {
                tumeKyouenRepository.updateSyncClearData(it)
                    .subscribe()
            }

            withContext(Dispatchers.Main) {
                mutableConnectStatus.value = ConnectStatus.CONNECTED

                if (response.isSuccessful) {
                    refresh()
                } else {
                    _alertMessage.value = Event(R.string.alert_error_sync)
                }
            }
        }
    }

    fun requestStages(count: Int) {
        val taskCount = if (count == -1) Integer.MAX_VALUE else count
        disposable.add(
            tumeKyouenRepository.selectMaxStageNo()
                .subscribeOn(Schedulers.io())
                .flatMap { maxStageNo -> insertDataTask.run(maxStageNo, taskCount) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { successCount ->
                        _toastMessage.value = Event(
                            StringResource(R.string.toast_get_stage, arrayOf(successCount))
                        )
                        refresh()
                    },
                    {
                        _toastMessage.value = Event(StringResource(R.string.toast_no_stage))
                    }
                )
        )
    }

//    private fun sendAuthToken(authToken: TwitterAuthToken) {
//        GlobalScope.launch {
//            val response = tumeKyouenV2Service.login(LoginParam(authToken.token, authToken.secret))
//            withContext(Dispatchers.Main) {
//                if (!response.isSuccessful) {
//                    withContext(Dispatchers.IO) {
//                        Timber.d(
//                            "error body: %s",
//                            response.errorBody()?.string()
//                        )
//                    }
//
//                    mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
//                    _alertMessage.value = Event(R.string.alert_error_authenticate_twitter)
//                    return@withContext
//                }
//
//                auth.signInWithCustomToken(response.body()!!.token)
//                    .addOnCompleteListener { task ->
//                        if (!task.isSuccessful) {
//                            mutableConnectStatus.value = ConnectStatus.BEFORE_CONNECT
//                            _alertMessage.value = Event(R.string.alert_error_authenticate_twitter)
//                            return@addOnCompleteListener
//                        }
//
//                        val user = auth.currentUser
//                        Timber.d("Firebase auth user: %s", user?.uid)
//                        mutableConnectStatus.value = ConnectStatus.CONNECTED
//                    }
//            }
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}
