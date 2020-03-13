package kst.ksti.chauffeur.model.signup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *  쇼퍼얼굴사진정보, 쇼퍼운전자격증명정보
 */
public class RcpiVO implements Serializable {
    /**
     *  등록전화번호
     */
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;

    /**
     *  파일 카테고리(CHAUFFEUR : 얼굴 사진, LICENSE : 택시 운전 자격증명)
     */
    @SerializedName("fileCat")
    @Expose
    private String fileCat;

    /**
     *  업로드 이미지 파일
     */
    @SerializedName("fileVo")
    @Expose
    private RcpiFileVO fileVo;

    /**
     *  업로드 이미지 파일
     */
    @SerializedName("imgUrl")
    @Expose
    private String imgUrl;

    public String getImgUrl() { return imgUrl; }
}
