package hm.orz.chaos114.android.tumekyouen;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil;
import timber.log.Timber;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Timber.d("From: %s", message.getFrom());
        Timber.d("Notification Data: %s", message.getData());

        NotificationUtil.notify(this, getString(R.string.app_name),
                getString(R.string.notification_new_stage));
    }
}
