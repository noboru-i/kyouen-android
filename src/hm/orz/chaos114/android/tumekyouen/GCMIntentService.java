package hm.orz.chaos114.android.tumekyouen;

import static hm.orz.chaos114.android.tumekyouen.constants.GcmConstants.SERVER_ID;
import hm.orz.chaos114.android.tumekyouen.util.NotificationUtil;
import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

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
		super(SERVER_ID);
		Log.i("kyouen", "GCMIntentService init");
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
