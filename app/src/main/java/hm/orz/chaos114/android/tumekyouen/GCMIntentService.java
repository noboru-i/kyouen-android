package hm.orz.chaos114.android.tumekyouen;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;

/**
 * GCM通知受信用のサービスクラス。
 *
 * @author noboru
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = GCMIntentService.class.getSimpleName();

    /**
     * コンストラクタ。
     */
    public GCMIntentService() {
        super();
        Log.i("kyouen", "GCMIntentService init");
    }

    public static String getSenderId(Context context) {
        String senderId = context.getString(R.string.gcm_sender_id);
        return senderId;
    }

    @Override
    protected String[] getSenderIds(Context context) {
        return new String[]{getSenderId(this)};
    }

    @Override
    public void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "#onRegistered registration id:" + registrationId);
        ServerUtil.registGcm(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "#onUnregistered registration id:" + registrationId);
        ServerUtil.unregistGcm(context, registrationId);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "#onError error id:" + errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "#onMessage:" + intent);
        Log.i(TAG, "#onMessage message = " + intent.getStringExtra("message"));
        NotificationUtil.notify(context, getString(R.string.app_name),
                getString(R.string.notification_new_stage));
    }
}
