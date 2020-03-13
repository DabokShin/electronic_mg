package kst.ksti.chauffeur.listner;

import kst.ksti.chauffeur.model.StartRoadsale;

public interface TMapTimeMachineCallback {
    void onSuccess(StartRoadsale roadsale);
    void onFailure();
}
