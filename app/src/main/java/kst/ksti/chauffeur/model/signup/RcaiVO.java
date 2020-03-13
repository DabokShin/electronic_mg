package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *  쇼퍼부가정보
 */
public class RcaiVO implements Serializable {
    /**
     *  등록전화번호
     */
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;

    /**
     *  은행코드
     */
    @SerializedName("bankCd")
    @Expose
    public String bankCd;

    /**
     *  예금주명
     */
    @SerializedName("depositor")
    @Expose
    public String depositor;

    /**
     *  계좌번호
     */
    @SerializedName("acntNo")
    @Expose
    public String acntNo;

    /**
     *  차량제조사코드
     */
    @SerializedName("autmakeCd")
    @Expose
    public String autmakeCd;

    /**
     *  차량모델코드
     */
    @SerializedName("carmodlCd")
    @Expose
    public String carmodlCd;

    /**
     *  차량제조사이름
     */
    @SerializedName("autmakeName")
    @Expose
    public String autmakeName;

    /**
     *  차량모델이름
     */
    @SerializedName("carmodlName")
    @Expose
    public String carmodlName;

    /**
     *  차량번호
     */
    @SerializedName("carNo")
    @Expose
    public String carNo;

    /**
     *  차량유형(MIDSIZE:중형, FULLSIZE:대형, MOBUM:모범, BLACK:블랙)
     */
    @SerializedName("carCat")
    @Expose
    public String carCat;
}
