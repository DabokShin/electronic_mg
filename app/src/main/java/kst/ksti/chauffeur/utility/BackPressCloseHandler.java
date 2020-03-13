package kst.ksti.chauffeur.utility;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.activity.MainActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.fragment.MainWorkFragment;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity activity) {
        this.activity = activity;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            if(activity instanceof MainActivity) {
                Fragment fragment = ((MainActivity)activity).getCurrentFragment();
                if (fragment instanceof MainWorkFragment) {
                    ((MainActivity)activity).playLoadingViewAnimation();
                    if(MacaronApp.clientChauffeurStatus != AppDef.ChauffeurStatus.DISCONNECT)
                    {
                        ((MainActivity)activity).setEXIT(true, AppDef.ChauffeurStatus.DISCONNECT);
                        MacaronApp.clientChauffeurStatus = AppDef.ChauffeurStatus.DISCONNECT;
                    }
                } else {
                    ((MainActivity)activity).playLoadingViewAnimation();
                    ((MainActivity)activity).setLogout();
                }


            } else {
                activity.finishAffinity();
            }

            toast.cancel();
        }
    }

    public void showGuide() {
        if(activity instanceof MainActivity) {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르면 퇴근됩니다.", Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
        }

        toast.show();
    }
}
