package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class AllocationCompletedOne implements Serializable {

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

    @SerializedName("resvOrgLat")
    @Expose
    public double resvOrgLat;

    @SerializedName("resvOrgLon")
    @Expose
    public double resvOrgLon;

    @SerializedName("resvDstPoi") //예상도착지
    @Expose
    public String resvDstPoi;

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
    public long estmTime;

    @SerializedName("estmDist")
    @Expose
    public Long estmDist;

    @SerializedName("estmTaxiFare")
    @Expose
    public String estmTaxiFare;

    @SerializedName("estmTotalCost")
    @Expose
    public String estmTotalCost;

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

    @SerializedName("resvCost")
    @Expose
    public String resvCost;

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

    @SerializedName("serviceNameList")
    @Expose
    public ArrayList<String> serviceNameList = new ArrayList<>(); //부가서비스 목록 리스트

    @SerializedName("usrCardIdx")
    @Expose
    public long usrCardIdx;

    @SerializedName("realOrgPoi")
    @Expose
    public String realOrgPoi;

    @SerializedName("realOrgLat")
    @Expose
    public double realOrgLat;

    @SerializedName("realOrgLon")
    @Expose
    public double realOrgLon;

    @SerializedName("realDstPoi")
    @Expose
    public String realDstPoi;

    @SerializedName("realDstLat")
    @Expose
    public double realDstLat;

    @SerializedName("realDstLon")
    @Expose
    public double realDstLon;

    @SerializedName("couponPubIdx")
    @Expose
    public long couponPubIdx;

    @SerializedName("cancelReasonCat")
    @Expose
    public String cancelReasonCat;

    @SerializedName("allocationStatusToString")
    @Expose
    public String allocationStatusToString;

    @SerializedName("poi")
    @Expose
    public String poi;

    @SerializedName("lat")
    @Expose
    public double lat;

    @SerializedName("lon")
    @Expose
    public double lon;

    @SerializedName("serviceCount")
    @Expose
    public Integer serviceCount;

    @SerializedName("doneYn")
    @Expose
    public String doneYn;

    @SerializedName("realDist")
    @Expose
    public Integer realDist;

    @SerializedName("cardNo")
    @Expose
    public String cardNo;

    @SerializedName("cardCat")
    @Expose
    public String cardCat;

    @SerializedName("cardCompanyName")
    @Expose
    public String cardCompanyName;

    @SerializedName("couponName")
    @Expose
    public String couponName;

    @SerializedName("couponContents")
    @Expose
    public String couponContents;

    @SerializedName("couponCat")
    @Expose
    public String couponCat;

    @SerializedName("discountRate")
    @Expose
    public Long discountRate;

    @SerializedName("discountAmt")
    @Expose
    public Integer discountAmt;

    @SerializedName("couponCd")
    @Expose
    public String couponCd;

    @SerializedName("couponBeginDatetime")
    @Expose
    public long couponBeginDatetime;

    @SerializedName("couponEndDatetime")
    @Expose
    public long couponEndDatetime;

    @SerializedName("couponAmt")
    @Expose
    public Integer couponAmt;

    // 예약비 취소 여부
    @SerializedName("resvCostCancelYn")
    @Expose
    public String resvCostCancelYn;

    // 부가서비스 총 비용
    @SerializedName("resvServiceAmt")
    @Expose
    public String resvServiceAmt;

    // 결제 총금액
    @SerializedName("totalCost")
    @Expose
    public String totalCost;

    @SerializedName("serviceList")
    @Expose
    public ArrayList<ServiceList> serviceList = new ArrayList<>(); //부가서비스 목록 리스트

    @SerializedName("realOrgAddress")
    @Expose
    public String realOrgAddress;

    @SerializedName("realDstAddress")
    @Expose
    public String realDstAddress;

    @SerializedName("resvOrgAddress")
    @Expose
    public String resvOrgAddress;

    @SerializedName("resvDstAddress")
    @Expose
    public String resvDstAddress;

    @SerializedName("carInfo")
    @Expose
    public CarInfo carInfo;

    @SerializedName("chauffeurInfo")
    @Expose
    public ChauffeurInfo chauffeurInfo;

}
