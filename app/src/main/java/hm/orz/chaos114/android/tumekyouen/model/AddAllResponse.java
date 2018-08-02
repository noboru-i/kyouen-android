package hm.orz.chaos114.android.tumekyouen.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Date;
import java.util.List;

@AutoValue
public abstract class AddAllResponse {
    public abstract String message();

    public abstract List<Stage> data();

    public static TypeAdapter<AddAllResponse> typeAdapter(Gson gson) {
        return new AutoValue_AddAllResponse.GsonTypeAdapter(gson);
    }

    @AutoValue
    public static abstract class Stage {
        public abstract int stageNo();

        public abstract Date clearDate();

        public static TypeAdapter<Stage> typeAdapter(Gson gson) {
            return new AutoValue_AddAllResponse_Stage.GsonTypeAdapter(gson);
        }
    }
}
