package kst.ksti.chauffeur.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.adapter.DriveScheduleAdapter;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrWorkingBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.AllocationCompletedPage;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.Summary;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 메인화면
 */
public class DrivingFragment extends NativeFragment implements OnTitleListener {

    private FrWorkingBinding mBind;
    private Timer scheduleTimer;
    private DriveScheduleAdapter adapter;
    private AllocationCompletedPage allocationCompletedPage = new AllocationCompletedPage();
    private Summary summaryData = new Summary();
    private ArrayList<AllocationSchedule> allocScheduleList = new ArrayList<>();
    private MacaronCustomDialog dialog;
    private AppDef.ChauffeurStatus chauffeurStatus;
    private boolean refreshCheck = true;    // 새로고침이 중복 발생되지 않도록 막는 Flag

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MacaronApp.nearByDrivingStatusCheck = true;
        MacaronApp.nearByDriveStartCheck = true;
        allocatedCheck = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(dialog != null) {
            dialog.dismiss();
        }

        clearScheduleListItem();
        cancelScheduleTimer();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_working, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그
        MacaronApp.allocStatus = AppDef.AllocationStatus.NONE;

        SetTitle("");
        SetDividerVisibility(false);
        SetTitleListener(this);
        setDrawerLayoutEnable(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrWorkingBinding.bind(getView());

        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });
        mBind.title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        // 직영일때만 사용되는 버튼을 보여준다.
