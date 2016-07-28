package hm.orz.chaos114.android.tumekyouen.model;

import java.util.List;

import lombok.Data;

@Data
public class AddAllResponse {
    private String message;
    private List<TumeKyouenModel> data;
}
