package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CarInfo implements Serializable {

    @SerializedName("carIdx")
    @Expose
    public long carIdx;

    @SerializedName("carName")
    @Expose
    public String carName;

    @SerializedName("company")
    @Expose
    public String company;

    @SerializedName("carNo")
    @Expose
    public String carNo;

    @SerializedName("carCat")
    @Expose
    public String carCat;

    @SerializedName("useYn")
    @Expose
    public String useYn;

    @SerializedName("regDatetime")
    @Expose
    public long regDatetime;

    @SerializedName("modelName")
    @Expose
    public String modelName;

    @SerializedName("carStatusCat")
    @Expose
    public String carStatusCat;

}
