package kst.ksti.chauffeur.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivityDriveHistoryDetailBinding;
import kst.ksti.chauffeur.model.AllocationCompletedOne;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.DateUtils;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.Util;

/**
 * CHECKOUT, NOSHOW, CANCELED
 * 배차상태별로 보여줘야하는 내용이 다름
 */
public class DriveHistoryDetailActivity extends BaseActivity<ActivityDriveHistoryDetailBinding> {

    private long alloc_idx = -1L;
    private AllocationCompletedOne allocCompletedOne;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(DriveHistoryDetailActivity.this));

        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1L);
        }

        setBind(R.layout.activity_drive_history_detail);
        setLoadingBarLayout(getBind().loadingBarLayout);
        setProgressBar(getBind().progressBar);

        playLoadingViewAnimation();

        initTitleBar();
        setViewEvent();

        getAllocationCompletedOne(alloc_idx);
    }

    private void setViewEvent() {
        getBind().defailConfirmBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            }
        });

        getBind().btnCallCustomer.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Call(allocCompletedOne.safePhoneno);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsHelper.getInstance(this).sendScreenFromJson(this, DriveHistoryDetailActivity.class.getSimpleName());
    }

    /**
     * 타이틀 영역 초기화
     */
    private void initTitleBar() {
        if (getBind().title != null) {
            getBind().title.btnTitleBack.setVisibility(View.VISIBLE);
            getBind().title.btnDrawerOpen.setVisibility(View.GONE);
            getBind().title.tvTitle.setText("배달 상세내역");
            getBind().title.ivDivider.setVisibility(View.VISIBLE);
            getBind().title.btnTitleBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                }
            });

            if(BuildConfig.DEBUG) {
                if(Global.getDEV()) {
                    String dev_type = Global.getServerType();
                    getBind().title.tvTitleServerChange.setText(dev_type);
                } else {
                    getBind().title.tvTitleServerChange.setText("");
                }
                getBind().title.tvTitleServerChange.setVisibility(View.VISIBLE);

            } else {
                getBind().title.tvTitleServerChange.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 운행 상세내역 서버호출
     * @param alloc_idx idx
     */
    private void getAllocationCompletedOne(long alloc_idx) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", alloc_idx);

        DataInterface.getInstance().getAllocationCompletedOne(DriveHistoryDetailActivity.this, params, new DataInterface.ResponseCallback<ResponseData<AllocationCompletedOne>>() {
            @Override
            public void onSuccess(ResponseData<AllocationCompletedOne> response) {
                if ("S000".equals(response.getResultCode())) {
                    initUI(response.getData());
                } else {
                    showErrorDialog(response.getError());
                }
                cancelLoadingViewAnimation();
            }

            @Override
            public void onError(ResponseData<AllocationCompletedOne> response) {
                showErrorDialog(response.getError());
                cancelLoadingViewAnimation();
            }

            @Override
            public void onFailure(Throwable t) {
                cancelLoadingViewAnimation();
            }
        });
    }

    private MacaronCustomDialog dialog;

    private void showErrorDialog(String msg) {
        getBind().tmp14.setText("도착예정시간");
        getBind().tmp16.setText("예상시간");
        getBind().tmp27.setText("결제예정정보");
        getBind().tmp38.setText("예상금액");

        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new MacaronCustomDialog(DriveHistoryDetailActivity.this, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                getBind().defailConfirmBtn.performClick();
            }
        });

        UIThread.executeInUIThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

    private void initUI(AllocationCompletedOne allocationCompletedOne) {

        if(allocationCompletedOne != null) {
            allocCompletedOne = allocationCompletedOne;

            //쇼퍼이름
            getBind().detailChauffeurName.setText(allocationCompletedOne.chauffeurInfo.name);

            showDriveInfo(allocationCompletedOne);
            showDrivingTime(allocationCompletedOne);
            showCarInfo(allocationCompletedOne);
            showServiceInfo(allocationCompletedOne);
            showPaymentInfo(allocationCompletedOne);

            getBind().detailScrollView.setVisibility(View.VISIBLE);

            // 고객에게 전화하기 버튼 출력
            // 도착시간 + 24시간
            if(allocCompletedOne.arrvDatetime + Global._1DAY > System.currentTimeMillis()) {
                getBind().btnCallCustomer.setVisibility(View.VISIBLE);
            }
            else {
                getBind().btnCallCustomer.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(DriveHistoryDetailActivity.this, "배달이력 정보가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTime(long timeMillis) {
        return DateUtils.getTime("yyyy.MM.dd E HH:mm", timeMillis);
    }

    /**
     * 운행정보
     */
    private void showDriveInfo(AllocationCompletedOne allocationCompletedOne) {
        try {
            if(allocationCompletedOne == null) {
                return;
            }

            if(allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CHECKOUT.toString())) {
                getBind().detailStatus.setTextColor(Color.parseColor("#00cfda"));
                getBind().detailStatus.setText("탑승완료");

            } else if(allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.NOSHOW.toString())) {
                getBind().detailStatus.setTextColor(Color.parseColor("#f97dad"));
                getBind().detailStatus.setText("승객미탑승");

            } else if(allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CANCELED.toString())) {

                if(allocationCompletedOne.cancelReasonCat != null && allocationCompletedOne.cancelReasonCat.equals("UNRUNEND"))
                {
                    getBind().detailStatus.setTextColor(Color.parseColor("#f97dad"));
                    getBind().detailStatus.setText(R.string.txt_unrun);
                }
                else {
                    getBind().detailStatus.setTextColor(Color.parseColor("#f97dad"));
                    getBind().detailStatus.setText("예약취소");
                }

            } else {
                getBind().detailStatus.setTextColor(Color.parseColor("#f97dad"));
                getBind().detailStatus.setText("예약취소");
            }


            //호출유형
            getBind().detailCallKind.setText((allocationCompletedOne.otherYn.equalsIgnoreCase("N") ? "내가타기" : "불러주기"));
            //예약요청일
            getBind().detailReservationDay.setText(getTime(allocationCompletedOne.regDatetime));
            //출발지
            StringBuilder org = new StringBuilder();
            if(!TextUtils.isEmpty(allocationCompletedOne.resvOrgPoi)) {
                org.append(allocationCompletedOne.resvOrgPoi);
            }
            if(!TextUtils.isEmpty(allocationCompletedOne.resvOrgAddress)) {
                if(!TextUtils.isEmpty(allocationCompletedOne.resvOrgPoi)) {
                    org.append("\n");
                }
                org.append(allocationCompletedOne.resvOrgAddress);
            }
            getBind().detailOrgLoc.setText(org.toString());

            //도착지
            if(!TextUtils.isEmpty(allocationCompletedOne.resvDstPoi)) {
                getBind().detailDestLoc.setText(allocationCompletedOne.resvDstPoi);
            } else {
                getBind().detailDestLoc.setText(allocationCompletedOne.resvDstAddress);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 출발시간, 도착시간, 운행시간, 운행거리
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void showDrivingTime(AllocationCompletedOne allocationCompletedOne) {
        try {
            if(allocationCompletedOne == null) {
                return;
            }

            if(allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CHECKOUT.toString())) {
                getBind().tmp12.setText("출발시간");
                getBind().tmp14.setText("도착시간");
                getBind().tmp16.setText("시간");

                getBind().detailOrgTime.setText(getTime(allocationCompletedOne.departDatetime));
                getBind().detailDestTime.setText(getTime(allocationCompletedOne.arrvDatetime));

                long totlaTime = (allocationCompletedOne.arrvDatetime - allocationCompletedOne.departDatetime) / 1000;
                int realDriveHour = (int) (totlaTime / 3600);
                int realDriveMinute = (int) (totlaTime % 3600 / 60);
                int realDriveSecond = (int) (totlaTime % 3600 % 60);

                StringBuilder time = new StringBuilder();
                if(realDriveHour > 0) time.append(String.valueOf(realDriveHour)).append("시간");
                if(realDriveMinute > 0) time.append(" ").append(String.valueOf(realDriveMinute)).append("분");
                if(realDriveSecond > 0) time.append(" ").append(String.valueOf(realDriveSecond)).append("초");
                getBind().detailDriveTime.setText(time.toString());

                double distance = Double.parseDouble(String.format("%.1f", (allocationCompletedOne.realDist.doubleValue() / 1000)));
                getBind().detailDistance.setText(String.valueOf(distance)+"km");

            } else {
                getBind().tmp12.setText("출발예정시간");
                getBind().tmp14.setText("도착예정시간");
                getBind().tmp16.setText("예상시간");

                // 예약취소
                long estmArriveTime = allocationCompletedOne.resvDatetime + (allocationCompletedOne.estmTime * 1000);

                getBind().detailOrgTime.setText(getTime(allocationCompletedOne.resvDatetime));
                getBind().detailDestTime.setText(getTime(estmArriveTime));

                int estmDriveHour = (int) (allocationCompletedOne.estmTime / 3600);
                int estmDriveMinute = (int) (allocationCompletedOne.estmTime % 3600 / 60);
                int estmDriveSecond = (int) (allocationCompletedOne.estmTime % 3600 % 60);

                StringBuilder time = new StringBuilder();
                if(estmDriveHour > 0) time.append(String.valueOf(estmDriveHour)).append("시간");
                if(estmDriveMinute > 0) time.append(" ").append(String.valueOf(estmDriveMinute)).append("분");
                if(estmDriveSecond > 0) time.append(" ").append(String.valueOf(estmDriveSecond)).append("초");
                getBind().detailDriveTime.setText(time.toString());

                double distance = Double.parseDouble(String.format("%.1f", (allocationCompletedOne.estmDist.doubleValue() / 1000)));
                getBind().detailDistance.setText(String.valueOf(distance)+"km");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 차량정보
     */
    private void showCarInfo(AllocationCompletedOne allocationCompletedOne) {
        try {
            if(allocationCompletedOne == null) {
                return;
            }

            //차종
            String carKind;
            switch (allocationCompletedOne.resvCarCat) {
                case "MIDSIZE":
                    carKind = "중형";
                    break;
                case "FULLSIZE":
                    carKind = "대형";
                    break;
                case "MOBUM":
                    carKind = "모범";
                    break;
                case "BLACK":
                    carKind = "블랙";
                    break;
                default:
                    carKind = "중형";
                    break;
            }

            boolean isCarInfo = false;
            if(allocationCompletedOne.carInfo != null) {
                isCarInfo = true;
                carKind += (" " +  allocationCompletedOne.carInfo.carName);
            }

            getBind().detailCarKind.setText(carKind);
            //차량번호
            getBind().detailCarNum.setText(isCarInfo ? allocationCompletedOne.carInfo.carNo : "-");
            //쇼퍼 이름
            getBind().detailSubChauffeurName.setText(allocationCompletedOne.chauffeurInfo.name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 서비스정보 - 동적
     */
    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    private void showServiceInfo(AllocationCompletedOne allocationCompletedOne) {

        try {
            if(allocationCompletedOne == null) {
                return;
            }

            if(allocationCompletedOne.serviceList != null && allocationCompletedOne.serviceList.size() > 0) {
                getBind().cardview03.setVisibility(View.VISIBLE);

                int id=1;
                for(int i=0; i<allocationCompletedOne.serviceList.size(); i++) {
                    TextView textViewLeft = new TextView(this);
                    textViewLeft.setId(id++);
                    textViewLeft.setTextColor(Color.parseColor("#585858"));
                    textViewLeft.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                    textViewLeft.setText(allocationCompletedOne.serviceList.get(i).name);

                    TextView textViewRight = new TextView(this);
                    textViewRight.setId(id);
                    textViewRight.setTextColor(Color.parseColor("#222222"));
                    textViewRight.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
                    textViewRight.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
                    textViewRight.setText(String.valueOf(allocationCompletedOne.serviceList.get(i).realCost) + "원");

                    if(i==0) {
                        ConstraintLayout.LayoutParams layoutParamLeft = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParamLeft.topMargin = (int)Util.convertDpToPixel(14, this);
                        layoutParamLeft.leftToLeft = R.id.parent;
                        layoutParamLeft.rightToLeft = id;
                        layoutParamLeft.topToBottom = R.id.view40;
                        layoutParamLeft.horizontalChainStyle = R.id.spread_inside;
                        textViewLeft.setLayoutParams(layoutParamLeft);

                        ConstraintLayout.LayoutParams layoutParamRight = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParamRight.baselineToBaseline = (id-1);
                        layoutParamRight.leftToRight = (id-1);
                        layoutParamRight.rightToRight = R.id.parent;
                        layoutParamRight.setMarginStart((int)Util.convertDpToPixel(30, this));
                        textViewRight.setLayoutParams(layoutParamRight);

                    } else {
                        ConstraintLayout.LayoutParams layoutParamLeft = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParamLeft.topMargin = (int)Util.convertDpToPixel(19, this);
                        layoutParamLeft.leftToLeft = R.id.parent;
                        layoutParamLeft.rightToLeft = id;
                        layoutParamLeft.topToBottom = id-2;
                        layoutParamLeft.horizontalChainStyle = R.id.spread_inside;
                        textViewLeft.setLayoutParams(layoutParamLeft);

                        ConstraintLayout.LayoutParams layoutParamRight = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParamRight.baselineToBaseline = (id-1);
                        layoutParamRight.leftToRight = (id-1);
                        layoutParamRight.rightToRight = R.id.parent;
                        layoutParamRight.setMarginStart((int)Util.convertDpToPixel(30, this));
                        textViewRight.setLayoutParams(layoutParamRight);
                    }

                    getBind().serviceLayout.addView(textViewLeft);
                    getBind().serviceLayout.addView(textViewRight);
                    id++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 결제정보
     * 운행완료, 승객미탑승 경우에만 결제정보 보여주기
     */
    @SuppressLint("SetTextI18n")
    private void showPaymentInfo(AllocationCompletedOne allocationCompletedOne) {
        try {
            if(allocationCompletedOne == null) {
                return;
            }

            getBind().cardview04.setVisibility(View.VISIBLE);

            boolean check = allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CHECKOUT.toString());

            //카드
            getBind().detailCardInfo.setText(allocationCompletedOne.cardCompanyName + " " + allocationCompletedOne.cardNo);

            //예약비
            if(allocationCompletedOne.resvCostCancelYn.equalsIgnoreCase("Y")) {
                getBind().detailReservationCost.setText("0원");
            }
            else
                getBind().detailReservationCost.setText(Util.makeStringComma(String.valueOf(allocationCompletedOne.resvCost))+"원");

            //서비스
            if(allocationCompletedOne.resvServiceAmt == null || allocationCompletedOne.resvServiceAmt.isEmpty())
                getBind().detailServiceCost.setText("0원");
            else {
                String serviceAmt;
                serviceAmt = String.valueOf(Util.makeStringComma(String.valueOf(allocationCompletedOne.resvServiceAmt)))+"원";
                getBind().detailServiceCost.setText(serviceAmt);
            }

            //택시운임
            String taxi;
            String fareCat;
            if(allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.CANCELED.toString())
                    || allocationCompletedOne.allocationStatus.equalsIgnoreCase(AppDef.AllocationStatus.NOSHOW.toString())) {
                getBind().tmp34.setText("패널티");

                if(allocationCompletedOne.penaltyAmt == null || allocationCompletedOne.penaltyAmt.isEmpty())
                    taxi = "0원";
                else
                    taxi = Util.makeStringComma(String.valueOf(allocationCompletedOne.penaltyAmt))+"원";

                fareCat = "앱 결제";
            }
            else {
                getBind().tmp34.setText("택시운임");
                taxi = Util.makeStringComma(String.valueOf(allocationCompletedOne.realTaxiFare))+"원";

                if(allocationCompletedOne.fareCat.equalsIgnoreCase("APPCARD")) {
                    fareCat = "앱 결제";
                } else {
                    fareCat = "직접 결제";
                }

            }

            getBind().detailRealTaxiFare.setText(taxi);
            getBind().detailRealTaxiFareMark.setText(fareCat);

            // 할인쿠폰
            if(!TextUtils.isEmpty(allocationCompletedOne.couponCat)) {
                if(allocationCompletedOne.couponCat.equalsIgnoreCase("FIX")) {
                    getBind().detailDiscountAmt.setText(Util.makeStringComma(String.valueOf(allocationCompletedOne.discountAmt))+"원");
                } else {
                    getBind().detailDiscountAmt.setText(Util.makeStringComma(String.valueOf(allocationCompletedOne.discountRate))+"원");
                }
            } else {
                getBind().detailDiscountAmt.setText("0원");
            }

            // 총 결제금액
            String total;
            getBind().tmp27.setText("결제정보");
            getBind().tmp38.setText("총 결제금액");
            total = Util.makeStringComma(String.valueOf(allocationCompletedOne.totalCost))+"원";
            getBind().detailRealPayAmt.setText(total);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}