//        if(MacaronApp.chauffeur.companyVo.getSvcType().equals(Global.ChauffeurMemberType.DIRECTMNG)) {
//            mBind.bottomGroup.setVisibility(View.VISIBLE); // 휴식, 일반운행 버튼
//        }
//        else {
//            mBind.bottomGroup.setVisibility(View.GONE); // 휴식, 일반운행 버튼
//        }

        initEventListener();

        // 화면 전환 셋팅
        // 앱 강제종료후 돌아왔을때 분기
        windowRetore();

        // 예약 스케쥴 호출
        setScheduleTimer();
    }

    /**
     * 화면 복구 시스템
     */
    private void windowRetore() {
        if(MacaronApp.isRestoreLogic)
        {
            MacaronApp.isRestoreLogic = false;

            if(MacaronApp.currAllocation.allocationStatus != null &&    // 예약운행 정보가 있다.
                    !MacaronApp.isCheckLoadSale())  // 일반운행을 하고 있지 않다.
            {
                switch (MacaronApp.currAllocation.allocationStatus) {
                    case "NEARBY":
                    case "DEPART": {
                        if (MacaronApp.currAllocation != null) {
                            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
                            GoNativeScreenReplaceAdd(new OrgArrivedFragment(), null, 1);

                        } else {
                            Toast.makeText(nativeMainActivity, "해당 배차정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }

                    case "ORIGIN":
                        AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
                        GoNativeScreenReplaceAdd(new CustomerLoadFragment(), null, 1);
                        break;

                    case "CHECKIN": {
                        if (PrefUtil.getStartTime(nativeMainActivity) == 0) {
                            PrefUtil.setStartTime(nativeMainActivity, System.currentTimeMillis());
                        }

                        if (MacaronApp.currAllocation != null) {
                            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
                            GoNativeScreenReplaceAdd(new DestArrivedFragment(), null, 1);

                        } else {
                            Toast.makeText(nativeMainActivity, "해당 배차정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }

                    case "ARRIVAL":
                        AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("배달완료", "배달완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
                        switchToPayment(MacaronApp.currAllocation.fareCat);
                        break;
                }
            }
            else if(MacaronApp.isCheckLoadSale())   // 일반운행중이다.
            {
                // 일반운행 인덱스가 있으면 일반운행 중이다.
                // 일반운행을 종료하면 꼭 인덱스를 초기화 해줘야함.
                if(MacaronApp.currStartRoadsale.roadSaleIdx > 0)
                {
                    PrefUtil.setStartTime(nativeMainActivity, System.currentTimeMillis());
                    GoNativeScreen(new RoadsaleCompleteFragment(), null, 1);
                }
            }
        }
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.btnRest.setOnClickListener(onSingleClickListener);
        mBind.btnMoveToGarage.setOnClickListener(onSingleClickListener);
        mBind.btnFloating.setOnClickListener(onClickListener);
        mBind.btnTTS.setOnClickListener(onClickListener);
        mBind.btnRoadsale.setOnClickListener(onSingleClickListener);
        //mBind.tvfailTotalUseAttemptedCount.setOnClickListener(onSingleClickListener);
        mBind.btnMoveToGarage2.setOnClickListener(onSingleClickListener);

        // 당겨서 새로고침
        mBind.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList(Global.FIRST_PAGE);
            }
        });

        // 체크박스 싱크 맞춰준다.
        if(PrefUtil.getOptionTTS(nativeMainActivity))
            mBind.btnTTS.setChecked(true);
        else
            mBind.btnTTS.setChecked(false);

        // 체크박스 싱크 맞춰준다.
        if(PrefUtil.getOptionFloating(nativeMainActivity))
            mBind.btnFloating.setChecked(true);
        else
            mBind.btnFloating.setChecked(false);

        // 예약리스트 스크롤
        mBind.recycleDriveSchedule.setHasFixedSize(true);
        mBind.recycleDriveSchedule.setLayoutManager(new LinearLayoutManager(nativeMainActivity));

        adapter = new DriveScheduleAdapter(nativeMainActivity, allocScheduleList, DriveScheduleAdapter.CALL_TYPE_DRIVING);
        mBind.recycleDriveSchedule.setAdapter(adapter);

        mBind.recycleDriveSchedule.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 예약 리스트 다음 페이지 호출
                if(allocationCompletedPage != null)
                {
                    try
                    {
                        int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                        int itemTotalCount = recyclerView.getAdapter().getItemCount();

                        //Logger.e("LOG1 - findLastVisibleItemPosition = " + lastVisibleItemPosition + ", getItemCount()-3 = " + (itemTotalCount-3));
                        if( (lastVisibleItemPosition == itemTotalCount-3) && allocationCompletedPage.hasNextPage) {
                            if(allocationCompletedPage.page < allocationCompletedPage.totalPages) {
                                allocationCompletedPage.hasNextPage = false;        // 한번 호출 하면 호출 하지 말자
                                refreshList(allocationCompletedPage.page + 1);
                            }
                        }
                    }
                    catch(NullPointerException e)
                    {
                        Logger.e(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {

            switch (v.getId()) {
                case R.id.btnRest:
                    showChangeStatusDialog("휴식", "휴식하시겠습니까?", AppDef.ChauffeurStatus.REST);
                    break;

                case R.id.btnMoveToGarage:
                case R.id.btnMoveToGarage2:
                    showChangeStatusDialog(getString(R.string.txt_move_to_garage), getString(R.string.popup_retire), AppDef.ChauffeurStatus.RETIRE);
                    break;

                case R.id.btnRoadsale:
                    nativeBaseActivity.playLoadingViewAnimation();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new DestSearchFragment(), bundle2, 1);

                    //showChangeStatusDialog("일반운행", "일반운행을 시작하시겠습니까?", AppDef.ChauffeurStatus.ROADSALE);
                    break;
            }
        }
    };

    /**
     * 퇴근하기 다이얼로그 호출
     */
    private void confirmLeave() {
        String title, message;

        if(MacaronApp.scheduleCount > 0) {
            title = "긴급퇴근";
            message = "<font color='#ff1c74'>잔여 예약</font>이 남아있습니다.<br/>정말로 퇴근하시겠습니까?";
        } else {
            title = "퇴근";
            message = "정말로 퇴근하시겠습니까?";
        }

        dialog = new MacaronCustomDialog(nativeMainActivity, title, Html.fromHtml(message), "네", "아니오",
                leftListener,
                rightListener,
                true);
        dialog.show();
    }

    /**
     * 퇴근하기 다이얼로그 오른쪽버튼 클릭리스너
     */
    private View.OnClickListener rightListener = new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
        }
    };

    /**
     * 퇴근하기 다이얼로그 왼쪽버튼 클릭리스너
     */
    private View.OnClickListener leftListener = new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
            nativeBaseActivity.playLoadingViewAnimation();
            nativeMainActivity.setLogout();
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnFloating:
                    nativeBaseActivity.cancelLoadingViewAnimation();

                    // 플로팅 버튼 활성 여부
                    if(PrefUtil.getOptionFloating(nativeMainActivity)) {
                        PrefUtil.setOptionFloating(nativeMainActivity, false);
                        mBind.btnFloating.setChecked(false);
                    }
                    else {
                        PrefUtil.setOptionFloating(nativeMainActivity, true);
                        mBind.btnFloating.setChecked(true);
                    }
                    break;

                case R.id.btnTTS:
                    nativeBaseActivity.cancelLoadingViewAnimation();

                    // TTS 활성 여부
                    if(PrefUtil.getOptionTTS(nativeMainActivity)) {
                        PrefUtil.setOptionTTS(nativeMainActivity, false);
                        mBind.btnTTS.setChecked(false);
                    }
                    else {
                        PrefUtil.setOptionTTS(nativeMainActivity, true);
                        mBind.btnTTS.setChecked(true);
                    }
                    break;
            }
        }
    };

    /**
     * 쇼퍼 상태변경 요청 다이얼로그
     */
    private void showChangeStatusDialog(String title, String content, final AppDef.ChauffeurStatus status) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new MacaronCustomDialog(nativeMainActivity, title, content, "네", "아니오",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        chauffeurStatus = status;

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("chauffeurStatusCat", status.toString());
                        changeChauffeurStatusAndGoNextScreen(params, changeStatusInterface);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        nativeBaseActivity.cancelLoadingViewAnimation();
                    }
                });
        dialog.show();
    }

    /**
     * 1분마다 예약리스트 새로고침 타이머.
     */
    public void setScheduleTimer() {
        if(scheduleTimer == null) {
            scheduleTimer = new Timer(true);

            scheduleTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    UIThread.executeInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshList(Global.FIRST_PAGE);
                        }
                    });
                }
            }, 0, 1000 * 60);
        }
    }

    /**
     * 1분마다 새로고침 타이머 정지
     */
    public void cancelScheduleTimer() {
        if(scheduleTimer != null) {
            scheduleTimer.cancel();
            scheduleTimer = null;
        }
    }

    /**
     * 예약리스트 새로고침 호출 (기존에 있던 항목들은 전부 초기화)
     */
    public void refreshList(int page) {
        try {
            if(refreshCheck) {
                refreshCheck = false;

                // 첫 페이지 호출 할때만 리스트 클리어 해준다.
                if(page == Global.FIRST_PAGE) {
                    // 페이지 데이터 초기화
                    if(allocationCompletedPage != null)
                    {
                        allocationCompletedPage = null;
                        allocationCompletedPage = new AllocationCompletedPage();
                    }
                    else
                        allocationCompletedPage = new AllocationCompletedPage();

                    clearScheduleListItem();
                }

                getAllocScheduleList(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 예약리스트 초기화
     */
    private void clearScheduleListItem() {
        try {
            if (allocScheduleList != null && allocScheduleList.size() > 0) {
                allocScheduleList.clear();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 쇼퍼 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            switch (chauffeurStatus) {
                case REST:
                    Bundle bundle = new Bundle();
                    bundle.putLong("breakTime", Math.round((double)response.getData()));
                    GoNativeScreenReplaceAdd(new BreakTimeFragment(), bundle, 1);
                    break;

                case RETIRE:
                    GoNativeScreenReplaceAdd(new MoveGarageFragment(), null, 1);
                    break;

                case ROADSALE:
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("arrowBack", "y");
                    GoNativeScreenReplaceAdd(new DestSearchFragment(), bundle2, 1);
                    break;

                default:
                    nativeBaseActivity.cancelLoadingViewAnimation();
            }
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            switch (response.getResultCode()) {
                case Global.ErrorCode.EC901:
                    showErrorCode901_Dialog(nativeMainActivity, response.getError());
                    break;
                case Global.ErrorCode.EC902:
                    showErrorCode902_Dialog(getActivity(), response.getError());
                    break;
                default:
                    showErrorCodeEtcDialog(nativeMainActivity, response.getError());
                    break;
            }

            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onError() {
            nativeBaseActivity.cancelLoadingViewAnimation();
        }

        @Override
        public void onFailed(Throwable t) {
            nativeBaseActivity.cancelLoadingViewAnimation();
        }
    };

    @Override
    public void onTitleBackPress() {

    }

    @Override
    public void onTitleClosePress() {
        // 전체 화면 팝업 닫기 버튼 선택
    }

    @Override
    public void onSidelistClicked() {

    }

    @Override
    public void doBack() {

    }

    /**
     * 예약리스트 호출
     */
    @SuppressLint("SetTextI18n")
    private void getAllocScheduleList(int page) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", Global.LIST_LIMIT_COUNT);

        DataInterface.getInstance().getAllocationMainList(nativeMainActivity, params, true, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                int listSize = 0;
                int attemptedCount = 0; // 미수행 건수

                if ("S000".equals(response.getResultCode())) {
                    summaryData = response.getData().summaryData;
                    setAllocList(response);

                    allocationCompletedPage = response.getData().paging;
                } else {
                    Logger.d("쇼퍼정보 receive 실패");
                }

                if(summaryData != null) {
                    listSize = summaryData.totalCount;
                    attemptedCount = summaryData.attemptedCount;
                    MacaronApp.scheduleCount = summaryData.totalCount;
                }

                if(listSize == 0) {
                    nativeBaseActivity.cancelLoadingViewAnimation();
                }

//                if(attemptedCount == 0) {
//                    mBind.unuseAttemptedCount.setVisibility(View.VISIBLE);
//                    mBind.useAttemptedCount.setVisibility(View.GONE);
//                }
//                else {
//                    mBind.unuseAttemptedCount.setVisibility(View.GONE);
//                    mBind.useAttemptedCount.setVisibility(View.VISIBLE);
//                }

                mBind.unuseAttemptedCount.setVisibility(View.VISIBLE);
                mBind.useAttemptedCount.setVisibility(View.GONE);

                mBind.tvTotalUnuseAttemptedCount.setText("배달목록 총 " + String.valueOf(listSize) + "개");

                mBind.totalUseAttemptedCount.setText("배달목록 총 " + String.valueOf(listSize) + "개");
                mBind.tvfailTotalUseAttemptedCount.setText("미배달 " + String.valueOf(attemptedCount) + "개");

                mBind.swipeRefresh.setRefreshing(false);
                refreshCheck = true;
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                errorGetAllocList();
            }

            @Override
            public void onFailure(Throwable t) {
                errorGetAllocList();
            }
        });
    }

    private boolean allocatedCheck;

    /**
     * 예약리스트 초기화
     * @param response 예약정보
     */
    private void setAllocList(ResponseData<AllocationSchedule> response) {
        if(response != null) {
            allocScheduleList.addAll(response.getList());

//            if(allocScheduleList.size() > 0) {
//                for(int i=0; i<allocScheduleList.size(); i++) {
//                    String status = allocScheduleList.get(i).allocationStatus;
//
//                    // 제일 처음 들어오는 항목이고, 배차상태가 ALLOCATED가 아닐경우 Idx를 요청하고나서 각 상태별 화면으로 이동시킴
//                    if(!status.equalsIgnoreCase(AppDef.AllocationStatus.ALLOCATED.toString()) && !allocatedCheck) {
//                        // 운행화면으로 넘어가야할 경우, 다음 아이템이 접근 못하게 true 처리
//                        if (status.equalsIgnoreCase(AppDef.AllocationStatus.DEPART.toString())
//                                || status.equalsIgnoreCase(AppDef.AllocationStatus.NEARBY.toString())
//                                || status.equalsIgnoreCase(AppDef.AllocationStatus.ORIGIN.toString())
//                                || status.equalsIgnoreCase(AppDef.AllocationStatus.CHECKIN.toString())
//                                || status.equalsIgnoreCase(AppDef.AllocationStatus.ARRIVAL.toString())) {
//                            allocatedCheck = true;
//                        }
//                        getAllocation(allocScheduleList.get(i).allocationIdx, allocScheduleList.get(i).allocationStatus);
//                    }
//                }
//            }

            if(!allocatedCheck) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

//            mBind.recycleDriveSchedule.setHasFixedSize(true);
//            mBind.recycleDriveSchedule.setLayoutManager(new LinearLayoutManager(nativeMainActivity));

//            adapter = new DriveScheduleAdapter(nativeMainActivity, allocScheduleList, DriveScheduleAdapter.CALL_TYPE_DRIVING);
//            mBind.recycleDriveSchedule.setAdapter(adapter);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void getAllocation(long alloc_idx, final String status) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        DataInterface.getInstance().getAllocation(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    MacaronApp.currAllocation = response.getData();
                    setActionOfStatus(status);

                } else {
                    sendEventFail(status, null);
                }
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                sendEventFail(status, null);
            }

            @Override
            public void onFailure(Throwable t) {
                sendEventFail(status, Util.getExceptionError(t));
            }
        });
    }

    private void sendEventFail(String status, String label) {
        String localLabel = "";
        if(label != null) {
            localLabel = label;
        }

        switch (status) {
            case "DEPART":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 실패", localLabel, Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
                break;
            case "ORIGIN":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 실패", localLabel, Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
                break;
            case "CHECKIN":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 실패", localLabel, Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
                break;
            case "ARRIVAL":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("운행완료", "운행완료 실패", localLabel, Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
                break;
        }
    }

    /**
     * 배차상태별 행동처리
     * @param status 배차상태
     */
    private void setActionOfStatus(String status) {
        switch (status) {
            case "NEARBY":
            case "DEPART": {
                if (MacaronApp.currAllocation != null) {
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("탑승시작", "탑승시작 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_DEPART);
                    GoNativeScreenReplaceAdd(new OrgArrivedFragment(), null, 1);

                } else {
                    Toast.makeText(nativeMainActivity, "해당 배차정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case "ORIGIN":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출발지도착", "출발지도착 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ORIGIN);
                GoNativeScreenReplaceAdd(new CustomerLoadFragment(), null, 1);
                break;

            case "CHECKIN": {
                if (PrefUtil.getStartTime(nativeMainActivity) == 0) {
                    PrefUtil.setStartTime(nativeMainActivity, System.currentTimeMillis());
                }

                if (MacaronApp.currAllocation != null) {
                    AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("고객탑승완료", "고객탑승완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_CHECKIN);
                    GoNativeScreenReplaceAdd(new DestArrivedFragment(), null, 1);

                } else {
                    Toast.makeText(nativeMainActivity, "해당 배차정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case "ARRIVAL":
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("운행완료", "운행완료 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_ARRIVAL);
                switchToPayment(MacaronApp.currAllocation.fareCat);
                break;
        }
    }

    private void switchToPayment(String payCat) {
        Bundle bundle = new Bundle();
        switch (payCat) {
            case "APPCARD":
                bundle.putString("arrowBack", "y");
                bundle.putString("title", "앱 결제 요금 입력");
                GoNativeScreenReplaceAdd(new InputPayFragment(), bundle, 1);
                break;
            case "OFFLINE":
                bundle.putString("arrowBack", "y");
                bundle.putString("title", "직접 결제 요금 입력");
                GoNativeScreenReplaceAdd(new InputPayFragment(), bundle, 1);
                break;
            default:
                Toast.makeText(nativeMainActivity, "결제방식이 존재하지 않습니다." ,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 예약리스트 호출실패 했을 때
     */
    private void errorGetAllocList() {
        MacaronApp.scheduleCount = 0;
        mBind.totalUseAttemptedCount.setText("배달목록 총 0개");
        mBind.tvfailTotalUseAttemptedCount.setText("미배달 0개");
        mBind.swipeRefresh.setRefreshing(false);
        refreshCheck = true;
        nativeBaseActivity.cancelLoadingViewAnimation();
    }

}
