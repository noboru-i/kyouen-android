package hm.orz.chaos114.android.tumekyouen.network;

import java.util.List;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import hm.orz.chaos114.android.tumekyouen.network.entity.Answer;
import hm.orz.chaos114.android.tumekyouen.network.entity.AuthInfo;
import hm.orz.chaos114.android.tumekyouen.network.entity.Stage;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Interface of kyouen API.
 */
public interface NewKyouenService {
    @POST("/users/login")
    Observable<Void> login(@Body AuthInfo authInfo);

    @POST("/page/add_all")
    Observable<AddAllResponse> addAll(@Field("data") String data);

    @POST("/answers")
    Observable<Void> answer(@Body Answer answer);

    @GET("/stages")
    Observable<List<Stage>> getStage(@Query("offset") int offset, @Query("limit") int limit);
}
