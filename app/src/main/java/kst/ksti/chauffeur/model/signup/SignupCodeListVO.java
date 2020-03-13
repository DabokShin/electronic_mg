package kst.ksti.chauffeur.model.signup;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class SignupCodeListVO implements Serializable {
    /**
     *  그룹 인덱스
     */
    @SerializedName("groupIdx")
    @Expose
    private long groupIdx;

    /**
     *  그룹 코드
     */
    @SerializedName("groupCd")
    @Expose
    private String groupCd;

    /**
     *
     */
    @SerializedName("groupName")
    @Expose
    private String groupName;

    /**
     *
     */
    @SerializedName("description")
    @Expose
    private String description;

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
    private long regId;

    /**
     *
     */
    @SerializedName("updateId")
    @Expose
    private long updateId;

    /**
     *
     */
    @SerializedName("cd")
    @Expose
    private String cd;

    /**
     *
     */
    @SerializedName("codeList")
    @Expose
    public ArrayList<SignupCodeList> codeList = new ArrayList<>();

    public String getGroupCd() { return groupCd; }

    @Nullable
    public ArrayList<SignupCodeList> getCodeList() {
        return codeList;
    }

    public void setCodeList(@Nullable ArrayList<SignupCodeList> codeList) {
        this.codeList = codeList;
    }
}
