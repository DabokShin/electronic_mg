package kst.ksti.chauffeur.listner;

import java.util.HashMap;

public interface ReverseGeocodingInterfaceInoffice {
    void onSuccess(HashMap<String, Object> params, ResultInoffice resultInoffice);
    void onError(HashMap<String, Object> params, ResultInoffice resultInoffice, String errorMsg);
    void onGpsError(HashMap<String, Object> params, ResultInoffice resultInoffice);
}
