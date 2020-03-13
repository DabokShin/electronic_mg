package kst.ksti.chauffeur.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.BuildConfig;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.BaseActivity;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.OnTitleListener;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;

/**
 * BaseFragment
 */
public class NativeFragment extends Fragment {

    protected BaseActivity nativeBaseActivity;
    protected MainActivity nativeMainActivity;
    protected OnTitleListener mOnTitleListener;

    private TextView tvTitle;
    private TextView tvTitleServerChange;
    private Button btnDrawerOpen;
    private Button btnBackArrow;
    private ImageView ivDivider;
    private String arrowBack;
    private MacaronCustomDialog errorCodeDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.nativeBaseActivity = (BaseActivity) getActivity();
        this.nativeMainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UIThread.initializeHandler();

        Bundle bundle = getArguments();
        if (bundle != null) {
            arrowBack = bundle.getString("arrowBack");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = nativeBaseActivity.findViewById(R.id.tvTitle);
        tvTitleServerChange = nativeBaseActivity.findViewById(R.id.tvTitleServerChange);
        ivDivider = nativeBaseActivity.findViewById(R.id.ivDivider);
        btnDrawerOpen = nativeBaseActivity.findViewById(R.id.btnDrawerOpen);
        btnBackArrow = nativeBaseActivity.findViewById(R.id.btnTitleBack);

        if(BuildConfig.DEBUG) {
            if(Global.getDEV()) {
                String dev_type = Global.getServerType();
                tvTitleServerChange.setText(dev_type);
            } else {
                tvTitleServerChange.setText("");
            }
            tvTitleServerChange.setVisibility(View.VISIBLE);

        } else {
            tvTitleServerChange.setVisibility(View.GONE);
        }

        if ("y".equals(arrowBack)) {
            btnBackArrow.setVisibility(View.VISIBLE);
            btnDrawerOpen.setVisibility(View.GONE);
        } else {
            btnBackArrow.setVisibility(View.GONE);
            btnDrawerOpen.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Navigation 메뉴들과 타이틀 View 핸들 설정
     */
    protected void EventForTitleView(final View view) {
        switch (view.getId()) {
            case R.id.btnTitleBack:
                nativeBaseActivity.GoNativeBackStack();
                break;

            case R.id.btnDrawerOpen:
                ((MainActivity) nativeBaseActivity).OpenMenuMap();
                break;
        }
    }

    /**
     * 뒤로가기 버튼 클릭시
     */
    public void BackProcess() {
        nativeBaseActivity.GoNativeBackStack();
    }

    /**
     * 전달받은 Native 화면으로 이동
     *
     * @param fragment  Native 화면 개체
     * @param bundle    Parameter 번들
     */
    public void GoNativeScreen(NativeFragment fragment, Bundle bundle, int direction) {
        if (nativeBaseActivity != null)
            nativeBaseActivity.GoNativeScreenReplace(fragment, bundle, direction);
    }

    /**
     * 전달받은 Native 화면으로 이동, 백스택에 추가시킴
     *
     * @param fragment  Native 화면 개체
     * @param bundle    Parameter 번들
     * @param direction 화면전환 애니메이션
     */
    public  void GoNativeScreenReplaceAdd(NativeFragment fragment, Bundle bundle, int direction) {
        if (nativeBaseActivity != null) {
            nativeBaseActivity.GoNativeScreenReplaceAdd(fragment, bundle, direction);
        }
    }

    /**
     * 타이틀 선택한 정보 전달받을 Listener 등록
     *
     * @param listener 리스너
     */
    public void SetTitleListener(OnTitleListener listener) {
        this.mOnTitleListener = listener;
    }

    /**
     * 화면 타이틀 설정
     *
     * @param label 타이틀
     */
    public void SetTitle(final String label) {
        if (tvTitle != null) {
            tvTitle.setText(label);
        }
    }

    /**
     * 네비게이션과 화면 사이의 Divider 출력 처리
     *
     * @param isVisible true: 보이기, false: 숨김
     */
    public void SetDividerVisibility(final boolean isVisible) {
        if (ivDivider != null) ivDivider.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 네비게이션뷰 슬라이드 오픈 허용, 거부
     *
     * @param isEnable true: 허용, false: 거부
     */
    public void setDrawerLayoutEnable(final boolean isEnable) {
        if (nativeBaseActivity != null) {
            if(nativeBaseActivity.getDrawerLayout() != null) {
                if(isEnable) {
                    nativeBaseActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                } else {
                    nativeBaseActivity.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        }
    }

    /**
     * 901 에러코드 다이얼로그
     */
    public void showErrorCode901_Dialog(Context context, String msg) {
        if(errorCodeDialog != null && errorCodeDialog.isShowing()) {
            errorCodeDialog.dismiss();
        }

        errorCodeDialog = new MacaronCustomDialog(context, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCodeDialog.dismiss();
                nativeBaseActivity.playLoadingViewAnimation();
                nativeMainActivity.setLogout();
            }
        });

        try {
            errorCodeDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 902 에러코드 다이얼로그
     */
    public void showErrorCode902_Dialog(Context context, String msg) {
        if(errorCodeDialog != null && errorCodeDialog.isShowing()) {
            errorCodeDialog.dismiss();
        }

        errorCodeDialog = new MacaronCustomDialog(context, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCodeDialog.dismiss();
                GoNativeScreenReplaceAdd(new DrivingFragment(), null, 2);
            }
        });

        try {
            errorCodeDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 201 에러코드 다이얼로그
     */
    public void showErrorCode201_Dialog(Context context, String msg) {
        if(errorCodeDialog != null && errorCodeDialog.isShowing()) {
            errorCodeDialog.dismiss();
        }

        errorCodeDialog = new MacaronCustomDialog(context, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCodeDialog.dismiss();
                // 플래그먼트 뒤로가기(백스택)
                nativeBaseActivity.GoNativeBackStack();
            }
        });

        try {
            errorCodeDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 기타에러 다이얼로그
     */
    public void showErrorCodeEtcDialog(Context context, String msg) {
        if(errorCodeDialog != null && errorCodeDialog.isShowing()) {
            errorCodeDialog.dismiss();
        }

        errorCodeDialog = new MacaronCustomDialog(context, null, msg, "확인", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorCodeDialog.dismiss();
            }
        });

        try {
            errorCodeDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 쇼퍼 상태변경 API 호출
     *
     * @param params 파라미터
     * @param changeStatusInterface 상태변경 콜백
     */
    public void changeChauffeurStatusAndGoNextScreen(HashMap<String, Object> params, ChangeStatusInterface changeStatusInterface) {
        nativeBaseActivity.sendChauffeurStatusAndGoNextScreen(params, changeStatusInterface);
    }

    /**
     * 배차 상태변경 API 호출
     *
     * @param allocationIdx 해당배차 idx
     * @param status 배차상태
     * @param changeStatusInterface 상태변경 콜백
     */
    public void changeAllocStatusAndGoNextScreen(long allocationIdx, AppDef.AllocationStatus status, ChangeStatusInterface changeStatusInterface) {
        nativeBaseActivity.sendAllocStatusAndGoNextScreen(allocationIdx, status, changeStatusInterface);
    }

}
