package kst.ksti.chauffeur.listner;

import kst.ksti.chauffeur.net.ResponseData;

public interface ChangeStatusInterface {
    void onSuccess(ResponseData<Object> response);
    void onErrorCode(ResponseData<Object> response);
    void onError();
    void onFailed(Throwable t);
}
