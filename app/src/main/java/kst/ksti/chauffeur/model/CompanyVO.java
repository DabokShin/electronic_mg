package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CompanyVO implements Serializable {

    /**
     *  회사 인덱스
     */
    @SerializedName("companyIdx")
    @Expose
    private long companyIdx;

    /**
     *  회사구분
     */
    @SerializedName("companyType")
    @Expose
    private String companyType;

    /**
     *  회사이름
     */
    @SerializedName("name")
    @Expose
    private String name;

    /**
     *
     */
    @SerializedName("businessNo")
    @Expose
    private String businessNo;

    /**
     *
     */
    @SerializedName("corporationNo")
    @Expose
    private String corporationNo;

    /**
     *
     */
    @SerializedName("phoneNo")
    @Expose
    private String phoneNo;

    /**
     *
     */
    @SerializedName("address1")
    @Expose
    private String address1;

    /**
     *
     */
    @SerializedName("address2")
    @Expose
    private String address2;

    /**
     *
     */
    @SerializedName("postNo")
    @Expose
    private String postNo;

    /**
     *
     */
    @SerializedName("regDatetime")
    @Expose
    private long regDatetime;

    /**
     *
     */
    @SerializedName("fareSmtrate")
    @Expose
    private String fareSmtrate;

    /**
     *
     */
    @SerializedName("serviceSmtrate")
    @Expose
    private String serviceSmtrate;

    /**
     *
     */
    @SerializedName("presidentName")
    @Expose
    private String presidentName;

    /**
     *
     */
    @SerializedName("contBeginDate")
    @Expose
    private long contBeginDate;

    /**
     *
     */
    @SerializedName("contEndDate")
    @Expose
    private long contEndDate;

    /**
     *
     */
    @SerializedName("svcAreaIdx")
    @Expose
    private long svcAreaIdx;

    /**
     * 서비스 구분(DIRECTMNG:직영, PARTNER:가맹)
     */
    @SerializedName("svcType")
    @Expose
    private String svcType;

    /**
     *
     */
    @SerializedName("assosiationCd")
    @Expose
    private String assosiationCd;

    /**
     *
     */
    @SerializedName("useYn")
    @Expose
    private String useYn;

    @SerializedName("carCat")
    @Expose
    private String carCat;

    @SerializedName("svcStatus")
    @Expose
    private String svcStatus;

    /**
     * 지역(시도)
     */
    @SerializedName("signupAreaCd")
    @Expose
    private String signupAreaCd;

    /**
     * 지역(시군구)
     */
    @SerializedName("signupAreaDescriptionCd")
    @Expose
    private String signupAreaDescriptionCd;

    /**
     * 면허보유수량
     */
    @SerializedName("licenseRetentionQuanty")
    @Expose
    private String licenseRetentionQuanty;

    /**
     * 등록 전화번호
     */
    @SerializedName("regPhoneno")
    @Expose
    private String regPhoneno;

    /**
     * 지역명(시도)
     */
    @SerializedName("signupAreaName")
    @Expose
    private String signupAreaName;

    /**
     * 지역명(시군구)
     */
    @SerializedName("signupAreaDescriptionName")
    @Expose
    private String signupAreaDescriptionName;

    /**
     * 차종명(중형,대형,모범,블랙)
     */
    @SerializedName("carCatName")
    @Expose
    private String carCatName;

    /**
     * 은행코드
     */
    @SerializedName("bankCd")
    @Expose
    private String bankCd;

    /**
     * 은행명
     */
    @SerializedName("bankName")
    @Expose
    private String bankName;

    /**
     * 예금주
     */
    @SerializedName("depositor")
    @Expose
    private String depositor;

    /**
     * 계좌번호
     */
    @SerializedName("acntNo")
    @Expose
    private String acntNo;

    /**
     * 가입 경로 카테고리(HOMEPAGE:홈페이지 광고,AGENCY:대리점추천,CHAUFFEUR:쇼퍼추천,ETC:기타)
     */
    @SerializedName("signupPathCat")
    @Expose
    private String signupPathCat;

    /**
     * 가입 추천 지역 코드(공통코드의 시도코드 사용, 미사용시 NULL)
     */
    @SerializedName("signupRecmdAreaCd")
    @Expose
    private String signupRecmdAreaCd;

    /**
     * 가입 추천 기타 내용
     */
    @SerializedName("signupRecmdEtcContents")
    @Expose
    private String signupRecmdEtcContents;

    public long getCompanyIdx() {
        return companyIdx;
    }

    public String getCompanyType() {
        return companyType;
    }

    public String getName() {
        return name;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public String getCorporationNo() {
        return corporationNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getPostNo() {
        return postNo;
    }

    public long getRegDatetime() {
        return regDatetime;
    }

    public String getFareSmtrate() {
        return fareSmtrate;
    }

    public String getServiceSmtrate() {
        return serviceSmtrate;
    }

    public String getPresidentName() {
        return presidentName;
    }

    public long getContBeginDate() {
        return contBeginDate;
    }

    public long getContEndDate() {
        return contEndDate;
    }

    public long getSvcAreaIdx() {
        return svcAreaIdx;
    }

    public String getSvcType() {
        return svcType;
    }

    public String getAssosiationCd() {
        return assosiationCd;
    }

    public String getUseYn() {
        return useYn;
    }

    public String getCarCat() {
        return carCat;
    }

    public String getSvcStatus() {
        return svcStatus;
    }

    public String getSignupAreaCd() {
        return signupAreaCd;
    }

    public String getSignupAreaDescriptionCd() {
        return signupAreaDescriptionCd;
    }

    public String getLicenseRetentionQuanty() {
        return licenseRetentionQuanty;
    }

    public String getRegPhoneno() {
        return regPhoneno;
    }

    public String getSignupAreaName() {
        return signupAreaName;
    }

    public String getSignupAreaDescriptionName() {
        return signupAreaDescriptionName;
    }

    public String getCarCatName() {
        return carCatName;
    }

    public String getBankCd() {
        return bankCd;
    }

    public String getBankName() {
        return bankName;
    }

    public String getDepositor() {
        return depositor;
    }

    public String getAcntNo() {
        return acntNo;
    }

    public String getSignupPathCat() {
        return signupPathCat;
    }

    public String getSignupRecmdAreaCd() {
        return signupRecmdAreaCd;
    }

    public String getSignupRecmdEtcContents() {
        return signupRecmdEtcContents;
    }
}
