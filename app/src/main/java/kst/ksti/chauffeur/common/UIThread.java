package kst.ksti.chauffeur.common;

import android.os.Handler;
import android.os.Looper;

public class UIThread {

    /**
     * main thread 이외에서 UI 를 변경하기 위한 핸들러
     */
    private static Handler m_Handler = null;

    /**
     * main thread 이외의 곳에서 UI 변경.
     *
     * @param r 수행할 일
     */
    public static void executeInUIThread(Runnable r) {
        m_Handler.post(r);
    }

    /**
     * main thread 이외의 곳에서 UI 변경.
     *
     * @param r    수행할 일
     * @param time 지연시간.
     */
    public static void executeInUIThread(Runnable r, long time) {
        m_Handler.postDelayed(r, time);
    }

    /**
     * 수행한 thread 삭제.
     *
     * @param r 삭제할 일
     */
    public static void removeUIThread(Runnable r) {
        m_Handler.removeCallbacks(r);
    }

    /**
     * 클래스 초기화.
     * 핸들러를 초기화하는 과정으로 인트로나 Application 클래스에서 초기화를 수행하도록 한다.
     */
    public static void initializeHandler() {
        if (m_Handler == null) {
            m_Handler = new Handler(Looper.getMainLooper());
        }
    }
}
