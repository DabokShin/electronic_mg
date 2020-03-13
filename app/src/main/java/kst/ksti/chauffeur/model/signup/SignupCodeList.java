package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SignupCodeList implements Serializable {
    /**
     *  코드
     */
    @SerializedName("cd")
    @Expose
    private String cd;

    /**
     *  코드 이름
     */
    @SerializedName("cdName")
    @Expose
    private String cdName;

    /**
     *
     */
    @SerializedName("description")
    @Expose
    private String description;
    /**
     *
     */
    @SerializedName("dispOrd")
    @Expose
    private int dispOrd;
    /**
     *
     */
    @SerializedName("extCd1")
    @Expose
    private String extCd1;
    /**
     *
     */
    @SerializedName("extCd2")
    @Expose
    private String extCd2;
    /**
     *
     */
    @SerializedName("extCd3")
    @Expose
    private String extCd3;
    /**
     *
     */
    @SerializedName("extCd4")
    @Expose
    private String extCd4;
    /**
     *
     */
    @SerializedName("extCd5")
    @Expose
    private String extCd5;
    /**
     *
     */
    @SerializedName("useYn")
    @Expose
    private String useYn;
    /**
     *
     */
    @SerializedName("regDatetime")
    @Expose
    private long regDatetime;
    /**
     *
     */
    @SerializedName("updateDatetime")
    @Expose
    private long updateDatetime;
    /**
     *
     */
    @SerializedName("regId")
    @Expose
    private String regId;
    /**
     *
     */
    @SerializedName("updateId")
    @Expose
    private String updateId;
    /**
     *
     */
    @SerializedName("groupIdx")
    @Expose
    private long groupIdx;

    public String getCd() { return cd; }

    public String getCdName() { return cdName; }

    public String getExtCd1() { return extCd1; }

    public String getExtCd2() { return extCd2; }
}
