package kst.ksti.chauffeur.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.R;
import kst.ksti.chauffeur.activity.AllocSelectActivity;
import kst.ksti.chauffeur.activity.TmpSplashActivity;
import kst.ksti.chauffeur.common.AppDef;
import kst.ksti.chauffeur.common.DeepLik;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.AllocationSelectModel;
import kst.ksti.chauffeur.net.DataInterface;
import kst.ksti.chauffeur.net.ResponseData;
import kst.ksti.chauffeur.utility.Logger;
import kst.ksti.chauffeur.utility.PrefUtil;
import kst.ksti.chauffeur.utility.Util;

import static android.speech.tts.TextToSpeech.ERROR;

public class FcmListenerService extends FirebaseMessagingService {

    private NotificationCompat notificationCompatBuilder;
    private TextToSpeech tts;

    private long firstfcmAllocSelectRecvTime; // 처음으로 수락배차 FCM 받은 시간

    private long sendAllocSelectId;

    @Override
    public void onCreate() {
        super.onCreate();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
    }

//    data : {
//        "titile" : "[배차취소]",
//        "body" : "배차취소되었습니다.",
//        "clickAction" : "macaron://chauffeur/reservation/ticket?reservationId=15"
//    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Logger.d("LOG1 : remoteMessage; " + remoteMessage.getData());

//        String idx = Uri.parse(remoteMessage.getData().get("clickAction")).getQueryParameter("reservationId");
//        String url = Uri.parse(remoteMessage.getData().get("clickAction")).getPath();

        String title = "";
        String body = "";
        String clickAction = "";

        for(Map.Entry<String, String> entry : remoteMessage.getData().entrySet() ){
            if(entry.getKey().equalsIgnoreCase("title")) {
                title = entry.getValue();
            }
            if(entry.getKey().equalsIgnoreCase("body")) {
                body = entry.getValue();
            }
            if(entry.getKey().equalsIgnoreCase("clickAction")) {
                clickAction = entry.getValue();
            }
        }

