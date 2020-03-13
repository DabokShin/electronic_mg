package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AllocationCompletedPage implements Serializable {
    @SerializedName("limit")
    @Expose
    public int limit;

    @SerializedName("page")
    @Expose
    public int page;

    @SerializedName("totalCount")
    @Expose
    public int totalCount;

    @SerializedName("offset")
    @Expose
    public int offset;

    @SerializedName("startRow")
    @Expose
    public int startRow;

    @SerializedName("hasNextPage")
    @Expose
    public boolean hasNextPage;

    @SerializedName("endRow")
    @Expose
    public int endRow;

    @SerializedName("hasPrePage")
    @Expose
    public boolean hasPrePage;

    @SerializedName("firstPage")
    @Expose
    public boolean firstPage;

    @SerializedName("prePage")
    @Expose
    public int prePage;

    @SerializedName("totalPages")
    @Expose
    public int totalPages;

    @SerializedName("nextPage")
    @Expose
    public int nextPage;

    @SerializedName("lastPage")
    @Expose
    public boolean lastPage;

}
