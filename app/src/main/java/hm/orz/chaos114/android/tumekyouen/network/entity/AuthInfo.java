package hm.orz.chaos114.android.tumekyouen.network.entity;

import lombok.Value;

@Value
public class AuthInfo {
    private String token;
    private String tokenSecret;
}
