package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class AllocationSchedule implements Serializable {

    @SerializedName("paging")
    @Expose
    public AllocationCompletedPage paging;

    @SerializedName("summary")
    @Expose
    public Summary summaryData;

    @SerializedName("allocationIdx")
    @Expose
    public long allocationIdx;

    @SerializedName("usrIdx")
    @Expose
    public long usrIdx;

    @SerializedName("chauffeurIdx")
    @Expose
    public long chauffeurIdx;

    @SerializedName("carIdx")
    @Expose
    public long carIdx;

    @SerializedName("allocationStatus")
    @Expose
    public String allocationStatus;

    @SerializedName("otherYn")
    @Expose
    public String otherYn;    //불러주기 여부

    @SerializedName("otherName")
    @Expose
    public String otherName;

    @SerializedName("otherMobileNo")
    @Expose
    public String otherMobileNo;

    @SerializedName("resvOrgPoi")
    @Expose
    public String resvOrgPoi;

    @SerializedName("resvOrgAddress")
    @Expose
    public String resvOrgAddress;

    @SerializedName("resvOrgLat")
    @Expose
    public double resvOrgLat;

    @SerializedName("resvOrgLon")
    @Expose
    public double resvOrgLon;

    @SerializedName("resvDstPoi") //예상도착지
    @Expose
    public String resvDstPoi;

    @SerializedName("resvDstAddress") //예상도착지
    @Expose
    public String resvDstAddress;

    @SerializedName("resvDstLat")
    @Expose
    public double resvDstLat;

    @SerializedName("resvDstLon")
    @Expose
    public double resvDstLon;

    @SerializedName("resvDatetime")
    @Expose
    public long resvDatetime;

    @SerializedName("resvCarCat")
    @Expose
    public String resvCarCat;

    @SerializedName("estmTime")
    @Expose
    public long estmTime;           // 예상시간(초)

    @SerializedName("estmDist")
    @Expose
    public long estmDist;           // 이동거리

    @SerializedName("estmTaxiFare")
    @Expose
    public String estmTaxiFare;

    @SerializedName("estmTotalCost")
    @Expose
    public String estmTotalCost;    // 예상금액

    @SerializedName("realPayAmt")
    @Expose
    public String realPayAmt;

    @SerializedName("estmServiceCost")
    @Expose
    public Integer estmServiceCost;  //null이면 부가서비스도 null

    @SerializedName("fareCat")
    @Expose
    public String fareCat;

    @SerializedName("likeYn")
    @Expose
    public String likeYn;

    @SerializedName("feedContents")
    @Expose
    public String feedContents;

    @SerializedName("regDatetime")
    @Expose
    public long regDatetime;

    @SerializedName("resvcost")
    @Expose
    public String resvcost;

    @SerializedName("realTaxiFare")
    @Expose
    public String realTaxiFare;

    @SerializedName("departDatetime") //앱에서는 필요없음
    @Expose
    public long departDatetime;

    @SerializedName("arrvDatetime")
    @Expose
    public long arrvDatetime;

    @SerializedName("cancelPersonCat")
    @Expose
    public String cancelPersonCat;

    @SerializedName("penaltyAmt")
    @Expose
    public String penaltyAmt;

    @SerializedName("payStatusCat")
    @Expose
    public String payStatusCat;

    @SerializedName("safePhoneno")  //고객 안심번호
    @Expose
    public String safePhoneno;

    @SerializedName("feedCat")
    @Expose
    public String feedCat;

    @SerializedName("serviceCount")
    @Expose
    public int serviceCount;

    @SerializedName("address")
    @Expose
    public String address;

    @SerializedName("realOrgAddress")
    @Expose
    public String realOrgAddress;

    @SerializedName("realDstAddress")
    @Expose
    public String realDstAddress;

    @SerializedName("unrunYn")
    @Expose
    public String unrunYn;

    @SerializedName("realDist")
    @Expose
    public long realDist;       // 실제 이동거리

    @SerializedName("serviceNameList")
    @Expose
    public ArrayList<String> serviceNameList = new ArrayList<>(); //부가서비스 목록 리스트

    public boolean isCompleted = false;

    public AllocationSchedule(){

    }
}

