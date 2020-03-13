package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppInfo implements Serializable {
    @SerializedName("confIdx")
    @Expose
    public String confIdx;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("confValue")
    @Expose
    public String confValue;  //앱버전

    @SerializedName("contents")
    @Expose
    public String contents;

    @SerializedName("confCat")
    @Expose
   public String confCat;

    @SerializedName("regDatetime")
    @Expose
    public String regDatetime;

    @SerializedName("useYn")
    @Expose
    public  String useYn;

}
