package hm.orz.chaos114.android.tumekyouen.network;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface TumeKyouenService {
    @FormUrlEncoded
    @POST("/page/api_login")
    Observable<Void> login(@Field("token") String token,
                           @Field("token_secret") String tokenSecret);

    @FormUrlEncoded
    @POST("/page/add_all")
    Observable<AddAllResponse> addAll(@Field("data") String data);
}
