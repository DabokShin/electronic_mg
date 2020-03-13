package kst.ksti.chauffeur.listner;

import java.util.HashMap;

public interface ReverseGeocodingInterface {
    void onSuccess(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface);
    void onError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface, String errorMsg);
    void onGpsError(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface);
}