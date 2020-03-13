package kst.ksti.chauffeur.listner;

import kst.ksti.chauffeur.model.Inoffice;
import kst.ksti.chauffeur.net.ResponseData;

public interface ResultInoffice {
    void onSuccess(ResponseData<Inoffice> response);
    void onErrorCode(ResponseData<Inoffice> response);
    void onError();
    void onFailed(Throwable t);
}
