package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RegistChauffeurInfoVO implements Serializable {
    /**
     *  등록전화번호
     */
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;

    /**
     *  쇼퍼등록정보(MEMBER:기본정보,PROFILE:프로필정보,ETC:부가정보)
     */
    @SerializedName("chauffeurRegInfoCat")
    @Expose
    private String chauffeurRegInfoCat;

    /**
     *  쇼퍼기본정보
     */
    @SerializedName("rcbi")
    @Expose
    private RcbiVO rcbi;

    /**
     *  쇼퍼얼굴사진정보
     */
    @SerializedName("rcpi")
    @Expose
    private RcpiVO rcpi;

    /**
     *  쇼퍼운전자격증명정보
     */
    @SerializedName("rcpi2")
    @Expose
    private RcpiVO rcpi2;

    /**
     *  쇼퍼부가정보
     */
    @SerializedName("rcai")
    @Expose
    private RcaiVO rcai;

    /**
     *  쇼퍼가입시 개인/법인 타입 (INDIVIDUAL/CORPORATE)
     */
    @SerializedName("companyType")
    @Expose
    private String companyType;

    public String getChauffeurRegInfoCat() { return chauffeurRegInfoCat; }

    public RcbiVO getRcbi() { return rcbi; }

    public RcpiVO getRcpi() { return rcpi; }

    public RcpiVO getRcpi2() { return rcpi2; }

    public RcaiVO getRcai() { return rcai; }

    public String getCompanyType() { return companyType; }
}
