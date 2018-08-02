package hm.orz.chaos114.android.tumekyouen.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class LoginResult {
    public abstract String message();

    public static LoginResult create(String message) {
        return new AutoValue_LoginResult(message);
    }

    public static TypeAdapter<LoginResult> typeAdapter(Gson gson) {
        return new AutoValue_LoginResult.GsonTypeAdapter(gson);
    }
}
