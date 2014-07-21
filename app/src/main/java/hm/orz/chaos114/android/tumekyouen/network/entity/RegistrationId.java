package hm.orz.chaos114.android.tumekyouen.network.entity;

import org.springframework.util.LinkedMultiValueMap;

public class RegistrationId extends LinkedMultiValueMap<String, String> {
    public RegistrationId(String regId) {
        add("regId", regId);
    }
}
