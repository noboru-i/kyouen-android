package hm.orz.chaos114.android.tumekyouen.network;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import hm.orz.chaos114.android.tumekyouen.model.LoginResult;
import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TumeKyouenService {
    @FormUrlEncoded
    @POST("/page/api_login")
    Single<LoginResult> login(@Field("token") String token,
                              @Field("token_secret") String tokenSecret);

    @FormUrlEncoded
    @POST("/page/add_all")
    Single<AddAllResponse> addAll(@Field("data") String data);

    @FormUrlEncoded
    @POST("/page/add")
    Maybe<Void> add(@Field("stageNo") int stageNo);

    @GET("/kyouen/get")
    Single<Response<ResponseBody>> getStage(@Query("stageNo") int stageNo);
}
