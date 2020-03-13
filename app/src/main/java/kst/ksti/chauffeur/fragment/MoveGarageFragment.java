package kst.ksti.chauffeur.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AnalyticsHelper;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.FrMoveGarageBinding;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

/**
 * 차고지 이동중 화면
 */
public class MoveGarageFragment extends NativeFragment implements OnTitleListener {

    private FrMoveGarageBinding mBind;
    private MacaronCustomDialog dialog;
    private long startMoveGarageTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
        }

        startMoveGarageTime = SystemClock.elapsedRealtime();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_move_garage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_fragment));    // 프래그먼트 호출 로그

        SetTitle("");
        SetDividerVisibility(false);
        SetTitleListener(this);
        setDrawerLayoutEnable(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AnalyticsHelper.getInstance(getContext()).sendScreenFromJson(nativeMainActivity, getClass().getSimpleName());

        mBind = FrMoveGarageBinding.bind(getView());

        initEventListener();

        nativeBaseActivity.cancelLoadingViewAnimation();
        UIThread.executeInUIThread(mTickExecutor, 100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        UIThread.removeUIThread(mTickExecutor);
    }

    /**
     * 차고지이동 타이머 쓰레드
     */
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            setMoveGarageTime();
            UIThread.executeInUIThread(mTickExecutor, 100);
        }
    };

    /**
     * 차고지이동 계산하여 화면에 노출
     */
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setMoveGarageTime() {
        long time   = (startMoveGarageTime < 0) ? 0 : (SystemClock.elapsedRealtime() - startMoveGarageTime);
        int hour    = (int) (time / (1000*60*60)) % 24;
        int minutes = (int) (time / (1000*60)) % 60;
        int seconds = (int) (time / 1000) % 60;

        String hourText = (hour>0) ? hour+":" : "";
        String minutesText = String.format("%02d", minutes) + ":";
        String secondsText = String.format("%02d", seconds);

        mBind.duration.setText(hourText + minutesText + secondsText);
    }

    /**
     * 이벤트 리스너 관리
     */
    private void initEventListener() {
        mBind.title.btnDrawerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventForTitleView(v);
            }
        });

        mBind.btnDriving.setOnClickListener(onSingleClickListener);
        mBind.btnLeave.setOnClickListener(onSingleClickListener);
    }

    /**
     * 뒤로가기 버튼 클릭시
     */
    public void backProcess() {
        nativeBaseActivity.playLoadingViewAnimation();

        HashMap<String, Object> params = new HashMap<>();
        params.put("chauffeurStatusCat", AppDef.ChauffeurStatus.WORK.toString());
        changeChauffeurStatusAndGoNextScreen(params, changeStatusInterface);
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnLeave:
                    confirmLeave();
                    break;

                case R.id.btnDriving:
                    backProcess();
                    break;
            }
        }
    };

    /**
     * 쇼퍼 상태변경 콜백
     */
    private ChangeStatusInterface changeStatusInterface = new ChangeStatusInterface() {
        @Override
        public void onSuccess(ResponseData<Object> response) {
            GoNativeScreenReplaceAdd(new DrivingFragment(), null, 2);
        }

        @Override
        public void onErrorCode(ResponseData<Object> response) {
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
