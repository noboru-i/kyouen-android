package hm.orz.chaos114.android.tumekyouen.network

import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import hm.orz.chaos114.android.tumekyouen.network.models.ClearStage
import hm.orz.chaos114.android.tumekyouen.network.models.ClearedStage
import hm.orz.chaos114.android.tumekyouen.network.models.LoginParam
import hm.orz.chaos114.android.tumekyouen.network.models.LoginResult
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import retrofit2.Invocation
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface TumeKyouenV2Service {
    @POST("/v2/users/login")
    suspend fun login(@Body loginParam: LoginParam): Response<LoginResult>

    @RequireAuth
    @PUT("/v2/stages/{stageNo}/clear")
    suspend fun putStagesClear(@Path("stageNo") stageNo: Int, @Body clearStage: ClearStage): Response<Void>

    @RequireAuth
    @POST("/v2/stages/sync")
    suspend fun postSync(@Body clearStage: List<ClearedStage>): Response<List<ClearedStage>>
}

annotation class RequireAuth

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()

        val invocation = request.tag(Invocation::class.java)
        val authAnnotation = invocation?.method()?.getAnnotation(RequireAuth::class.java)
        if (authAnnotation != null) {
            runBlocking {
                getIdToken()?.let {
                    request = request
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $it")
                        .build()
                }
            }
        }
        return chain.proceed(request)
    }

    private suspend fun getIdToken(): String? =
        suspendCoroutine { cont ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                cont.resume(null)
                return@suspendCoroutine
            }

            user.getIdToken(true)
                .addOnCompleteListener(object : OnCompleteListener<GetTokenResult> {
                    override fun onComplete(@NonNull task: Task<GetTokenResult>) {
                        if (!task.isSuccessful) {
                            cont.resume(null)
                            return
                        }

                        val idToken = task.getResult()?.token
                        cont.resume(idToken)
                    }
                })
        }
}
