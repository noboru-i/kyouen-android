package hm.orz.chaos114.android.tumekyouen.network;

import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.FormHttpMessageConverter;

import hm.orz.chaos114.android.tumekyouen.network.entity.RegistrationId;

@Rest(rootUrl = "http://my-android-server.appspot.com", converters = {FormHttpMessageConverter.class})
public interface TumekyouenClient {
    @Post("/gcm/regist")
    void registGcm(RegistrationId registrationId);
}
