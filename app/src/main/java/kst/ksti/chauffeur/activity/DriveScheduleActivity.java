package kst.ksti.chauffeur.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.adapter.DriveScheduleActivityAdapter;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.databinding.ActivityDriveScheduleBinding;
import kst.ksti.chauffeur.model.AllocationCompletedPage;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;

/**
 * DriveScheduleFragment 구조와 같아야 한다.
 * 이유 : MainActivity가 아닌 다른 Activity에서 사용 할 수 없기 때문에 새로운 Acitivity를 만들어서 사용.
 * DriveScheduleFragment에서 변경 하면 이곳도 똑같이 변경 해야 한다.
 */
public class DriveScheduleActivity extends BaseActivity<ActivityDriveScheduleBinding> {

    private DriveScheduleActivityAdapter adapter;
    private AllocationCompletedPage allocationCompletedPage = new AllocationCompletedPage();
    private ArrayList<AllocationSchedule> allocScheduleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(DriveScheduleActivity.this));

        setBind(R.layout.activity_drive_schedule);

        // 타이틀 화면
        if (getBind().title != null) {
            getBind().title.tvTitle.setText("예약목록");
            getBind().title.btnDrawerOpen.setVisibility(View.GONE);
            getBind().title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                }
            });
        }

        cancelLoadingViewAnimation();

        // 예약리스트 스크롤
        getBind().recycleDriveSchedule.setHasFixedSize(true);
        getBind().recycleDriveSchedule.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DriveScheduleActivityAdapter(this, allocScheduleList, DriveScheduleActivityAdapter.CALL_TYPE_RESV);
        getBind().recycleDriveSchedule.setAdapter(adapter);
        getBind().recycleDriveSchedule.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        clearRecyclerview(Global.FIRST_PAGE);
        getAllocScheduleList(Global.FIRST_PAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

    @SuppressLint("SetTextI18n")
    private void getAllocScheduleList(int page) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("limit", Global.LIST_LIMIT_COUNT);
        DataInterface.getInstance().receiveAllocSchedule(this, params, true, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode())) {
                    int listSize = 0;

                    allocationCompletedPage = response.getData().paging;
                    allocScheduleList.addAll(response.getList());
                    if(adapter != null) adapter.notifyDataSetChanged();

                    if(allocationCompletedPage != null) {
                        listSize = allocationCompletedPage.totalCount;
                    }

                    getBind().total.setText(listSize + "개 예약");

                } else {
                    Logger.d("쇼퍼정보 receive 실패");
                }

                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, AllocDetailActivity.class.getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // 테스트 브로드 캐스트
//        Intent intent = new Intent("android.intent.action.AllocSelect");
//        intent.putExtra("data1", "black");
//        intent.putExtra("data2", "Jin");
//        intent.putExtra("data3", "Data");
//
//        this.sendBroadcast(intent);
        // 테스트 종료

        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
