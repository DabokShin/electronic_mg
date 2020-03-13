package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChauffeurStatusVO implements Serializable {
    @SerializedName("regPhoneno")
    @Expose
    private String regPhoneno;

    @SerializedName("companyIdx")
    @Expose
    private int companyIdx;

    // 서비스 상태 (APPRWAIT:승인대기,AVAILABLE:이용가능,BYE:탈퇴,REQUEST:승인요청:,REREQUEST:재승인요청:,APPROVED:승인완료,INFORJCT:정보승인보류,CONTRJCT:걔약승인보류)
    @SerializedName("svcStatus")
    @Expose
    private String svcStatus;

    // 쇼퍼 등록정보 카테고리(MEMBER:기본정보, PROFILE:프로필정보, ETC:부가정보)
    // 2가지 이상 일때는 왼쪽 순서로 내려온다.
    @SerializedName("chauffeurRegInfoCat")
    @Expose
    private String chauffeurRegInfoCat;

    @SerializedName("chauffeurRjctInfoList")
    @Expose
    private ArrayList<String> chauffeurRjctInfoList;

    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("cart")
    @Expose
    private ChauffeurStatusVO.Cart cart;

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

    public String getChauffeurRegInfoCat() {
        return chauffeurRegInfoCat;
    }

    public ArrayList<String> getChauffeurRjctInfoList() {
        return chauffeurRjctInfoList;
    }

    public String getReason() { return reason; }

    public void setSvcStatus(String svcStatus) {
        this.svcStatus = svcStatus;
    }

    public String getId() {
        return id;
    }

    public ChauffeurStatusVO.Cart getCart() {
        return cart;
    }

    public void setCart(ChauffeurStatusVO.Cart cart) {
        this.cart = cart;
    }

    public List<ChauffeurStatusVO.CitList> getCitList() {
        return citList;
    }

    public void setCitList(List<ChauffeurStatusVO.CitList> citList) {
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
        @SerializedName("chauffeurIncorrectinfoCat")
        @Expose
        private String chauffeurIncorrectinfoCat;

        @SerializedName("chauffeurIncorrectinfoText")
        @Expose
        private String chauffeurIncorrectinfoText;

        public String getChauffeurIncorrectinfoCat() {
            return chauffeurIncorrectinfoCat;
        }

        public String getChauffeurIncorrectinfoText() { return chauffeurIncorrectinfoText; }
    }
}
