package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AllocationDist {
    @SerializedName("allocationIdx") // 운행id
    @Expose
    public long allocationIdx;

    @SerializedName("chauffeurIdx") // 쇼퍼 id
    @Expose
    public long chauffeurIdx;

    @SerializedName("realDist")     // 총 이동거리
    @Expose
    public long realDist;
}
