package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Roadsale {

    @SerializedName("resvOrgPoi")       // 시작 명칭
    @Expose
    public String resvOrgPoi;

    @SerializedName("resvOrgAddress")   // 시작 주소
    @Expose
    public String resvOrgAddress;

    @SerializedName("resvOrgLat")       // 시작 위도
    @Expose
    public double resvOrgLat;

    @SerializedName("resvOrgLon")       // 시작 경도
    @Expose
    public double resvOrgLon;

    @SerializedName("resvDstPoi")       // 예상 도착지 명칭
    @Expose
    public String resvDstPoi;

    @SerializedName("resvDstLat")       // 예상 도착지 위도
    @Expose
    public double resvDstLat;

    @SerializedName("resvDstLon")       // 예상 도착지 경도
    @Expose
    public double resvDstLon;

    @SerializedName("resvDstAddress")   // 예상 도착지 주소
    @Expose
    public String resvDstAddress;
}
