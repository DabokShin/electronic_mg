package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *  쇼퍼기본정보
 */
public class RcbiVO implements Serializable {
    /**
     *  등록전화번호
     */
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;

    /**
     *  회사타입(CORPORATE:법인, INDIVIDUAL:개인)
     */
    @SerializedName("companyType")
    @Expose
    public String companyType;

    /**
     *  회사순번
     */
    @SerializedName("companyIdx")
    @Expose
    public long companyIdx;

    /**
     *  회사이름
     */
    @SerializedName("companyName")
    @Expose
    public String companyName;

    /**
     *  사업자등록번호
     */
    @SerializedName("businessNo")
    @Expose
    public String businessNo;

    /**
     *  이름
     */
    @SerializedName("name")
    @Expose
    public String name;

    /**
     *  생년월일
     */
    @SerializedName("birth")
    @Expose
    public String birth;

    /**
     *  영업지역 시도코드
     */
    @SerializedName("signupAreaCd")
    @Expose
    public String signupAreaCd;

    /**
     *  영업지역 시군구코드
     */
    @SerializedName("signupAreaDescriptionCd")
    @Expose
    public String signupAreaDescriptionCd;

    /**
     * 가입 경로 카테고리(HOMEPAGE:홈페이지 광고,AGENCY:대리점추천,CHAUFFEUR:쇼퍼추천,ETC:기타)
     */
    @SerializedName("signupPathCat")
    @Expose
    public String signupPathCat;

    /**
     * 가입 추천 지역 코드(공통코드의 시도코드 사용, 미사용시 NULL)
     */
    @SerializedName("signupRecmdAreaCd")
    @Expose
    public String signupRecmdAreaCd;

    /**
     * 가입 추천 기타 내용
     */
    @SerializedName("signupRecmdEtcContents")
    @Expose
    public String signupRecmdEtcContents;
}
