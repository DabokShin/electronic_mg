package kst.ksti.chauffeur.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhoneAuthVO implements Serializable {
    @SerializedName("macaronCdIdx")
    @Expose
    private int macaronCdIdx;

    @SerializedName("cd")
    @Expose
    @Nullable
    private String cd;

    @SerializedName("cdCat")
    @Expose
    private String cdCat;

    @SerializedName("useYn")
    @Expose
    private String useYn;

    @SerializedName("regDatetime")
    @Expose
    @Nullable
    private String regDatetime;

    @SerializedName("receivePhoneNo")
    @Expose
    @Nullable
    private String receivePhoneNo;

    public int getMacaronCdIdx() {
        return macaronCdIdx;
    }

    public void setMacaronCdIdx(int macaronCdIdx) {
        this.macaronCdIdx = macaronCdIdx;
    }

    @Nullable
    public String getCd() {
        return cd;
    }

    public void setCd(@Nullable String cd) {
        this.cd = cd;
    }

    public String getCdCat() {
        return cdCat;
    }

    public void setCdCat(String cdCat) {
        this.cdCat = cdCat;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    @Nullable
    public String getRegDatetime() {
        return regDatetime;
    }

    public void setRegDatetime(@Nullable String regDatetime) {
        this.regDatetime = regDatetime;
    }

    @Nullable
    public String getReceivePhoneNo() {
        return receivePhoneNo;
    }

    public void setReceivePhoneNo(@Nullable String receivePhoneNo) {
        this.receivePhoneNo = receivePhoneNo;
    }
}
