package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EvaluationSum {

    @SerializedName("likeYn")
    @Expose
    public String likeYn ;

    @SerializedName("likeYnCount")
    @Expose
    public int likeYnCount ;

}
