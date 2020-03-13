package kst.ksti.chauffeur.utility;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {

    private final long MIN_CLICK_INTERVAL = 1000; // 중복 클릭 방지 시간 설정
    private long mLastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < MIN_CLICK_INTERVAL) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        onSingleClick(view);
    }

}
