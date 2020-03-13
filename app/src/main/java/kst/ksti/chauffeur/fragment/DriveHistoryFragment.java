package kst.ksti.chauffeur.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.adapter.DriveHistoryAdapter;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.databinding.FrDriveHistoryBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.AllocationCompleted;
import kst.ksti.chauffeur.model.AllocationCompletedPage;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;

/**
 * 운행이력 화면
 */
public class DriveHistoryFragment extends NativeFragment implements OnTitleListener {

    private AllocationCompletedPage completedPage = new AllocationCompletedPage();
    private DriveHistoryAdapter adapter;
    private FrDriveHistoryBinding mBind;
    private LinearLayoutManager linearLayoutManager;

    private boolean isBottomCheck     = true;   // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 구분하는 Flag
    private boolean firstLoadingCheck = false;  // 서버통신을 한번이상 했는지 구분하는 Flag

    private boolean isBackKeyCheck = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Bundle bundle = getArguments();
//        if (bundle != null) {
//        }

        firstLoadingCheck = false;

        baackKeyStatusCheck();
    }

    /**
     * 백키 활성화 체크하는 함수
     * 출근하기 버튼 누르기전에 운행이력 화면으로 이동했을 경우,
     * 백키가 안먹히게 일시적으로 status 변경시킴.
     * finish() 호출시 isBackKeyCheck를 확인하여 다시 원래 status로 원복.
     */
    private void baackKeyStatusCheck() {
        if(!PrefUtil.getBackKeyCheck(nativeMainActivity)) {
            isBackKeyCheck = true;
            PrefUtil.setBackKeyCheck(nativeMainActivity, true);
        } else {
            isBackKeyCheck = false;
        }
    }

    @Override
    public void onDetach() {
        if(adapter != null && adapter.getItemCount() > 0) {
            adapter.clear();
        }

        if(isBackKeyCheck) {
            PrefUtil.setBackKeyCheck(nativeMainActivity, false);
        }

        System.gc();
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_drive_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("운행이력");

        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrDriveHistoryBinding.bind(getView());
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

        // 운행통계 버튼 클릭
        mBind.title.btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLoadingCheck = false;
                GoNativeScreenReplaceAdd(new StatisticsFragment(), null, 0);
            }
        });
    }

    /**
     * 운행이력리스트 호출할 페이지정보 세팅 YT
     */
    private HashMap<String, Object> getParams(int page) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", Global.LIST_LIMIT_COUNT);

        return params;
    }

    /**
     * 운행이력 리사이클러뷰 초기화
     */
    private void initDriveHistoryRecyclerview() {
        linearLayoutManager = new LinearLayoutManager(nativeMainActivity);
        mBind.recyclerviewDriveHistory.setLayoutManager(linearLayoutManager);

        mBind.recyclerviewDriveHistory.setHasFixedSize(true);
        adapter = new DriveHistoryAdapter(nativeMainActivity, new ArrayList<AllocationCompleted>());

        mBind.recyclerviewDriveHistory.setAdapter(adapter);

        mBind.recyclerviewDriveHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if( (linearLayoutManager.findLastVisibleItemPosition() == linearLayoutManager.getItemCount()-3) && isBottomCheck) {
                    isBottomCheck = false;

                    if(completedPage.page < completedPage.totalPages) {
                        getAllocationCompletedList(getParams((completedPage.page + 1)));
                    }
                }
            }
        });
    }


    /**
     * 운행이력 요청 함수
     * @param params page:요청페이지 / limit:요청
     */
    private void getAllocationCompletedList(HashMap<String, Object> params) {
        DataInterface.getInstance().getAllocationCompletedList(nativeMainActivity, params, new DataInterface.ResponseCallback<ResponseData<AllocationCompleted>>() {
            @Override
            public void onSuccess(ResponseData<AllocationCompleted> response) {
                if ("S000".equals(response.getResultCode())) {
                    try {
                        adapter.addItems(response.getList());
                        completedPage = response.getData().paging;

                        if(!firstLoadingCheck) {
                            firstLoadingCheck = true;
                        } else {
                            isBottomCheck = true;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Logger.d("쇼퍼정보 receive 실패");
                }

                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationCompleted> response) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!firstLoadingCheck) {
            initDriveHistoryRecyclerview();
            getAllocationCompletedList(getParams(1));
        }
    }


    @Override
    public void onTitleBackPress() {

    }

    @Override
    public void onTitleClosePress() {

    }

    @Override
    public void onSidelistClicked() {

    }

    @Override
    public void doBack() {

    }
}
