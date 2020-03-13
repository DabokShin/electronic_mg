package kst.ksti.chauffeur.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeedBack {

    @SerializedName("chauffeurIdx")
    @Expose
    public long chauffeurIdx ;

    @SerializedName("likeYn")
    @Expose
    public String likeYn ;

    @SerializedName("feedContents")
    @Expose
    public String feedContents ;

    @SerializedName("feedCat")
    @Expose
    public String feedCat ;


}
