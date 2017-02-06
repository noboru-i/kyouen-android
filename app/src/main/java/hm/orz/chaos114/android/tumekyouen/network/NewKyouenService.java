package hm.orz.chaos114.android.tumekyouen.network;

import java.util.List;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import hm.orz.chaos114.android.tumekyouen.network.entity.Stage;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Interface of kyouen API.
 */
public interface NewKyouenService {
    @POST("/page/api_login")
    Observable<Void> login(@Field("token") String token,
                           @Field("token_secret") String tokenSecret);

    @POST("/page/add_all")
    Observable<AddAllResponse> addAll(@Field("data") String data);

    @POST("/page/add")
    Observable<Void> add(@Field("stageNo") int stageNo);

    @GET("/stages")
    Observable<List<Stage>> getStage(@Query("offset") int offset, @Query("limit") int limit);
}
