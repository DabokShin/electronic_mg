package kst.ksti.chauffeur.common;

import kst.ksti.chauffeur.model.autosearch.Poi;

/**
 * Created by KJH on 2017-11-07.
 */

public class SearchEntity {
    private String title;
    private String address;
    private Poi poi;

    public SearchEntity(String title, String address, Poi poi) {
        this.title = title;
        this.address = address;
        this.poi = poi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Poi getPoi() {
        return poi;
    }
}
