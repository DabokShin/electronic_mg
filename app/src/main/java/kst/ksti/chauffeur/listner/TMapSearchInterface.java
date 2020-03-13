package kst.ksti.chauffeur.listner;

import java.util.ArrayList;

import kst.ksti.chauffeur.common.SearchEntity;

public interface TMapSearchInterface {
    void onReturn(ArrayList<SearchEntity> searchEntities);
}
