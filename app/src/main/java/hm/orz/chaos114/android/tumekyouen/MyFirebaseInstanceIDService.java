package hm.orz.chaos114.android.tumekyouen;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import hm.orz.chaos114.android.tumekyouen.util.ServerUtil;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("HOGE", "refreshedToken: " + refreshedToken);
        ServerUtil.registGcm(this, refreshedToken);
    }
}
