package kst.ksti.chauffeur.net;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
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
import kst.ksti.chauffeur.model.PhoneAuthVO;
import kst.ksti.chauffeur.model.RegistPictureVO;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.model.autosearch.TMapSearchInfo;
import kst.ksti.chauffeur.model.signup.ChauffeurStatusVO;
import kst.ksti.chauffeur.model.signup.RegistChauffeurInfoVO;
import kst.ksti.chauffeur.model.signup.SignupCodeList;
import kst.ksti.chauffeur.model.signup.SignupCodeListVO;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.Util;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataInterface extends BasicDataInterface {
    private static DataInterface instance;
    private String TAG = getClass().getSimpleName();
    private MacaronCustomDialog dialog;

    public interface ResponseCallback<T> {
        void onSuccess(T response);

        void onError(T response);

        void onFailure(Throwable t);
    }

    public static void setDataInterface(DataInterface dataInterface) {
        instance = dataInterface;
    }

    public static DataInterface getInstance() {
        if (instance == null) {
            synchronized (DataInterface.class) {
                if (instance == null) {
                    instance = new DataInterface();
                }
            }
        }

        return instance;
    }

    public static DataInterface getInstance(String url) {
        if (instance == null) {
            synchronized (DataInterface.class) {
                if (instance == null) {
                    instance = new DataInterface(url);
                }
            }
        }

        return instance;
    }

    public DataInterface() {
        super();
    }

    public DataInterface(String url) {
        super(url);
    }

    public static boolean isCallSuccess(Response response) {
        return response.isSuccessful();
    }

    private void showDialog(final Context context, String title, String msg) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new MacaronCustomDialog(context, title, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        UIThread.executeInUIThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void processCommonError(Context context, ResponseCallback callback, Response response, String url) {
        processCommonError(context, callback, response, url, true);
    }

    private void processCommonError(Context context, ResponseCallback callback, Response response, String url, boolean isCommonError) {
        if (callback == null) {
            return;
        }

        ResponseData data = (ResponseData) response.body();

        if (response.isSuccessful()) {
            if (data != null) {
                if (isCommonError && !data.getResultCode().equals("S000")) {
                    if(!data.getResultCode().equals("EC201")) { // EC201 : 예약상세에서 이 코드가 내려오면 그 곳에서 팝업을 새로 만들어야 하기 때문에 EC201 아니면 이 다이얼로그 쓴다
                        showDialog(context, null, data.getError());
                    }
                    setCancelLoadingViewAnimation(context);

                } else if(!data.getResultCode().equals("S000")) {
                    setCancelLoadingViewAnimation(context);
                }

                AnalyticsHelper.getInstance(context).sendEvent(url, "호출 성공", "", Global.FA_EVENT_NAME.API_REQUEST);
                callback.onSuccess(data);

            } else {
                AnalyticsHelper.getInstance(context).sendEvent(url, "호출 실패", "", Global.FA_EVENT_NAME.API_REQUEST);
                callback.onError(null);
            }

        } else {
            AnalyticsHelper.getInstance(context).sendEvent(url, "호출 실패", "", Global.FA_EVENT_NAME.API_REQUEST);

            if (isCommonError) {
                if (data != null) {
                    showDialog(context, null, data.getError());
                } else {
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
                setCancelLoadingViewAnimation(context);

            } else {
                callback.onError(null);
            }
        }
    }

    /**
     * onError()콜백을 넘기지 않을 경우에 직접 로딩뷰를 캔슬시킴
     */
    private void setCancelLoadingViewAnimation(Context context) {
        boolean check = false;
        if(context instanceof BaseActivity) {
            check = true;
        }

        if (check) {
            try {
                ((BaseActivity) context).cancelLoadingViewAnimation();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 버전정보체크
     */
    public void getAppVersion(final Context context, final ResponseCallback callback) {
        try {
            Call<ResponseData<AppInfo>> call = getService().getAppInfo(new HashMap<String, Object>());

            call.enqueue(new Callback<ResponseData<AppInfo>>() {
                @Override
                public void onResponse(Call<ResponseData<AppInfo>> call, Response<ResponseData<AppInfo>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<AppInfo>> call, Throwable t) {
                    if (callback == null) return;

                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    t.printStackTrace();
                    callback.onFailure(t);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 쇼퍼상태변경
     */
    public void sendChauffeurStatus(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().sendChauffeurStatus(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;

                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 배차상태변경
     */
    public void changeAllocationStatus(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().changeAllocStatus(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;

                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 사고접수
     */
    public void informCarAccident(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().informCarAccident(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 예약정보 메인
     */
    public void getAllocationMainList(final Context context, HashMap<String, Object> params, final boolean isCommonError, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<AllocationSchedule>> call = getService().getAllocationMainList(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationSchedule>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationSchedule>> call, Response<ResponseData<AllocationSchedule>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), isCommonError);
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationSchedule>> call, Throwable t) {
                    t.printStackTrace();
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    callback.onFailure(t);

                    if(isCommonError) {
                        showDialog(context, null, "네트웍상태를 확인해 주세요.");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 예약정보
     */
    public void receiveAllocSchedule(final Context context, HashMap<String, Object> params, final boolean isCommonError, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<AllocationSchedule>> call = getService().getAllocScheduleWithPage(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationSchedule>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationSchedule>> call, Response<ResponseData<AllocationSchedule>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), isCommonError);
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationSchedule>> call, Throwable t) {
                    t.printStackTrace();
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    callback.onFailure(t);

                    if(isCommonError) {
                        showDialog(context, null, "네트웍상태를 확인해 주세요.");
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 예약정보상세
     */
    public void getAllocation(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<AllocationSchedule>> call = getService().getAllocation(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationSchedule>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationSchedule>> call, Response<ResponseData<AllocationSchedule>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationSchedule>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 수락 배차 상세
     */
    public void getAcceptAllocation(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<AllocationSchedule>> call = getService().getAcceptAllocation(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationSchedule>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationSchedule>> call, Response<ResponseData<AllocationSchedule>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationSchedule>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 수락 배차 수락 요청
     */
    public void acceptAllocation(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<AcceptAllocationVO>> call = getService().acceptAllocation(params, auth_str);

            call.enqueue(new Callback<ResponseData<AcceptAllocationVO>>() {
                @Override
                public void onResponse(Call<ResponseData<AcceptAllocationVO>> call, Response<ResponseData<AcceptAllocationVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<AcceptAllocationVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 수락 배차 거절 요청
     */
    public void rejectAllocation(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<AcceptAllocationVO>> call = getService().rejectAllocation(params, auth_str);

            call.enqueue(new Callback<ResponseData<AcceptAllocationVO>>() {
                @Override
                public void onResponse(Call<ResponseData<AcceptAllocationVO>> call, Response<ResponseData<AcceptAllocationVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<AcceptAllocationVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 로그인
     */
    public void login(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            Call<ResponseData<ChauffeurInfo>> call = getService().login(params);

            call.enqueue(new Callback<ResponseData<ChauffeurInfo>>() {
                @Override
                public void onResponse(Call<ResponseData<ChauffeurInfo>> call, Response<ResponseData<ChauffeurInfo>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFailure(Call<ResponseData<ChauffeurInfo>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 로그아웃
     */
    public void logout(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().logout(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);
                    if (callback == null) return;

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 크래커로 위치정보 전송
     */
    public void sendLocationInfo(String id, String valid, double lat, double lon, String timestamp, double hdop,
                                 double altitude, double speed, double heading, double accuracy, double batt, long tmp,
                                 final ResponseCallback callback) {
        try {
            Call<ResponseData<Object>> call = getService().sendLocationInfo(Global.getTDCSUrl(), id, valid, lat, lon, timestamp, hdop, altitude, speed, heading, accuracy, batt, tmp);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    if (callback == null) {
                        return;
                    }

                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError(null);
                    }
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;

                    t.printStackTrace();
                    callback.onFailure(t);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 직접결제로_전환
     */
    public void changeFareCatOffline(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().changeFareCatOffline(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 직접결제
     */
    public void confirmOfflinePay(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().confirmOfflinePay(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    /**
     * 요금결제
     */
    public void requestCardPay(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<Object>> call = getService().requestCardPay(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 좋아요,싫어요 리스트
     */
    public void receiveEvaluation(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<ChauffeurEval>> call = getService().getChauffeurFeedback(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<ChauffeurEval>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<ChauffeurEval>> call, Response<ResponseData<ChauffeurEval>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<ChauffeurEval>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 좋아요_싫어요_카운트
     */
    public void getEvaluationSum(final Context context, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;
        try {
            Call<ResponseData<EvaluationSum>> call = getService().getEvaluationSum(new HashMap<String, Object>(), auth_str);

            call.enqueue(new Callback<ResponseData<EvaluationSum>>() {
                @Override
                public void onResponse(Call<ResponseData<EvaluationSum>> call, Response<ResponseData<EvaluationSum>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFailure(Call<ResponseData<EvaluationSum>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 운행이력
     */
    public void getAllocationCompletedList(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<AllocationCompleted>> call = getService().getAllocationCompletedList(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationCompleted>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationCompleted>> call, Response<ResponseData<AllocationCompleted>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationCompleted>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 운행이력_상세
     */
    public void getTMap(final Context context, String search, final ResponseCallback callback) {
        String appKey = Global.TMAP_APIKEY;

        try {
            Call<TMapSearchInfo> call = getService().getTMap(appKey, search);

            call.enqueue(new Callback<TMapSearchInfo>() {
                @Override
                public void onResponse(Call<TMapSearchInfo> call, Response<TMapSearchInfo> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<TMapSearchInfo> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getAllocationCompletedOne(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        String auth_str = "Bearer ";
        if(MacaronApp.chauffeur != null) auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<AllocationCompletedOne>> call = getService().getAllocationCompletedOne(params, auth_str);

            call.enqueue(new Callback<ResponseData<AllocationCompletedOne>>() {
                @Override
                public void onResponse(Call<ResponseData<AllocationCompletedOne>> call, Response<ResponseData<AllocationCompletedOne>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<AllocationCompletedOne>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 일반운행 시작
     */
    public void sendStartRoadsale(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {

        String auth_str = "Bearer ";

        if(MacaronApp.chauffeur != null)
            auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<StartRoadsale>> call = getService().sendStartRoadsale(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<StartRoadsale>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<StartRoadsale>> call, Response<ResponseData<StartRoadsale>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<StartRoadsale>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 일반운행 종료
     */
    public void sendCompleteRoadsale(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {

        String auth_str = "Bearer ";

        if(MacaronApp.chauffeur != null)
            auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<Object>> call = getService().sendCompleteRoadsale(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Object>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 출근 요청
     */
    public void sendInoffice(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {

        String auth_str = "Bearer ";

        if(MacaronApp.chauffeur != null)
            auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<Inoffice>> call = getService().sendInoffice(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<Inoffice>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<Inoffice>> call, Response<ResponseData<Inoffice>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<Inoffice>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *  인증번호 요청
     */

    public void requestPhoneAuth(final Context context, HashMap<String, Object> params, @Nullable AppDef.AuthType authType, final ResponseCallback callback) {
        try {
            Call<ResponseData<PhoneAuthVO>> call = authType == AppDef.AuthType.CORPORATE
                    ? getService().requestCompanyPhoneAuth(params) : getService().requestChauffeurPhoneAuth(params);

            call.enqueue(new Callback<ResponseData<PhoneAuthVO>>() {
                @Override
                public void onResponse(Call<ResponseData<PhoneAuthVO>> call, Response<ResponseData<PhoneAuthVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), true);
                }

                @Override
                public void onFailure(Call<ResponseData<PhoneAuthVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkPhoneAuth(final Context context, HashMap<String, Object> params, AppDef.AuthType authType, final ResponseCallback callback) {
        try {
            Call<ResponseData<PhoneAuthVO>> call = authType == AppDef.AuthType.CORPORATE
                    ? getService().checkCompanyPhoneAuth(params) : getService().checkChauffeurPhoneAuth(params);

            call.enqueue(new Callback<ResponseData<PhoneAuthVO>>() {
                @Override
                public void onResponse(Call<ResponseData<PhoneAuthVO>> call, Response<ResponseData<PhoneAuthVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<PhoneAuthVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUserInfo(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().updateUserInfo(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getChauffeurInfo(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<ChauffeurInfo>> call = getService().getChauffeur(params, auth_str);

            call.enqueue(new Callback<ResponseData<ChauffeurInfo>>() {
                @Override
                public void onResponse(Call<ResponseData<ChauffeurInfo>> call, Response<ResponseData<ChauffeurInfo>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<ChauffeurInfo>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCommonCodes(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<SignupCodeListVO>> call = getService().getCodeList(params, auth_str);

            call.enqueue(new Callback<ResponseData<SignupCodeListVO>>() {
                @Override
                public void onResponse(Call<ResponseData<SignupCodeListVO>> call, Response<ResponseData<SignupCodeListVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<SignupCodeListVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCommonSubCodes(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<SignupCodeList>> call = getService().getCodeSubList(params, auth_str);

            call.enqueue(new Callback<ResponseData<SignupCodeList>>() {
                @Override
                public void onResponse(Call<ResponseData<SignupCodeList>> call, Response<ResponseData<SignupCodeList>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<SignupCodeList>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkBisNumber(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().checkBusinessNo(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCompanyList(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<CompanyVO>> call = getService().getCompanyList(params, auth_str);

            call.enqueue(new Callback<ResponseData<CompanyVO>>() {
                @Override
                public void onResponse(Call<ResponseData<CompanyVO>> call, Response<ResponseData<CompanyVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<CompanyVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkBankAccount(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().checkBankAccount(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void requestCorporationRegister(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().requestCorporationRegister(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCodeList(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<SignupCodeListVO>> call = getService().getCodeList(params, auth_str);

            call.enqueue(new Callback<ResponseData<SignupCodeListVO>>() {
                @Override
                public void onResponse(Call<ResponseData<SignupCodeListVO>> call, Response<ResponseData<SignupCodeListVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<SignupCodeListVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCodeAreaList(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<SignupCodeList>> call = getService().getCodeAreaList(params, auth_str);

            call.enqueue(new Callback<ResponseData<SignupCodeList>>() {
                @Override
                public void onResponse(Call<ResponseData<SignupCodeList>> call, Response<ResponseData<SignupCodeList>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<SignupCodeList>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCodeSubList(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<SignupCodeList>> call = getService().getCodeSubList(params, auth_str);

            call.enqueue(new Callback<ResponseData<SignupCodeList>>() {
                @Override
                public void onResponse(Call<ResponseData<SignupCodeList>> call, Response<ResponseData<SignupCodeList>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<SignupCodeList>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getRegistCompanyStatus(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<CompanyStatusVO>> call = getService().getRegistCompanyStatus(params, auth_str);

            call.enqueue(new Callback<ResponseData<CompanyStatusVO>>() {
                @Override
                public void onResponse(Call<ResponseData<CompanyStatusVO>> call, Response<ResponseData<CompanyStatusVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<CompanyStatusVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCompany(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<CompanyVO>> call = getService().getCompany(params, auth_str);

            call.enqueue(new Callback<ResponseData<CompanyVO>>() {
                @Override
                public void onResponse(Call<ResponseData<CompanyVO>> call, Response<ResponseData<CompanyVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<CompanyVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void registChauffeurBaseInfo(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().registChauffeurBaseInfo(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void registChauffeurAddInfo(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().registChauffeurAddInfo(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 이미지 업로드
     */
    public void uploadImage(final Context context, HashMap<String, Object> params, String imagePath, final ResponseCallback callback) {
        String auth_str = "Bearer ";

        if(MacaronApp.chauffeur != null)
            auth_str += MacaronApp.chauffeur.accessToken;

        try {
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), new File(imagePath));
            MultipartBody.Part part =  MultipartBody.Part.createFormData("file", imagePath, fileBody);
            RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), params.get("mobileNo").toString());
            RequestBody fileCat = RequestBody.create(MediaType.parse("text/plain"), params.get("fileCat").toString());
            Call<ResponseData<RegistPictureVO>> call = getService().uploadImage(part, phone, fileCat, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<RegistPictureVO>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<RegistPictureVO>> call, Response<ResponseData<RegistPictureVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<RegistPictureVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 사진 등록 후 신청 버튼
    public void registChauffeurStatus(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<Object>> call = getService().registChauffeurStatus(params, auth_str);

            call.enqueue(new Callback<ResponseData<Object>>() {
                @Override
                public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 쇼퍼 등록 정보 조회
    public void getRegistChauffeurInfo(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<RegistChauffeurInfoVO>> call = getService().getRegistChauffeurInfo(params, auth_str);

            call.enqueue(new Callback<ResponseData<RegistChauffeurInfoVO>>() {
                @Override
                public void onResponse(Call<ResponseData<RegistChauffeurInfoVO>> call, Response<ResponseData<RegistChauffeurInfoVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<RegistChauffeurInfoVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 쇼퍼 등록 정보 조회
    public void getRegistChauffeurStatus(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {
        try {
            String auth_str = "Bearer ";

            if(MacaronApp.chauffeur != null)
                auth_str += MacaronApp.chauffeur.accessToken;

            Call<ResponseData<ChauffeurStatusVO>> call = getService().getRegistChauffeurStatus(params, auth_str);

            call.enqueue(new Callback<ResponseData<ChauffeurStatusVO>>() {
                @Override
                public void onResponse(Call<ResponseData<ChauffeurStatusVO>> call, Response<ResponseData<ChauffeurStatusVO>> response) {
                    processCommonError(context, callback, response, call.request().url().toString(), false);
                }

                @Override
                public void onFailure(Call<ResponseData<ChauffeurStatusVO>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 운행 이동거리
     */
    public void getAllocationRealDist(final Context context, HashMap<String, Object> params, final ResponseCallback callback) {

        String auth_str = "Bearer ";

        if(MacaronApp.chauffeur != null)
            auth_str += MacaronApp.chauffeur.accessToken;

        try {
            Call<ResponseData<AllocationDist>> call = getService().getAllocationRealDist(params, auth_str);

            call.enqueue(new RetryableCallback<ResponseData<AllocationDist>>(call, context) {
                @Override
                public void onFinalResponse(Call<ResponseData<AllocationDist>> call, Response<ResponseData<AllocationDist>> response) {
                    processCommonError(context, callback, response, call.request().url().toString());
                }

                @Override
                public void onFinalFailure(Call<ResponseData<AllocationDist>> call, Throwable t) {
                    if (callback == null) return;
                    AnalyticsHelper.getInstance(context).sendEvent(call.request().url().toString(), "호출 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.API_REQUEST);

                    t.printStackTrace();
                    callback.onFailure(t);
                    showDialog(context, null, "네트웍상태를 확인해 주세요.");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
