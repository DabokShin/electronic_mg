package kst.ksti.chauffeur.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivityAllocselectBinding;
import kst.ksti.chauffeur.model.AcceptAllocationVO;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.DateUtils;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;

public class AllocSelectActivity extends BaseActivity<ActivityAllocselectBinding> {

    private final int READYTIME = 30 * 1000 * 39;        // 예약 대기 시간(39분)

    private long allocIdx = 0;
    private long resvDatetime = 0;
    private String resvOrgPoi = "";                 // 출발지 POI
    private String resvOrgAddress = "";             // 출발지 주소
    private String resvDstPoi = "";                 // 도착지 POI
    private String resvDstAddress = "";             // 도착지 주소
    private long estmDist = 0;                      // 이동거리
    private long estmTime = 0;                      // 예상시간
    private String estmTotalCost = "";              // 예상금액
    private String serviceKind = "";

    private long startReadyTime = 0;
    private MacaronCustomDialog macaronCustomDialog;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(AllocSelectActivity.this));

        overridePendingTransition(R.anim.pull_in_up, R.anim.push_out_down);

        Intent intent = getIntent();
        if (intent != null) {
            allocIdx = intent.getLongExtra("a_allocationIdx", 0);
            resvDatetime = intent.getLongExtra("a_resvDatetime", 0);
            resvOrgPoi = intent.getStringExtra("a_org");
            resvOrgAddress = intent.getStringExtra("a_orgAddress");
            resvDstPoi = intent.getStringExtra("a_resvDstPoi");
            resvDstAddress = intent.getStringExtra("a_resvDstAddress");
            estmDist = intent.getLongExtra("a_estmDist", 0);
            estmTime = intent.getLongExtra("a_estmTime", 0);
            estmTotalCost = intent.getStringExtra("a_estmTotalCost");
            serviceKind = intent.getStringExtra("a_serviceKind");
        }

        setBind(R.layout.activity_allocselect);

        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        getBind().btnReservationList.setOnClickListener(onSingleClickListener);     // 예약목록 버튼
        getBind().btnRefuse.setOnClickListener(onSingleClickListener);              // 거절 버튼
        getBind().btnAccept.setOnClickListener(onSingleClickListener);              // 수락 버튼

        // 탑 화면 세팅
        MacaronApp.topActivity = Global.TOP_SCREEN.ALLOCSELECT;

        // UI 세팅
        initUI();

        // 시간 카운트다운 스레드 시작
        UIThread.executeInUIThread(mTickExecutor, 100);

        // 브로드캐스트 리시버
