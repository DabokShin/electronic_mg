package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChauffeurEval implements Serializable {

    @SerializedName("cd")
    @Expose
    public String cd ;

    @SerializedName("cdName")
    @Expose
    public String cdName ;

    @SerializedName("likeYn")
    @Expose
    public String likeYn ;

    @SerializedName("cdCount")
    @Expose
    public String cdCount ;

    @SerializedName("likeYnCount")
    @Expose
    public String likeYnCount ;


}
