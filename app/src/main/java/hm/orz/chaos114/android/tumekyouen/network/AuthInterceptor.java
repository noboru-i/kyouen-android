package hm.orz.chaos114.android.tumekyouen.network;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Authorization interceptor.
 */
public class AuthInterceptor implements Interceptor {
    private String token;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (token != null) {
            request = request.newBuilder()
                    .addHeader("X-Authorization", token)
                    .build();
        }
        Response response = chain.proceed(request);
        if (!request.url().encodedPath().equals("/users/login")) {
            return response;
        }
        String rawString = response.body().string();
        saveToken(rawString);
        return response.newBuilder().body(ResponseBody.create(response.body().contentType(), rawString)).build();
    }

    private void saveToken(String body) {
        try {
            JSONObject obj = new JSONObject(body);
            if (obj.has("api_token")) {
                token = obj.getString("api_token");
            }
        } catch (JSONException e) {
            // ignore

        }
    }
}
