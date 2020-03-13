package kst.ksti.chauffeur.fragment;

import android.annotation.SuppressLint;
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
import kst.ksti.chauffeur.adapter.DriveScheduleAdapter;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.databinding.FrAllocationScheduleBinding;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.model.AllocationCompletedPage;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;

/**
 * DriveScheduleActivity 구조와 같아야 한다.
 * DriveScheduleActivity에서 변경 하면 이곳도 똑같이 변경 해야 한다.
 * 이유는 DriveScheduleActivity 에서 확인
 */
public class DriveScheduleFragment extends NativeFragment implements OnTitleListener {

    private DriveScheduleAdapter adapter;
    private FrAllocationScheduleBinding mBind;
    private AllocationCompletedPage allocationCompletedPage = new AllocationCompletedPage();
    private ArrayList<AllocationSchedule> allocScheduleList = new ArrayList<>();

    private boolean isBackKeyCheck = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        if(isBackKeyCheck) {
            PrefUtil.setBackKeyCheck(nativeMainActivity, false);
        }

        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearRecyclerview(Global.FIRST_PAGE);
    }

    private void clearRecyclerview(int page) {
        // 첫 페이지 호출 할때만 리스트 클리어 해준다.
        if(page == Global.FIRST_PAGE) {
            if(allocScheduleList != null && allocScheduleList.size() > 0) {
                allocScheduleList.clear();

                // 페이지 데이터 초기화
                if(allocationCompletedPage != null)
                {
                    allocationCompletedPage = null;
                    allocationCompletedPage = new AllocationCompletedPage();
                }
                else
                    allocationCompletedPage = new AllocationCompletedPage();

                if(adapter != null) adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_allocation_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("배달목록");
        SetDividerVisibility(true);
        SetTitleListener(this);
        setDrawerLayoutEnable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());
        mBind = FrAllocationScheduleBinding.bind(getView());

        // 목록 초기화
        clearRecyclerview(Global.FIRST_PAGE);
        getAllocScheduleList(Global.FIRST_PAGE);

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

        // 예약리스트 스크롤
        mBind.recycleDriveSchedule.setHasFixedSize(true);
        mBind.recycleDriveSchedule.setLayoutManager(new LinearLayoutManager(nativeMainActivity));

        adapter = new DriveScheduleAdapter(nativeMainActivity, allocScheduleList, DriveScheduleAdapter.CALL_TYPE_RESV);
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
                        int lastVisibleItemPosition = 0;
                        lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                        int itemTotalCount = recyclerView.getAdapter().getItemCount();

                        //Logger.e("LOG1 - findLastVisibleItemPosition = " + lastVisibleItemPosition + ", getItemCount()-3 = " + (itemTotalCount-3));
                        if( (lastVisibleItemPosition == itemTotalCount-3) && allocationCompletedPage.hasNextPage) {
                            if(allocationCompletedPage.page < allocationCompletedPage.totalPages) {
                                allocationCompletedPage.hasNextPage = false;        // 한번 호출 하면 호출 하지 말자
                                getAllocScheduleList(allocationCompletedPage.page + 1);
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

//        mBind.today.setText(DateUtils.getCurrentYear()+"년 " + DateUtils.getCurrentMonth()+ "월 " + DateUtils.getCurrentDay()+ "일");
    }

    @SuppressLint("SetTextI18n")
    private void getAllocScheduleList(int page) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", Global.LIST_LIMIT_COUNT);
        DataInterface.getInstance().receiveAllocSchedule(getActivity(), params, true, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    int listSize = 0;

                    allocationCompletedPage = response.getData().paging;
                    allocScheduleList.addAll(response.getList());
                    if(adapter != null) adapter.notifyDataSetChanged();
                    //adapter.addItems(response.getList());

//                    mBind.recycleDriveSchedule.setHasFixedSize(true);
//                    mBind.recycleDriveSchedule.setLayoutManager(new LinearLayoutManager(getActivity()));
//
//                    adapter = new DriveScheduleAdapter(getActivity(), allocScheduleList, DriveScheduleAdapter.CALL_TYPE_RESV);
//                    mBind.recycleDriveSchedule.setAdapter(adapter);

                    if(allocationCompletedPage != null) {
                        listSize = allocationCompletedPage.totalCount;
                    }

                    mBind.total.setText(listSize + "개 예약");

                } else {
                    Logger.d("쇼퍼정보 receive 실패");
                }

                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                nativeBaseActivity.cancelLoadingViewAnimation();
            }
        });
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
