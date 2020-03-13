package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *  쇼퍼얼굴사진정보, 쇼퍼운전자격증명정보
 */
public class RcpiFileVO implements Serializable {
    /**
     *
     */
    @SerializedName("fileIdx")
    @Expose
    private long fileIdx;

    /**
     *
     */
    @SerializedName("name")
    @Expose
    private String name;

    /**
     *
     */
    @SerializedName("ext")
    @Expose
    private String ext;

    /**
     *
     */
    @SerializedName("regDatetime")
    @Expose
    private long regDatetime;

    /**
     *
     */
    @SerializedName("fileCat")
    @Expose
    private String fileCat;

    /**
     *
     */
    @SerializedName("filePath")
    @Expose
    private String filePath;
}
