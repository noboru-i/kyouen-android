package hm.orz.chaos114.android.tumekyouen.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StageCountModel {

    public abstract int stageCount();

    public abstract int clearStageCount();

    public static StageCountModel create(int stageCount, int clearStageCount) {
        return new AutoValue_StageCountModel(stageCount, clearStageCount);
    }
}
