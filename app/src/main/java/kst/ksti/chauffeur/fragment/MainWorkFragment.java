package kst.ksti.chauffeur.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocDetailActivity;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.MainFragmentBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ResultInoffice;
import kst.ksti.chauffeur.model.CarInfo;
import kst.ksti.chauffeur.model.Inoffice;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.service.MyLocationService;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

/**
 * 출근전 화면
 */
public class MainWorkFragment extends NativeFragment {

    private MainFragmentBinding mBind;
    private MacaronCustomDialog dialog;
    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;
    private String loginCarNo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeBaseActivity.cancelLoadingViewAnimation();

        Bundle bundle = getArguments();
        if (bundle != null) {
            alloc_idx = bundle.getLong("a_detail", -1);
            a_title = bundle.getString("a_title", "");
            a_flags = bundle.getBoolean("a_flags", true);

            pushAllocDetailActivity();
        }

        if(savedInstanceState == null) {
            ((TextView)nativeMainActivity.getBind().navView.findViewById(R.id.leftTvLogout)).setText("로그아웃");
            //nativeMainActivity.getBind().navView.findViewById(R.id.leftAccident).setVisibility(View.VISIBLE);
            nativeMainActivity.getBind().navView.findViewById(R.id.carNo).setVisibility(View.GONE);

            // 직영일때만 보여준다.
//            if(MacaronApp.chauffeur.companyVo.getSvcType().equals(Global.ChauffeurMemberType.DIRECTMNG)) {
//                nativeMainActivity.getBind().navView.findViewById(R.id.leftBalanceAccounts).setVisibility(View.GONE);   // 정산내역
//            }
//            else {
//                nativeMainActivity.getBind().navView.findViewById(R.id.leftBalanceAccounts).setVisibility(View.VISIBLE);   // 정산내역
//            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("");
        SetDividerVisibility(false);
        setDrawerLayoutEnable(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = MainFragmentBinding.bind(getView());
        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        if(MacaronApp.chauffeur != null) {
            initUI();
            initEventListener();

        } else {
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            dialog = new MacaronCustomDialog(nativeMainActivity,
                    null,
                    "쇼퍼님의 정보가 없습니다.\n앱을 다시 실행해 주세요.",
                    "확인",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!nativeMainActivity.isFinishing()) {
                                dialog.dismiss();
                                nativeMainActivity.finish();
                            }
                        }
                    });
            dialog.show();
        }
    }

    @Override
    public void onDetach() {
        nativeMainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onDetach();
    }

    /**
     * UI 초기화
     */
    private void initUI() {
        mBind.tvWorkName.setText(MacaronApp.chauffeur.name);
        mBind.tvCarNo.setText(PrefUtil.getLoginCarNo(nativeMainActivity));
        nativeMainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if(MacaronApp.chauffeur.imgUrl != null) {
            int imageSize = (int) Util.convertDpToPixel(108.3f, nativeMainActivity);

            Drawable photo;

            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                photo = nativeMainActivity.getResources().getDrawable(R.drawable.photo_default, nativeMainActivity.getTheme());
            } else {
                photo = nativeMainActivity.getResources().getDrawable(R.drawable.photo_default);
            }

            Glide.with(nativeMainActivity.getApplicationContext())
                    .load(MacaronApp.chauffeur.imgUrl)
                    .apply(new RequestOptions().override(imageSize, imageSize).circleCrop().placeholder(photo).error(photo))
                    .into(mBind.imgWorkProfile);
        }
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {

        // 차량 변경하기 버튼
        mBind.btnCarChange.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Util.hideKeyboard(nativeMainActivity);

                if(dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                dialog = new MacaronCustomDialog(nativeMainActivity, "차량 변경하기", "등록된 차량을\n삭제하시겠습니까?", "확인", "취소", carChangeClickListener, cancelClickListener);
                dialog.show();
            }
        });

        // 출근하기 버튼
        mBind.btnStartWork.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                btnActionStartWork();
            }
        });

        mBind.mainWorkLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mBind.tvCarNo.isFocusableInTouchMode()) {
                    mBind.tvCarNo.setFocusable(false);
                    mBind.tvCarNo.setFocusableInTouchMode(false);
                    Util.hideKeyboard(nativeMainActivity, mBind.tvCarNo);
                }
                return false;
            }
        });

        mBind.tvCarNo.setSelection(mBind.tvCarNo.getText().toString().length());
        mBind.tvCarNo.setFocusable(false);
        mBind.tvCarNo.setFocusableInTouchMode(false);

        mBind.tvCarNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mBind.tvCarNo.setFocusable(true);
                mBind.tvCarNo.setFocusableInTouchMode(true);
                return false;
            }
        });

        mBind.tvCarNo.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        Util.hideKeyboard(nativeMainActivity, mBind.tvCarNo);
                        mBind.tvCarNo.setFocusable(false);
                        mBind.tvCarNo.setFocusableInTouchMode(false);
                        break;
                }
                return true;
            }
        });

        mBind.tvCarNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String afterTextChanged = editable.toString();

                if (afterTextChanged.length() > 0) {
                    mBind.tvCarNo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 29);
                } else {
                    mBind.tvCarNo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                }
            }
        });

        if(TextUtils.isEmpty(PrefUtil.getLoginCarNo(nativeMainActivity))) {
            mBind.tvCarNo.setText("");
        }
    }

    /**
     * 등록된 차량 삭제 왼쪽 버튼 클릭리스너
     */
    private View.OnClickListener carChangeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();

            mBind.tvCarNo.setText("");
            mBind.tvCarNo.setFocusable(true);
            mBind.tvCarNo.setFocusableInTouchMode(true);
            mBind.tvCarNo.requestFocus();

            Util.showKeyboard(nativeMainActivity, mBind.tvCarNo);
        }
    };

    /**
     * 등록된 차량 삭제 오른쪽 버튼 클릭리스너
     */
    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    };


    /**
     * 푸시를 클릭하고나서 로그인 했을경우 바로 예약상세 화면을 띄움
     */
    private void pushAllocDetailActivity() {
        if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
            Intent intent = new Intent(nativeMainActivity, AllocDetailActivity.class);
            intent.putExtra("a_detail", alloc_idx);
            intent.putExtra("a_title", a_title);
            intent.putExtra("a_flags", a_flags);
            intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            nativeMainActivity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }

    /**
     * 출근버튼 클릭
     */
    @SuppressLint("MissingPermission")
    private void btnActionStartWork() {
        if(TextUtils.isEmpty(mBind.tvCarNo.getText().toString().trim())) {
            Toast.makeText(nativeMainActivity, "차량번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Util.chkGPS(nativeMainActivity)) {
            nativeBaseActivity.getBaseGpsDialog().show();
        } else {
            if(!nativeBaseActivity.isMyServiceRunning(MyLocationService.class)) {
                nativeBaseActivity.startLocationService();
            }

            nativeBaseActivity.playLoadingViewAnimation();
            loginCarNo = mBind.tvCarNo.getText().toString();

//            HashMap<String, Object> params = new HashMap<>();
//            params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.INOFFICE.toString());
//            params.put("carNo", loginCarNo);
//            changeChauffeurStatusAndGoNextScreen(params, changeStatusInterface);

            HashMap<String, Object> params = new HashMap<>();
            params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.INOFFICE.toString());
            params.put("carNo", loginCarNo);
            nativeBaseActivity.sendInoffice(params, resultInoffice);
        }
    }

    private ResultInoffice resultInoffice = new ResultInoffice() {
        @Override
        public void onSuccess(ResponseData<Inoffice> response) {

            String carNo = "";

            if (response.getResultCode().equals("S000")) {
                Logger.d("LOG1: 쇼퍼 출근 전환 성공");

                PrefUtil.setBackKeyCheck(nativeMainActivity, true);

                // 데이터 셋팅
                carNo = response.getData().carNo;
                MacaronApp.chauffeur.chauffeurStatusCat = response.getData().chauffeurStatusCat;

                switch(MacaronApp.chauffeur.chauffeurStatusCat)
                {
                    case "ALLOC":       // 배차
                    case "ORIGIN":      // 출발
                    case "LOAD":        // 승차
                    case "ARRIVAL":     // 도착
                    {
                        MacaronApp.isRestoreLogic = true;
                        MacaronApp.currAllocation = response.getData().allocation; // 예약배차 데이터
                    }
                    break;
                    case "ROADSALE": {
                        MacaronApp.isRestoreLogic = true;
                        MacaronApp.currStartRoadsale = response.getData().roadsale;   // 일반운행 데이터
                    }
                    break;
                }

                PrefUtil.setLoginCarNo(nativeMainActivity, carNo);

                nativeMainActivity.getBind().navView.findViewById(R.id.carNo).setVisibility(View.VISIBLE);

                // 직영일때만 보여준다.
//                if(MacaronApp.chauffeur.companyVo.getSvcType().equals(Global.ChauffeurMemberType.DIRECTMNG)) {
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftBalanceAccounts).setVisibility(View.GONE);   // 정산내역
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftRetire).setVisibility(View.VISIBLE);     // 차고지 이동 버튼
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftAccident).setVisibility(View.VISIBLE);   // 사고 접수
//                }
//                else {
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftBalanceAccounts).setVisibility(View.VISIBLE);   // 정산내역
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftRetire).setVisibility(View.GONE);        // 차고지 이동 버튼
//                    nativeMainActivity.getBind().navView.findViewById(R.id.leftAccident).setVisibility(View.GONE);      // 사고 접수
//                }

                ((TextView)nativeMainActivity.getBind().navView.findViewById(R.id.leftTvLogout)).setText("퇴근하기");
                ((TextView)nativeMainActivity.getBind().navView.findViewById(R.id.carNo)).setText(carNo);

                // 로그인 후 출근시 플로팅 버튼 활성 초기화
                PrefUtil.setOptionFloating(nativeMainActivity, true);

                // 로그인 후 출근시 TTS 활성 초기화
                PrefUtil.setOptionTTS(nativeMainActivity, true);

                MacaronApp.workStatus = AppDef.ActivityStatus.MAIN;

                GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);
                AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출근", "출근 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_INOFFICE);
            }
        }

        @Override
        public void onErrorCode(ResponseData<Inoffice> response) {
            Logger.d("LOG1 : " + response.getMessage());
        }

        @Override
        public void onError() {
            Logger.d("LOG1 : onError");
        }

        @Override
        public void onFailed(Throwable t) {
            Logger.d("LOG1 : onFailed");
        }
    };

    /**
     * 쇼퍼 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            Logger.d("출근상태 전환 성공");
            PrefUtil.setBackKeyCheck(nativeMainActivity, true);

            Gson gson = new Gson();
            CarInfo carInfo = gson.fromJson(response.getData().toString(), CarInfo.class);

            PrefUtil.setLoginCarNo(nativeMainActivity, carInfo.carNo);

            nativeMainActivity.getBind().navView.findViewById(R.id.leftAccident).setVisibility(View.VISIBLE);
            nativeMainActivity.getBind().navView.findViewById(R.id.carNo).setVisibility(View.VISIBLE);
            ((TextView)nativeMainActivity.getBind().navView.findViewById(R.id.leftTvLogout)).setText("퇴근하기");
            ((TextView)nativeMainActivity.getBind().navView.findViewById(R.id.carNo)).setText(carInfo.carNo);

            GoNativeScreenReplaceAdd(new DrivingFragment(), null, 1);
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출근", "출근 성공", "", Global.FA_EVENT_NAME.CHAUFFEUR_INOFFICE);

            MacaronApp.workStatus = AppDef.ActivityStatus.MAIN;
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
            Logger.e("출근상태 전환 실패 / " + response.getResultCode());
            nativeBaseActivity.cancelLoadingViewAnimation();
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출근", "출근 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_INOFFICE);

            switch (response.getResultCode()) {
                case Global.ErrorCode.EC901:
                    showErrorCode901_Dialog(nativeMainActivity, response.getError());
                    break;
                case Global.ErrorCode.EC902:
                    showErrorCode902_Dialog(nativeMainActivity, response.getError());
                    break;
                default:
                    showErrorCodeEtcDialog(nativeMainActivity, response.getError());
                    break;
            }
        }

        @Override
        public void onError() {
            Logger.e("출근상태 전환 실패");
            nativeBaseActivity.cancelLoadingViewAnimation();
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출근", "출근 실패", "", Global.FA_EVENT_NAME.CHAUFFEUR_INOFFICE);

            showDialog(null, "네트웍상태를 확인해 주세요.");
        }

        @Override
        public void onFailed(Throwable t) {
            Logger.e("출근상태 전환 실패 / " + t.getMessage());
            nativeBaseActivity.cancelLoadingViewAnimation();
            AnalyticsHelper.getInstance(nativeMainActivity).sendEvent("출근", "출근 실패", Util.getExceptionError(t), Global.FA_EVENT_NAME.CHAUFFEUR_INOFFICE);

            showDialog(null, "네트웍상태를 확인해 주세요.");
        }
    };

    private void showDialog(String title, String msg) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new MacaronCustomDialog(nativeMainActivity, title, msg, "확인", new View.OnClickListener() {
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

}


