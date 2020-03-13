package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ServiceList implements Serializable {

    @SerializedName("serviceIdx")
    @Expose
    public int serviceIdx;

    @SerializedName("allocationIdx")
    @Expose
    public int allocationIdx;

    @SerializedName("regDatetime")
    @Expose
    public long regDatetime;

    @SerializedName("realCost")
    @Expose
    public int realCost;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("contents")
    @Expose
    public String contents;

    // 취소여부
    @SerializedName("cancelYn")
    @Expose
    public String cancelYn;
}
