package hm.orz.chaos114.android.tumekyouen

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("From: %s", message.from)
        Timber.d("Notification Data: %s", message.data)

        NotificationUtil.notify(
            this, getString(R.string.app_name),
            getString(R.string.notification_new_stage)
        )
    }

    override fun onNewToken(s: String) {
        Timber.d("refreshedToken: %s", s)
    }
}
