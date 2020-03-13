package kst.ksti.chauffeur.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RegistPictureVO implements Serializable {
    @SerializedName("mobileNo")
    @Expose
    @Nullable
    private String mobileNo;

    @SerializedName("fileCat")
    @Expose
    @Nullable
    private String fileCat;

    @SerializedName("imgUrl")
    @Expose
    @Nullable
    private String imgUrl;

    @Nullable
    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(@Nullable String mobileNo) {
        this.mobileNo = mobileNo;
    }

    @Nullable
    public String getFileCat() {
        return fileCat;
    }

    public void setFileCat(@Nullable String fileCat) {
        this.fileCat = fileCat;
    }

    @Nullable
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(@Nullable String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