        // 푸시 데이터가 있는지 체크
        if(isContentsNull(title, body, clickAction)) {
            // 딥링크에 따른 파싱 데이터
            Object object = new DeepLik().separator(clickAction);

            if (object instanceof String) {  // 데이터에 ?가 없을경우
                Logger.e("딥링크 처리가 안되어 있습니다.");

            } else {  // 데이터에 ? 가 있을경우
                HashMap<String, String> hashMap = (HashMap<String, String>) object;
                hashMap.put("title", title);
                hashMap.put("body", body);
                startDeepLinkQueary(hashMap);
            }
        }
        else
        {
            boolean isTitle = TextUtils.isEmpty(title);
            boolean isBody = TextUtils.isEmpty(body);
            boolean isClickAction = TextUtils.isEmpty(clickAction);

            if(!isTitle && !isBody) {
                if(isClickAction)
                {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("title", title);
                    hashMap.put("body", body);
                    startDeepLinkQueary(hashMap);
                }
            }
        }
    }

    private boolean isContentsNull(String a, String b, String c) {
        return (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b) && !TextUtils.isEmpty(c));
    }

    private synchronized void startDeepLinkQueary(HashMap<String, String> data) {
        if (Global.DEEP_LINK.NEARBY.equals(data.get("path"))) {
            Intent intent = new Intent();
            intent.setAction(Global.DEEP_LINK.NEARBY);
            intent.setPackage("kst.ksti.chauffeur");
            intent.putExtra("idx", data.get("reservationId"));
            sendBroadcast(intent);

        } else if(Global.DEEP_LINK.TICKET.equals(data.get("path"))) {
            sendNotification(data.get("title"), data.get("body"), data.get("reservationId"), "예약취소", Global.DEEP_LINK.TICKET);

            // 예약 취소 브로드 캐스트
            Intent intent = new Intent("action.broadcast");
            intent.putExtra("reservation_cancel", "y");
            this.sendBroadcast(intent);

        } else if(Global.DEEP_LINK.DETAIL.equals(data.get("path"))) {
            sendNotification(data.get("title"), data.get("body"), data.get("reservationId"), "예약상세", Global.DEEP_LINK.DETAIL);

        } else if(Global.DEEP_LINK.REQUEST.equals(data.get("path"))) {
            // 수락배차가 처음으로 들어왔을때만 activity 띄워준다.
            // 예약이 진행중 일때는 리스트에 넣어주기만 한다.

            long idx = 0;
            try {
                idx = Long.parseLong(data.get("reservationId"));
            }
            catch (Exception e) {
                Logger.e("LOG1 : startDeepLinkQueary Exception idx; " + e.getMessage());
                e.printStackTrace();
            }

            // 내려주는 쇼퍼 인덱스와 다르면 수락배차를 실행 시키지 않는다.
            long chauffeurIdx = 0;
            try {
                chauffeurIdx = Long.parseLong(data.get("chauffeurIdx"));
            }
            catch (Exception e) {
                Logger.e("LOG1 : startDeepLinkQueary Exception chauffeurIdx;" + e.getMessage());
                e.printStackTrace();
            }

            if(MacaronApp.chauffeur != null && chauffeurIdx != 0) {
                if(MacaronApp.chauffeur.chauffeurIdx != chauffeurIdx)
                {
                    Logger.d("LOG1 : The chauffeur numbers are different.");
                    return;
                }
            }

            // 탑 화면이 수락배차 화면이 아닐 경우, 수락배차가 없는 것으로 판단 하여 리스트를 초기화 해준다.
            // 처음 받고 5초간은 다른 화면이여도 수락배차 푸시를 받을 수 있다. 배차 정보 통신 시간 때문에 화면이 바뀌는 시간이 늦을 수 있기 때문
            if(!MacaronApp.topActivity.equals(Global.TOP_SCREEN.ALLOCSELECT) && (firstfcmAllocSelectRecvTime + 1000 * 5 < System.currentTimeMillis())) {
                MacaronApp.allocSelectList.clear();
            }

            // 수락배차 예약 현황을 리스트로 만들어준다.
            if(MacaronApp.allocSelectList == null)
                MacaronApp.allocSelectList = new ArrayList<>();

            AllocationSelectModel model = new AllocationSelectModel();
            model.allocationIdx = idx;
            model.ttsText = data.get("body");
            model.isAction = true;      // 처음껀 정보를 요청 한다음에 받은 걸로 정보 세팅 하기 때문에 true, 나머진 false
            model.fcmRecvTime = SystemClock.elapsedRealtime();

            MacaronApp.allocSelectList.add(model);

            if(MacaronApp.allocSelectList.size() == 1)
            {
                // 수락배차 Activity 화면을 띄주기전에 데이터를 받아온다.
                getAcceptAllocation();
                firstfcmAllocSelectRecvTime = System.currentTimeMillis();
            }
            else {
                firstfcmAllocSelectRecvTime = System.currentTimeMillis();
            }
        } else {
            Logger.e("딥링크 처리가 안되어 있습니다.");
            // 백그라운드에 있을때는 그 상태로 띄워준다.
            // 그렇지 않을경우는 앱 실행

            if(data.containsKey("title") && data.containsKey("body"))
            {
                sendNotification(data.get("title"), data.get("body"), data.get("title"), "배달 알림", Global.DEEP_LINK.NONE);
            }
        }
    }

    private void sendNotification(String title, String body, String idx, String allocTitle, String deepLink) {
        if(isContentsNull(title, body, idx)) {

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    Util.getNotiChId(this, Global.NotiAlarmChannelID.CHANNEL_PUSH_ALLOC, getString(R.string.notification_allocation), getString(R.string.notification_allocation)))
                    .setSmallIcon(R.drawable.chauffeur_small_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round))
                    .setPriority(NotificationCompat.PRIORITY_MAX)                       // 헤드업 알림
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setContentTitle(title)
                    .setContentText(body)      // 긴 텍스트 적용
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))      // 긴 텍스트 적용
                    //.setContentText(body)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            long alloc_index = 0;
            if(deepLink != Global.DEEP_LINK.NONE)
                alloc_index = Long.parseLong(idx);

            Intent intent = new Intent(this, TmpSplashActivity.class);
            intent.putExtra("a_detail", alloc_index);
            intent.putExtra("a_title", allocTitle);
            intent.putExtra("a_flags", false);
            intent.putExtra("a_type", "notification");
            intent.putExtra("a_deepLink", deepLink);

            // 알림 받은 문장을 읽는다.
            if(PrefUtil.getOptionTTS(getApplicationContext())) {
                // 음성입력 권한 체크
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(body, TextToSpeech.QUEUE_FLUSH, null, "Voice");
                }
                else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Voice");
                    tts.speak(body, TextToSpeech.QUEUE_FLUSH, map);
                }
            }

            Logger.d("alram " + body);

            int requestCode = (int)System.currentTimeMillis();  // requestCode 결과값을 사용 안하기 때문에 어떤 값이 들어갔는지 몰라도 된다.

            PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(Util.createID(), notificationBuilder.build());

                if( !(MacaronApp.appStatus == MacaronApp.AppStatus.FOREGROUND || MacaronApp.appStatus == MacaronApp.AppStatus.RETURNED_TO_FOREGROUND)) {
                    if(!MacaronApp.isNews) {
                        if(MyLocationServiceManager.getInstance().getMyLocationService() != null) {
                            MacaronApp.isNews = true;
                            Logger.d("LOG1 : [헤드업알림] 플로팅 버튼 애니메이션 시작");

                            if(deepLink.equals(Global.DEEP_LINK.DETAIL))
                                MyLocationServiceManager.getInstance().getMyLocationService().startBtnAnimation(Global.URI.ALLOC_DETAIL, alloc_index);
                            else
                                MyLocationServiceManager.getInstance().getMyLocationService().startBtnAnimation(Global.URI.TMAP_END, 0);
                        }
                    }
                    else    // 플로팅 공지가 있는 경우 데이터만 바꿔준다.
                    {
                        if(deepLink.equals(Global.DEEP_LINK.DETAIL))
                            MyLocationServiceManager.getInstance().getMyLocationService().floatingBtnData(Global.URI.ALLOC_DETAIL, alloc_index);
                        else
                            MyLocationServiceManager.getInstance().getMyLocationService().floatingBtnData(Global.URI.TMAP_END, 0);
                    }
                }
            }
        }
    }

    /**
     * 배차예약현황정보 요청
     */
    private void getAcceptAllocation() {
        if(MacaronApp.allocSelectList.size() <= 0)
            return;

        // 통신 할 예약 배차 id
        MacaronApp.allocSelectList.get(0).isAction = true;
        sendAllocSelectId = MacaronApp.allocSelectList.get(0).getAllocationIdx();

        HashMap<String, Object> params = new HashMap<>();
        params.put("allocationIdx", sendAllocSelectId);

        DataInterface.getInstance().getAcceptAllocation(this, params, new DataInterface.ResponseCallback<ResponseData<AllocationSchedule>>() {
            @Override
            public void onSuccess(ResponseData<AllocationSchedule> response) {
                if ("S000".equals(response.getResultCode()) && response.getData() != null &&
                    // 서버에서 수락배차 프로세스가 완료되지 않은 것들만 받을 수 있다.
                    response.getData().allocationStatus.equals(AppDef.AllocationStatus.REQUESTED.toString()) &&
                    PrefUtil.getActivityStatus(getApplicationContext()).equals(AppDef.ActivityStatus.MAIN.toString()) &&
                    MacaronApp.topActivity.equals(Global.TOP_SCREEN.MAIN))
                    {
                        Intent intent = new Intent(getApplicationContext(), AllocSelectActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("a_allocationIdx", response.getData().allocationIdx);
                        intent.putExtra("a_resvDatetime", response.getData().resvDatetime);
                        intent.putExtra("a_org", response.getData().resvOrgPoi);
                        intent.putExtra("a_orgAddress", response.getData().resvOrgAddress);
                        intent.putExtra("a_resvDstPoi", response.getData().resvDstPoi);
                        intent.putExtra("a_resvDstAddress", response.getData().resvDstAddress);
                        intent.putExtra("a_estmDist", response.getData().estmDist);
                        intent.putExtra("a_estmTime", response.getData().estmTime);
                        intent.putExtra("a_estmTotalCost", response.getData().estmTotalCost);

                        // 서비스 항목 세팅
                        StringBuilder stringBuilder = new StringBuilder();
                        if (response.getData().serviceNameList != null && response.getData().serviceNameList.size() > 0) {
                            for(int i=0; i<response.getData().serviceNameList.size(); i++) {
                                stringBuilder.append(response.getData().serviceNameList.get(i));

                                if(i < response.getData().serviceNameList.size() - 1) {
                                    stringBuilder.append(", ");
                                }
                            }

                            intent.putExtra("a_serviceKind", stringBuilder.toString());
                        }

                        startActivity(intent);
                }
                else {
                    if(removeAllocation(sendAllocSelectId) > 0)
                        getAcceptAllocation();
                }
            }

            @Override
            public void onError(ResponseData<AllocationSchedule> response) {
                if(removeAllocation(sendAllocSelectId) > 0)
                    getAcceptAllocation();
            }

            @Override
            public void onFailure(Throwable t) {
                if(removeAllocation(sendAllocSelectId) > 0)
                    getAcceptAllocation();
            }
        });
    }

    /**
     * 예약배차 리스트중 하나 삭제
     */
    public int removeAllocation(long alloc_idx) {
        for(int i = 0; i < MacaronApp.allocSelectList.size(); i++)
        {
            if(!MacaronApp.allocSelectList.get(i).isAction()) continue;     // isActio이 true 일 경우 수락/거절 요청까지 완료 한 것이다.
            if(MacaronApp.allocSelectList.get(i).getAllocationIdx() == alloc_idx)
            {
                MacaronApp.allocSelectList.remove(i);
                break;
            }
        }

        return MacaronApp.allocSelectList.size();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.i("", "onNewToken; " + s);
        PrefUtil.setPushKey(this, s);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
