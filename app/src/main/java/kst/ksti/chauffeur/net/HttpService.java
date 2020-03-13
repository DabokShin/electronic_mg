package kst.ksti.chauffeur.net;

import java.util.Map;

import kst.ksti.chauffeur.model.AcceptAllocationVO;
import kst.ksti.chauffeur.model.AllocationCompleted;
import kst.ksti.chauffeur.model.AllocationCompletedOne;
import kst.ksti.chauffeur.model.AllocationDist;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.AppInfo;
import kst.ksti.chauffeur.model.ChauffeurEval;
import kst.ksti.chauffeur.model.ChauffeurInfo;
import kst.ksti.chauffeur.model.CompanyStatusVO;
import kst.ksti.chauffeur.model.CompanyVO;
import kst.ksti.chauffeur.model.EvaluationSum;
import kst.ksti.chauffeur.model.Inoffice;
import kst.ksti.chauffeur.model.RegistPictureVO;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.model.autosearch.TMapSearchInfo;
import kst.ksti.chauffeur.model.PhoneAuthVO;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.model.signup.SignupCodeList;
import kst.ksti.chauffeur.model.signup.SignupCodeListVO;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface HttpService {

    @POST("/api/common/getAppInfo")
    Call<ResponseData<AppInfo>> getAppInfo(@Body Map<String, Object> params);

    @POST("/api/chauffeur/getChauffeur")
    Call<ResponseData<ChauffeurInfo>> getChauffeur(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/auth/login")
    Call<ResponseData<ChauffeurInfo>> login(@Body Map<String, Object> params);

    @POST("/api/auth/logout")
    Call<ResponseData<Object>> logout(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/chauffeur/changeChauffeurStatus")
    Call<ResponseData<Object>> sendChauffeurStatus(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/chauffeur/changeCarStatus")
    Call<ResponseData<Object>> informCarAccident(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocationList")
    Call<ResponseData<AllocationSchedule>> getAllocSchedule(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocationMainList")
    Call<ResponseData<AllocationSchedule>> getAllocationMainList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocationListWithPage")
    Call<ResponseData<AllocationSchedule>> getAllocScheduleWithPage(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocationCompletedList")
    Call<ResponseData<AllocationCompleted>> getAllocationCompletedList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocationCompletedOne")
    Call<ResponseData<AllocationCompletedOne>> getAllocationCompletedOne(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/getAllocation")
    Call<ResponseData<AllocationSchedule>>  getAllocation(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/changeAllocationStatus")
    Call<ResponseData<Object>> changeAllocStatus(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @GET
    Call<ResponseData<Object>> sendLocationInfo(@Url String url,
                                                   @Query("id") String id,
                                                   @Query("valid") String valid,
                                                   @Query("lat") double lat,
                                                   @Query("lon") double lon,
                                                   @Query("timestamp") String timestamp,
                                                   @Query("hdop") double hdop,
                                                   @Query("altitude") double altitude,
                                                   @Query("speed") double speed,
                                                   @Query("heading") double heading,
                                                   @Query("accuracy") double accuracy,
                                                   @Query("batt") double batt,
                                                   @Query("ttt") long tmp);

    @POST("/api/reservation/changeFareOffline")
    Call<ResponseData<Object>> changeFareCatOffline(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/reservation/confirmOfflinePay")
    Call<ResponseData<Object>> confirmOfflinePay(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("api/reservation/cardPayment")
    Call<ResponseData<Object>> requestCardPay(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/evaluation/getEvaluationList")
    Call<ResponseData<ChauffeurEval>> getChauffeurFeedback( @Body Map<String, Object> params, @Header("Authorization") String auth );

    @POST("/api/evaluation/getEvaluationSum")
    Call<ResponseData<EvaluationSum>> getEvaluationSum(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @GET("https://api2.sktelecom.com/tmap/pois?version=1&searchType=all&resCoordType=WGS84GEO&format=json&count=20")
    Call<TMapSearchInfo> getTMap(@Query("appKey") String appKey, @Query("searchKeyword") String searchKeyword);

    @POST("/api/roadsale/startRoadsale")
    Call<ResponseData<StartRoadsale>> sendStartRoadsale(@Body Map<String, Object> params, @Header("Authorization") String auth);

    @POST("/api/roadsale/endRoadsale")
    Call<ResponseData<Object>> sendCompleteRoadsale(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 출근 api
    @POST("/api/chauffeur/inoffice")
    Call<ResponseData<Inoffice>> sendInoffice(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 수락 배차 상세
    @POST("/api/acceptAllocation/getAcceptAllocation")
    Call<ResponseData<AllocationSchedule>>  getAcceptAllocation(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 수락 배차 수락
    @POST("/api/acceptAllocation/acceptAllocation")
    Call<ResponseData<AcceptAllocationVO>>  acceptAllocation(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 수락 배차 거절
    @POST("/api/acceptAllocation/rejectAllocation")
    Call<ResponseData<AcceptAllocationVO>>  rejectAllocation(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 운행 이동거리
    @POST("/api/reservation/getAllocationRealDist")
    Call<ResponseData<AllocationDist>>  getAllocationRealDist(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 쇼퍼 인증번호 요청
    @POST("/api/common/getAuthNoForChauffeur")
    Call<ResponseData<PhoneAuthVO>> requestChauffeurPhoneAuth(@Body Map<String, Object> params);

    // 쇼퍼 인증번호 체크
    @POST("/api/common/checkAuthNoForChauffeur")
    Call<ResponseData<PhoneAuthVO>> checkChauffeurPhoneAuth(@Body Map<String, Object> params);

    // 법인 인증번호 요청
    @POST("/api/common/getAuthNoForCompany")
    Call<ResponseData<PhoneAuthVO>> requestCompanyPhoneAuth(@Body Map<String, Object> params);

    // 법인 인증번호 체크
    @POST("/api/common/checkAuthNoForCompany")
    Call<ResponseData<PhoneAuthVO>> checkCompanyPhoneAuth(@Body Map<String, Object> params);

    // 쇼퍼 정보 업데이트
    @POST("/api/chauffeur/modifyChauffeur")
    Call<ResponseData<Object>> updateUserInfo(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 사업자 번호 유효성 체크
    @POST("/api/company/checkBusinessNo")
    Call<ResponseData<Object>> checkBusinessNo(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 회사 리스트 가져오기
    @POST("/api/company/getCompanyList")
    Call<ResponseData<CompanyVO>> getCompanyList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 계좌번호 유효성 체크
    @POST("/api/common/checkBankAccount")
    Call<ResponseData<Object>> checkBankAccount(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 법인 등록 신청
    @POST("/api/company/registCompany")
    Call<ResponseData<Object>> requestCorporationRegister(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 공통코드 지역 조회
    @POST("/api/common/getCodeList")
    Call<ResponseData<SignupCodeListVO>> getCodeList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 공통코드 지역 조회
    @POST("/api/common/getCodeAreaList")
    Call<ResponseData<SignupCodeList>> getCodeAreaList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 특정 공통코드 조회
    @POST("/api/common/getCodeSubList")
    Call<ResponseData<SignupCodeList>> getCodeSubList(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 법인 등록 현황
    @POST("/api/company/getRegistCompanyStatus")
    Call<ResponseData<CompanyStatusVO>> getRegistCompanyStatus(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 법인 조회
    @POST("/api/company/getCompany")
    Call<ResponseData<CompanyVO>>  getCompany(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 쇼퍼 기본정보 등록
    @POST("/api/chauffeur/registChauffeurBaseInfo")
    Call<ResponseData<Object>> registChauffeurBaseInfo(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 쇼퍼 개인사업자 부가정보 등록
    @POST("/api/chauffeur/registChauffeurAddInfo")
    Call<ResponseData<Object>> registChauffeurAddInfo(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 이미지 업로드
    @Multipart
    @POST("/api/chauffeur/registChauffeurPicture")
    Call<ResponseData<RegistPictureVO>> uploadImage(@Part MultipartBody.Part file, /*@PartMap Map<String, Object> params,*/@Part("mobileNo") RequestBody phone, @Part("fileCat") RequestBody fileCat, @Header("Authorization") String auth);

    // 쇼퍼 등록 정보 변경
    @POST("/api/chauffeur/registChauffeurStatus")
    Call<ResponseData<Object>>  registChauffeurStatus(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 쇼퍼 등록 정보 조회
    @POST("/api/chauffeur/getRegistChauffeurInfo")
    Call<ResponseData<RegistChauffeurInfoVO>>  getRegistChauffeurInfo(@Body Map<String, Object> params, @Header("Authorization") String auth);

    // 쇼퍼 등록 현황
    @POST("/api/chauffeur/getRegistChauffeurStatus")
    Call<ResponseData<ChauffeurStatusVO>>  getRegistChauffeurStatus(@Body Map<String, Object> params, @Header("Authorization") String auth);
}
