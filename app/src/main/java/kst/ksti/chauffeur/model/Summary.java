package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Summary {

    @SerializedName("totalCount")
    @Expose
    public int totalCount;              // 예약 총 건수

    @SerializedName("attemptedCount")
    @Expose
    public int attemptedCount;          // 미수행 건수

}
