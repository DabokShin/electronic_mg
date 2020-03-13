package kst.ksti.chauffeur.model.TMapTimeMachine;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class TmapTimeMachine implements Serializable {

    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("features")
    @Expose
    public ArrayList<Features> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Features> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<Features> features) {
        this.features = features;
    }
}
