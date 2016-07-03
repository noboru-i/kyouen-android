package hm.orz.chaos114.android.tumekyouen;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.d(TAG, "From: " + message.getFrom());
        Log.d(TAG, "Notification Data: " + message.getData());

        NotificationUtil.notify(this, getString(R.string.app_name),
                getString(R.string.notification_new_stage));
    }
}
