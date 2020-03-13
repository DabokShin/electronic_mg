package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CompanyStatusVO implements Serializable {

    @SerializedName("regPhoneno")
    @Expose
    private String regPhoneno;

    @SerializedName("companyIdx")
    @Expose
    private int companyIdx;

    @SerializedName("svcStatus")
    @Expose
    private String svcStatus;

    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("cart")
    @Expose
    private Cart cart;

    @SerializedName("citList")
    @Expose
    private List<CitList> citList;

    public String getRegPhoneno() {
        return regPhoneno;
    }

    public void setRegPhoneno(String regPhoneno) {
        this.regPhoneno = regPhoneno;
    }

    public int getCompanyIdx() {
        return companyIdx;
    }

    public void setCompanyIdx(int companyIdx) {
        this.companyIdx = companyIdx;
    }

    public String getSvcStatus() {
        return svcStatus;
    }

    public void setSvcStatus(String svcStatus) {
        this.svcStatus = svcStatus;
    }

    public String getReason() { return reason; }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<CitList> getCitList() {
        return citList;
    }

    public void setCitList(List<CitList> citList) {
        this.citList = citList;
    }

    private class Cart implements Serializable {
        @SerializedName("rejectTrIdx")
        @Expose
        private int rejectTrIdx;

        @SerializedName("svcStatusTrIdx")
        @Expose
        private int svcStatusTrIdx;

        @SerializedName("corporationRegInfoCat")
        @Expose
        private String corporationRegInfoCat;

        @SerializedName("reason")
        @Expose
        private String reason;

        public int getRejectTrIdx() {
            return rejectTrIdx;
        }

        public void setRejectTrIdx(int rejectTrIdx) {
            this.rejectTrIdx = rejectTrIdx;
        }

        public int getSvcStatusTrIdx() {
            return svcStatusTrIdx;
        }

        public void setSvcStatusTrIdx(int svcStatusTrIdx) {
            this.svcStatusTrIdx = svcStatusTrIdx;
        }

        public String getCorporationRegInfoCat() {
            return corporationRegInfoCat;
        }

        public void setCorporationRegInfoCat(String corporationRegInfoCat) {
            this.corporationRegInfoCat = corporationRegInfoCat;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public class CitList implements Serializable {
        @SerializedName("companyIncorrectinfoTrIdx")
        @Expose
        private int companyIncorrectinfoTrIdx;

        @SerializedName("rejectTrIdx")
        @Expose
        private int rejectTrIdx;

        @SerializedName("companyIncorrectinfoCat")
        @Expose
        private String companyIncorrectinfoCat;

        @SerializedName("companyIncorrectinfoText")
        @Expose
        private String companyIncorrectinfoText;

        public int getCompanyIncorrectinfoTrIdx() {
            return companyIncorrectinfoTrIdx;
        }

        public void setCompanyIncorrectinfoTrIdx(int companyIncorrectinfoTrIdx) {
            this.companyIncorrectinfoTrIdx = companyIncorrectinfoTrIdx;
        }

        public int getRejectTrIdx() {
            return rejectTrIdx;
        }

        public void setRejectTrIdx(int rejectTrIdx) {
            this.rejectTrIdx = rejectTrIdx;
        }

        public String getCompanyIncorrectinfoCat() {
            return companyIncorrectinfoCat;
        }

        public void setCompanyIncorrectinfoCat(String companyIncorrectinfoCat) {
            this.companyIncorrectinfoCat = companyIncorrectinfoCat;
        }

        public String getCompanyIncorrectinfoText() {
            return companyIncorrectinfoText;
        }

        public void setCompanyIncorrectinfoText(String companyIncorrectinfoText) {
            this.companyIncorrectinfoText = companyIncorrectinfoText;
        }
    }
}