//        IntentFilter filter = new IntentFilter("android.intent.action.AllocSelect");
//
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String message = "";
//                if(intent != null)
//                {
//                    message = ("뭐가 왔니?? " + intent.getAction() + " : " + intent.getStringExtra("data1"));
//                }
//
//                Logger.d("LOG1 : " + message);
//
//                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//            }
//        };
//
//        registerReceiver(receiver, filter);
    }

    /**
     * 배차예약 남은시간 타이머 쓰레드
     */
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            if(isFinishing()) {
                UIThread.removeUIThread(mTickExecutor);
                return;
            }

            remainReadyTime();
            UIThread.executeInUIThread(mTickExecutor, 100);
        }
    };

    /**
     * 배차예약 남은시간 계산하여 화면에 노출
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void remainReadyTime() {

        int remainTime = 0;
        long time   = (startReadyTime < 0) ? 0 : (SystemClock.elapsedRealtime() - startReadyTime);

        remainTime = READYTIME - (int)time;
        if(remainTime < 0)
            remainTime = 0;

        int hour    = (remainTime / (1000*60*60)) % 24;
        int minutes = (remainTime / (1000*60)) % 60;
        int seconds = (remainTime / 1000) % 60;

        String hourText = (hour>0) ? hour+":" : "";
        String minutesText = String.format("%02d", minutes) + ":";
        String secondsText = String.format("%02d", seconds);

        getBind().stopwatch.setText(hourText + minutesText + secondsText);

        // 예약대기 시간이 종료 되면 자동으로 거절된것으로 처리 한다.
        if(remainTime <= 0)
        {
            //Toast.makeText(this, "예약 배차 요청을 거절하였습니다.", Toast.LENGTH_SHORT).show();
            removeAllocation(allocIdx, true);
        }
    }

    /**
     * 배차예약현황정보 요청
     * @param alloc_idx idx
     */
    private void getAcceptAllocation(long alloc_idx) {
        // 한번 수락배차 정보를 요청한 것은 다시 요청 하지 않도록 한다.
        for(int i = 0; i < MacaronApp.allocSelectList.size(); i++)
        {
            if(MacaronApp.allocSelectList.get(i).isAction()) continue;
            if(MacaronApp.allocSelectList.get(i).getAllocationIdx() == alloc_idx)
            {
                MacaronApp.allocSelectList.get(i).isAction = true;
                break;
            }
        }

        if(MacaronApp.allocSelectList.size() == 0) {
            Logger.e("LOG1 : MacaronApp.allocSelectList.size() == 0");
            return;
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        playLoadingViewAnimation();

        DataInterface.getInstance().getAcceptAllocation(AllocSelectActivity.this, params, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {

                    if(response.getData() != null) {

                        Logger.d("LOG1 : getAcceptAllocation Request Data : " + response.getData().toString());

                        // 서버에서 수락배차가 프로세스가 완료되지 않은 것들만 받을 수 있다.
                        if(response.getData().allocationStatus.equals(AppDef.AllocationStatus.REQUESTED.toString())) {
                            allocIdx = response.getData().allocationIdx;
                            resvDatetime = response.getData().resvDatetime;
                            resvOrgPoi = response.getData().resvOrgPoi;
                            resvOrgAddress = response.getData().resvOrgAddress;

                            // 서비스 항목 세팅
                            StringBuilder stringBuilder = new StringBuilder();
                            if (response.getData().serviceNameList != null && response.getData().serviceNameList.size() > 0) {
                                for(int i=0; i<response.getData().serviceNameList.size(); i++) {
                                    stringBuilder.append(response.getData().serviceNameList.get(i));

                                    if(i < response.getData().serviceNameList.size() - 1) {
                                        stringBuilder.append(", ");
                                    }
                                }

                                serviceKind = stringBuilder.toString();
                            }
                            else
                                serviceKind = "";

                            initUI();
                        }
                        else {
                            removeAllocation(allocIdx, true);
                        }
                    }

                }
                else if (Global.ErrorCode.EC201.equals(response.getResultCode())) {
                    showErrorCodeDialog(AllocSelectActivity.this, null, response.getError());
                }else {
                    Logger.d("배차예약 정보 receive 실패");
                    removeAllocation(allocIdx, true);
                }

                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                Logger.d("LOG1 : onError" + response.getResultCode());
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }

            @Override
            public void onFailure(Throwable t) {
                Logger.d("LOG1 : onFailure" + t.getMessage());
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void initUI() {
        // UI 초기화
        getBind().stopwatch.setText("00:30");
        getBind().reserveTime.setText("");
        getBind().org.setText("");
        getBind().orgAddress.setText("");
        getBind().resvDstPoi.setText("");
        getBind().resvDstAddress.setText("");
        //getBind().estDist.setText("");
        getBind().tvServiceKind.setText("");

        // 배차예약 대기 카운트 시작
        for(int i = 0; i < MacaronApp.allocSelectList.size(); i++)
        {
            if(MacaronApp.allocSelectList.get(i).getAllocationIdx() == allocIdx)
            {
                startReadyTime = MacaronApp.allocSelectList.get(i).fcmRecvTime;

                // 알림 받은 문장을 읽는다.
                if(MacaronApp.tts != null) {
                    if(PrefUtil.getOptionTTS(AllocSelectActivity.this)) {

                        // 음성입력 권한 체크
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            MacaronApp.tts.speak(MacaronApp.allocSelectList.get(i).ttsText, TextToSpeech.QUEUE_FLUSH, null, "Voice");
                        }
                        else {
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Voice");
                            MacaronApp.tts.speak(MacaronApp.allocSelectList.get(i).ttsText, TextToSpeech.QUEUE_FLUSH, map);
                        }


                    }
                }
                break;
            }
        }

        getBind().reserveTime.setText(Html.fromHtml(DateUtils.getAcceptAllocationTime(AllocSelectActivity.this, resvDatetime)));

        // 주소 세팅
        String poi = "";
        String address = "";
        if(!TextUtils.isEmpty(resvOrgPoi))
            poi = resvOrgPoi;
        else
            poi = resvOrgAddress;

        address = resvOrgAddress;

        getBind().org.setText(poi);
        getBind().orgAddress.setText(address);

        poi = "";
        address = "";
        if(!TextUtils.isEmpty(resvDstPoi))
            poi = resvDstPoi;
        else
            poi = resvDstAddress;

        address = resvDstAddress;

        getBind().resvDstPoi.setText(poi);
        getBind().resvDstAddress.setText(address);

        String td = String.format("%.1f", (estmDist * 0.001));
        long hour = estmTime / 3600;
        long min = estmTime % 3600 / 60;
        long totalMin = hour * 60 + min;
        long totalCost = 0;
        try
        {
            totalCost = Integer.parseInt(estmTotalCost);
        }
        catch (Exception e)
        {
            Logger.e("LOG1 : totalCost parsing fail");
            e.printStackTrace();
        }
        DecimalFormat formatter = new DecimalFormat("###,###");
        //getBind().estDist.setText(td + "km " + totalMin + "분 " + formatter.format(totalCost) + "원");

        getBind().tvServiceKind.setText(serviceKind);
        if(serviceKind == null || serviceKind.equals(""))
            getBind().llService.setVisibility(View.GONE);
        else
            getBind().llService.setVisibility(View.VISIBLE);
    }

    /**
     * 수락 배차 수락 요청
     * @param alloc_idx idx
     */
    private void acceptAllocation(long alloc_idx) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        playLoadingViewAnimation();

        DataInterface.getInstance().acceptAllocation(AllocSelectActivity.this, params, new DataInterface.ResponseCallback<ResponseData<AcceptAllocationVO>>() {
            @Override
            public void onSuccess(ResponseData<AcceptAllocationVO> response) {
                if ("S000".equals(response.getResultCode())) {
                    if(response.getData() != null) {
                        if(response.getData().getAcceptCode().equals("SUCCESS")) {
                            Toast.makeText(AllocSelectActivity.this, response.getData().getMessage(), Toast.LENGTH_SHORT).show();
                            removeAllocation(allocIdx, true);
                        }
                        else if(response.getData().getAcceptCode().equals("FAIL")) {
                            showErrorCodeDialog(AllocSelectActivity.this, "배차 실패", response.getData().getMessage());
                        }
                        else if(response.getData().getAcceptCode().equals("FINISH")) {
                            showErrorCodeDialog(AllocSelectActivity.this, "배차 실패", response.getData().getMessage());
                        }
                        else {
                            removeAllocation(allocIdx, true);
                        }
                    }
                    else {
                        removeAllocation(allocIdx, true);
                    }
                }
                else if (Global.ErrorCode.EC201.equals(response.getResultCode())) {
                    showErrorCodeDialog(AllocSelectActivity.this, null, response.getError());
                } else {
                    Logger.d("수락 요청 실패");
                    removeAllocation(allocIdx, true);
                }

                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AcceptAllocationVO> response) {
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }
        });
    }

    /**
     * 수락 배차 거절 요청
     * @param alloc_idx idx
     */
    private void rejectAllocation(long alloc_idx) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        playLoadingViewAnimation();

        DataInterface.getInstance().rejectAllocation(AllocSelectActivity.this, params, new DataInterface.ResponseCallback<ResponseData<AcceptAllocationVO>>() {
            @Override
            public void onSuccess(ResponseData<AcceptAllocationVO> response) {
                Toast.makeText(AllocSelectActivity.this, "예약 배차 요청을 거절하였습니다", Toast.LENGTH_SHORT).show();

                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }

            @Override
            public void onError(ResponseData<AcceptAllocationVO> response) {
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
                removeAllocation(allocIdx, true);
            }
        });
    }

    /**
     * 예약배차 리스트중 하나 삭제
     */
    public void removeAllocation(long alloc_idx, boolean isFinish) {
        // 수락배차를 한번 실행하면 실행한 idx를 찾아 지워준다.
        // 중간에 실패를 해도 이 작업을 해줘야 한다.
        // 이게 실행이 안될 경우 수락 배차를 받을 수 없다.
        for(int i = 0; i < MacaronApp.allocSelectList.size(); i++)
        {
            if(!MacaronApp.allocSelectList.get(i).isAction()) continue;     // isActio이 true 일 경우 수락/거절 요청까지 완료 한 것이다.
            if(MacaronApp.allocSelectList.get(i).getAllocationIdx() == alloc_idx)
            {
                MacaronApp.allocSelectList.remove(i);
                break;
            }
        }

        // 수락배차 예약 리스트가 남아 있는지 체크
        if(!checkReAllocList()) {
            if(isFinish) {
                finish();
            }
        }
    }

    /**
     * 예약배차 리스트가 남아있으면 다시 UI 셋팅 해준다.
     */
    public boolean checkReAllocList() {
        if(MacaronApp.allocSelectList.size() > 0)
        {
            allocIdx = MacaronApp.allocSelectList.get(0).getAllocationIdx();
            getAcceptAllocation(allocIdx);
            return true;
        }

        return false;
    }

    /**
     * 예약 배차 수락 팝업
     */
    public void showAcceptDialog(Context context, String title, String msg) {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        macaronCustomDialog = new MacaronCustomDialog(context, title, Html.fromHtml(msg), "확인", "취소",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        macaronCustomDialog.dismiss();

                        // 수락 요청
                        acceptAllocation(allocIdx);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        macaronCustomDialog.dismiss();
                    }
                }, true);

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
            Logger.e("LOG1 : " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 에러 다이얼로그 (수락배차 실패 팝업)
     */
    public void showErrorCodeDialog(Context context, String title, String msg) {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        macaronCustomDialog = new MacaronCustomDialog(context, title, msg, "확인",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        macaronCustomDialog.dismiss();

                        if(MacaronApp.allocSelectList.size() == 0) {
                            finish();
                        }
                        else {
                            removeAllocation(allocIdx, true);
                        }
                    }
                }, true);

        try {
            macaronCustomDialog.show();
        } catch (Exception e) {
                Logger.e("LOG1 : " + e.getMessage());
                e.printStackTrace();
        }
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btn_reservation_list: // 예약목록

                    Intent intent = new Intent(getApplicationContext(), DriveScheduleActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

                    break;
                case R.id.btnRefuse:    // 거절
                    rejectAllocation(allocIdx);
                    break;
                case R.id.btnAccept:    // 수락

                    String msg = "";
                    msg = DateUtils.getAcceptAllocationTime(AllocSelectActivity.this, resvDatetime) + "<br><br>예약 건을\n수락하시겠습니까?\n(수락 후 취소 불가)";
                    showAcceptDialog(AllocSelectActivity.this, "예약 배차 수락", msg);

                    //acceptAllocation(allocIdx);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, AllocSelectActivity.class.getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UIThread.removeUIThread(mTickExecutor);

        // 리시버 해제
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }
    }

    @Override
    public void finish() {
        super.finish();

        // 탑 화면 세팅
        // 메인으로 돌아가기 때문에 탑 액티비티를 메인으로 잡는다.
        MacaronApp.topActivity = Global.TOP_SCREEN.MAIN;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
