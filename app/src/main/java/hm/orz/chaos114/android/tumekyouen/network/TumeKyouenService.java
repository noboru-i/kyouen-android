package hm.orz.chaos114.android.tumekyouen.network;

import hm.orz.chaos114.android.tumekyouen.model.AddAllResponse;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface TumeKyouenService {
    @FormUrlEncoded
    @POST("/page/add_all")
    Observable<AddAllResponse> addAll(@Field("data") String data);

    @FormUrlEncoded
    @POST("/page/add")
    Observable<Void> add(@Field("stageNo") int stageNo);
}
