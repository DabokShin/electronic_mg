package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EvalSum implements Serializable {

    @SerializedName("likeYn")
    @Expose
    public String likeYn;

    @SerializedName("likeYnCount")
    @Expose
    public String likeYnCount;
}
