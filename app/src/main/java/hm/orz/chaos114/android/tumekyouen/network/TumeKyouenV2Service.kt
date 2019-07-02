package hm.orz.chaos114.android.tumekyouen.network

import hm.orz.chaos114.android.tumekyouen.network.models.LoginResult
import hm.orz.chaos114.android.tumekyouen.network.models.LoginParam
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TumeKyouenV2Service {
    @POST("/v2/users/login")
    suspend fun login(@Body loginParam: LoginParam): Response<LoginResult>
}
