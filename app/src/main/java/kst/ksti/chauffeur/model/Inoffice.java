package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Inoffice {

    @SerializedName("carNo")
    @Expose
    public String carNo;

    @SerializedName("chauffeurStatusCat")
    @Expose
    public String chauffeurStatusCat;

    @SerializedName("allocation")
    @Expose
    public AllocationSchedule allocation;

    @SerializedName("roadsale")
    @Expose
    public StartRoadsale roadsale;

}
