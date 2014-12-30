package hm.orz.chaos114.android.tumekyouen.network;

import hm.orz.chaos114.android.tumekyouen.network.entity.Response;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface TumekyouenClient {
    @FormUrlEncoded
    @POST("/gcm/regist")
    void registGcm(@Field("regId") String registrationId, Callback<Response> cb);
}
