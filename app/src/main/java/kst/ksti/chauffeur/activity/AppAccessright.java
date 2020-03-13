package kst.ksti.chauffeur.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.UIThread;
import kst.ksti.chauffeur.databinding.ActivityAppAccessrightBinding;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.MacaronCustomDialog;
import kst.ksti.chauffeur.utility.OnSingleClickListener;

public class AppAccessright extends BaseActivity<ActivityAppAccessrightBinding> {

    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int PERMISSIONS_REQUEST_ACCOUNTS = 100;
    private final int REQ_CODE_OVERLAY_PERMISSION = 101;
    private final int APPLICATION_DETAILS_SETTINGS = 102;

    private MacaronCustomDialog macaronCustomDialog;

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;
    private String a_type;

    private Runnable activityFinishedThread = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Thread.setDefaultUncaughtExceptionHandler(((MacaronApp)getApplication()).getUncaughtExceptionHandler(AppAccessright.this));

        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1);
            a_title = intent.getStringExtra("a_title");
            a_flags = intent.getBooleanExtra("a_flags", true);
            a_type = intent.getStringExtra("a_type");
        }

        setBind(R.layout.activity_app_accessright);

        getBind().btnConfirm.setOnClickListener(onSingleClickListener);              // 거절 버튼
    }

    /**
     * 권한요청(필수권한, 다른앱위에그리기 권한)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        boolean check = false;
        List<String> remainingPermissions = new ArrayList<>();

        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
                check = true;
            }
        }

        if(check) {
            requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), PERMISSIONS_REQUEST_ACCOUNTS);
        } else {
            if (!Settings.canDrawOverlays(this)) {
                requestPermissionSystemAlertWindow();
            } else {
                startLoginActivity();
            }
        }
    }

    private OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()) {
                case R.id.btnConfirm: // 예약목록
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(arePermissionsEnabled()) {
                            startLoginActivity();
                        } else {
                            requestMultiplePermissions();
                        }
                    } else {
                        startLoginActivity();
                    }
                    break;
            }
        }
    };

    /**
     * 권한허용여부 체크
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        if (!Settings.canDrawOverlays(this)) {
            return false;
        }

        return true;
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한 요청
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionSystemAlertWindow() {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        String msg = "배달 운행을 위해<br><font color='#ff1c74'>[다른 앱 위에 그리기]</font>에 대한 권한을 허용해 주셔야 합니다.<br>지금 설정해 주세요.";
        macaronCustomDialog = new MacaronCustomDialog(AppAccessright.this, null, Html.fromHtml(msg), "설정", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQ_CODE_OVERLAY_PERMISSION);
            }
        }, false);
        macaronCustomDialog.setCancelable(false);
        macaronCustomDialog.show();
    }

    /**
     * 로그인화면으로 이동
     */
    private void startLoginActivity(){
        if(!isFinishing()) {

            Intent intent = new Intent(AppAccessright.this, LoginActivity.class);

            if(alloc_idx > 0 && !TextUtils.isEmpty(a_title)) {
                intent.putExtra("a_detail", alloc_idx);
                intent.putExtra("a_title", a_title);
                intent.putExtra("a_flags", a_flags);
                intent.putExtra("a_type", a_type);

                Logger.e("## ============================= 11");
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_OVERLAY_PERMISSION: {
                if (Settings.canDrawOverlays(this)) {
                    startLoginActivity();
                } else {
                    Toast.makeText(AppAccessright.this, "앱을 정상적으로 이용하려면 권한동의 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                    UIThread.executeInUIThread(activityFinishedThread, 1000);
                }
                break;
            }
            case APPLICATION_DETAILS_SETTINGS: {
                // 권한을 다시 물어본다.
                requestMultiplePermissions();
                break;
            }

        }
    }

    /**
     * SYSTEM_ALERT_WINDOW 권한체크
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkSystemAlertWindow() {
        if (!Settings.canDrawOverlays(this)) {
            requestPermissionSystemAlertWindow();
        } else {
            startLoginActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCOUNTS:
                if(grantResults.length > 0) {
                    boolean permissionCheck = true;
                    for(int result : grantResults) {
                        // 필수권한 중 거부된 권한이 있는지 체크.
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            permissionCheck = false;
                        }
                    }

                    if(permissionCheck) {
                        checkSystemAlertWindow();

                    } else {
                        permissionsFailPopup();
//                        Toast.makeText(AppAccessright.this, "앱을 정상적으로 이용하려면 권한동의 설정이 필요합니다.", Toast.LENGTH_SHORT).show();
//
//                        UIThread.executeInUIThread(activityFinishedThread, 1000);
                    }
                }
                break;
        }
    }

    /**
     * 권한요청 거부시 팝업
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void permissionsFailPopup() {
        if(macaronCustomDialog != null && macaronCustomDialog.isShowing()) {
            macaronCustomDialog.dismiss();
        }

        macaronCustomDialog = new MacaronCustomDialog(AppAccessright.this, null, "영광무안 배달 이용권한을 허용해주세요.\n권한 설정 페이지로 이동하시겠습니까?", "설정하기", "취소", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, APPLICATION_DETAILS_SETTINGS);
            }
        },
        new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                macaronCustomDialog.dismiss();
                Toast.makeText(AppAccessright.this, "앱을 정상적으로 이용하려면 권한동의 설정이 필요합니다.", Toast.LENGTH_SHORT).show();

                UIThread.executeInUIThread(activityFinishedThread, 1000);
            }
        }, false);

        macaronCustomDialog.setCancelable(false);
        macaronCustomDialog.show();
    }
}
