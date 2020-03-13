package kst.ksti.chauffeur.model;


import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChauffeurInfo implements Serializable {

  @SerializedName("chauffeurIdx")
  @Expose
  public long chauffeurIdx ;

  @SerializedName("carIdx")
  @Expose
  public long carIdx;

  @SerializedName("name")
  @Expose
  public  String name;

  @SerializedName("mobileNo")
  @Expose
  public  String mobileNo ;

  @SerializedName("regDatetime")
  @Expose
  public long regDatetime;

  @SerializedName("useYn")
  @Expose
  public String useYn ;

  @SerializedName("appOs")
  @Expose
  public String appOs;

  @SerializedName("appTokenTemp")
  @Expose
  public String appTokenTemp;

  @SerializedName("imgUrl")
  @Expose
  public String imgUrl;


  @SerializedName("appVersion")
  @Expose
  public String appVersion;

  @SerializedName("email")
  @Expose
  public String email;

  @SerializedName("pwd")
  @Expose
  public String pwd;

  @SerializedName("chauffeurStatusCat")
  @Expose
  public String chauffeurStatusCat ;

  @SerializedName("id")
  @Expose
  public String id;

  @SerializedName("caridx")
  @Expose
  public String mobileManufacturer;

  @SerializedName("mobileModel")
  @Expose
  public String mobileModel;

  @SerializedName("adsId")
  @Expose
  public String adsId;

  @SerializedName("fileIdx")
  @Expose
  public String fileIdx;

  @SerializedName("safePhoneno")
  @Expose
  public String safePhoneno;

  //public AppDef.ChauffeurStatus status;
  @SerializedName("carVo")
  @Expose
  public CarInfo carVo = new CarInfo();

  @SerializedName("accessToken")
  @Expose
  public String accessToken = "";

  @SerializedName("tdcsIntervalMS")
  @Expose
  public int tdcsIntervalMS;

  @SerializedName("companyIdx")
  @Expose
  public long companyIdx;

  @SerializedName("companyName")
  @Expose
  public String companyName;

  @SerializedName("companyPhoneNo")
  @Expose
  public String companyPhoneNo;

  @SerializedName("companyType")
  @Expose
  @Nullable
  public String companyType;

  @SerializedName("bankCd")
  @Expose
  public String bankCd;

  @SerializedName("bankName")
  @Expose
  public String bankName;

  @SerializedName("acntNo")
  @Expose
  public String acntNo;

  @SerializedName("depositor")
  @Expose
  public String depositor;

  @SerializedName("companyVo")
  @Expose
  public CompanyVO companyVo;
}
