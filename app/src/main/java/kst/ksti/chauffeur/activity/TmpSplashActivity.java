package kst.ksti.chauffeur.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.databinding.ActivitySplashBinding;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;

public class TmpSplashActivity extends BaseActivity<ActivitySplashBinding> {

    private long alloc_idx = -1;
    private String a_title;
    private boolean a_flags;
    private String a_type;
    private String a_deepLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(getResources().getString(R.string.begin_activity));    // 액티비티 호출 로그

        Intent intent = getIntent();
        if (intent != null) {
            alloc_idx = intent.getLongExtra("a_detail", -1);
            a_title = intent.getStringExtra("a_title");
            a_flags = intent.getBooleanExtra("a_flags", true);
            a_type = intent.getStringExtra("a_type");
            a_deepLink = intent.getStringExtra("a_deepLink");
        }

        setBind(R.layout.activity_tmpsplash);

        if(alloc_idx > 0 && !TextUtils.isEmpty(a_title) && !TextUtils.isEmpty(a_deepLink)) {
            switch (a_deepLink) {
                case Global.DEEP_LINK.TICKET:
                    startDeepLinkTicket();
                    break;

                case Global.DEEP_LINK.DETAIL:
                    startDeepLinkDetail();
                    break;

                default:
                    finish();
                    break;
            }
        } else {
            startNotDeepLink();
        }

    }

    /**
     * 예약상세
     */
    private void startDeepLinkDetail() {
        try {
            if(!isFinishing()) {
                Intent intent;

                Logger.e("## activityStatus = " + PrefUtil.getActivityStatus(getApplicationContext()));

                if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.MAIN.toString())) {
                    intent = new Intent(TmpSplashActivity.this, AllocDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);

                } else if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.LOGIN.toString())) {
                    intent = new Intent(TmpSplashActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                } else {
                    intent = new Intent(TmpSplashActivity.this, SplashActivity.class);
                }

                intent.putExtra("a_detail", alloc_idx);
                intent.putExtra("a_title", a_title);
                intent.putExtra("a_flags", a_flags);
                intent.putExtra("a_type", a_type);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    /**
     * 예약취소 액션
     */
    private void startDeepLinkTicket() {
        try {
            if(!isFinishing()) {
                Intent intent;

                Logger.e("## activityStatus = " + PrefUtil.getActivityStatus(getApplicationContext()));

                if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.MAIN.toString())) {
                    intent = new Intent(TmpSplashActivity.this, AllocDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("allocActivityType", AllocDetailActivity.ACTIVITY_TYPE_DEFAULT);

                } else if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.LOGIN.toString())) {
                    intent = new Intent(TmpSplashActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                } else {
                    intent = new Intent(TmpSplashActivity.this, SplashActivity.class);
                }

                intent.putExtra("a_detail", alloc_idx);
                intent.putExtra("a_title", a_title);
                intent.putExtra("a_flags", a_flags);
                intent.putExtra("a_type", a_type);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    /**
     * 딥링크 없을때 실행
     */
    private void startNotDeepLink() {
        try {
            if(!isFinishing()) {
                Intent intent;

                Logger.d("LOG1 ## activityStatus = " + PrefUtil.getActivityStatus(getApplicationContext()));

                if(PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.NONE.toString())) {
                    intent = new Intent(TmpSplashActivity.this, SplashActivity.class);
                    intent.putExtra("a_detail", alloc_idx);
                    intent.putExtra("a_title", a_title);
                    intent.putExtra("a_flags", a_flags);
                    intent.putExtra("a_type", a_type);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                }
                else
                {
                    finish();
                }
            }
            else
            {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
