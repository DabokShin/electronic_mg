package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StartRoadsale implements Serializable {

    @SerializedName("roadSaleIdx")          // 일반영업 순번 : 서버에서 받는 인덱스
    @Expose
    public long roadSaleIdx;

    @SerializedName("chauffeurIdx")
    @Expose
    public long chauffeurIdx;

    @SerializedName("carIdx")
    @Expose
    public long carIdx;

    @SerializedName("resvOrgPoi")
    @Expose
    public String resvOrgPoi;

    @SerializedName("resvOrgLat")
    @Expose
    public double resvOrgLat;

    @SerializedName("resvOrgLon")
    @Expose
    public double resvOrgLon;

    @SerializedName("resvOrgAddress")
    @Expose
    public String resvOrgAddress;

    @SerializedName("resvDstPoi")           // 예상 도착지 명칭
    @Expose
    public String resvDstPoi;

    @SerializedName("resvDstLat")           // 예상 도착지 위도
    @Expose
    public double resvDstLat;

    @SerializedName("resvDstLon")           // 예상 도착지 경도
    @Expose
    public double resvDstLon;

    @SerializedName("resvDstAddress")       // 예상 도착지 주소
    @Expose
    public String resvDstAddress;

    @SerializedName("resvDatetime")
    @Expose
    public long resvDatetime;

    @SerializedName("estmTime")         // 예상 시간
    @Expose
    public Integer estmTime;

    @SerializedName("estmDist")         // 예상 거리
    @Expose
    public Integer estmDist;

    @SerializedName("estmTaxiFare")     // 예상 택시 운임
    @Expose
    public Integer estmTaxiFare;

    @SerializedName("regDatetime")
    @Expose
    public long regDatetime;

    @SerializedName("realTaxiFare")
    @Expose
    public Integer realTaxiFare;

    @SerializedName("departDatetime")
    @Expose
    public long departDatetime;

    @SerializedName("arrvDatetime")
    @Expose
    public long arrvDatetime;

    @SerializedName("payStatusCat")
    @Expose
    public String payStatusCat;

    @SerializedName("realOrgPoi")
    @Expose
    public String realOrgPoi;

    @SerializedName("realOrgLat")
    @Expose
    public double realOrgLat;

    @SerializedName("realOrgLon")
    @Expose
    public double realOrgLon;

    @SerializedName("realOrgAddress")
    @Expose
    public String realOrgAddress;

    @SerializedName("realDstPoi")
    @Expose
    public String realDstPoi;

    @SerializedName("realDstLat")
    @Expose
    public double realDstLat;

    @SerializedName("realDstLon")
    @Expose
    public double realDstLon;

    @SerializedName("realDstAddress")
    @Expose
    public String realDstAddress;

    @SerializedName("realDist")
    @Expose
    public Integer realDist;

    @SerializedName("mobileNo")
    @Expose
    public String mobileNo;

    @SerializedName("poi")              // 시작 명칭
    @Expose
    public String poi;

    @SerializedName("lat")              // 시작 위도
    @Expose
    public double lat;

    @SerializedName("lon")              // 시작 경도
    @Expose
    public double lon;

    @SerializedName("address")          // 시작 주소
    @Expose
    public String address;
}
