package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AcceptAllocationVO implements Serializable {
    /**
     *  수락배차 성공/실패 코드
     */
    @SerializedName("acceptCode")
    @Expose
    private String acceptCode;

    /**
     *  메시지
     */
    @SerializedName("message")
    @Expose
    private String message;

    public String getAcceptCode() {
        return acceptCode;
    }

    public String getMessage() {
        return message;
    }
}
