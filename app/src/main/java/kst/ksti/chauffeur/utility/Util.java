package kst.ksti.chauffeur.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.skt.Tmap.TMapAddressInfo;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kst.ksti.chauffeur.common.Global;
import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.listner.ChangeStatusInterface;
import kst.ksti.chauffeur.listner.ResultInoffice;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterface;
import kst.ksti.chauffeur.listner.ReverseGeocodingInterfaceInoffice;
import kst.ksti.chauffeur.model.AllocationSchedule;
import kst.ksti.chauffeur.model.StartRoadsale;
import retrofit2.HttpException;

public class Util {

    public static String get(String key) {
        try {
            @SuppressLint("PrivateApi")
            Class clazz = Class.forName("android.os.SystemProperties");
            if (clazz == null) {
                return "";
            }

            Method method = clazz.getDeclaredMethod("get", String.class);
            if (method == null) {
                return "";
            }

            return (String) method.invoke(null, key);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    public static void callCompany(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Global.COMPANY_PHONENO));
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 루팅 체크 관련
     *
     * @return
     */
    public static boolean rootCheck() {
        String[] arrayOfString = {"/sbin/su", "/system/su", "/system/sbin/su", "/system/xbin/su", "/data/data/com.noshufou.android.su", "/system/app/Superuser.apk"};

        int i = 0;
        while (true) {
            if (i >= arrayOfString.length) {
                return false;
            }
            if (new File(arrayOfString[i]).exists()) {
                return true;
            }
            i++;
        }
    }

    /**
     * 숫자로 된 문자 열인지 체크
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        boolean result = false;
        try {
            Double.parseDouble(str);
            result = true;
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * null체크
     *
     * @param pStr
     * @return
     */
    public static boolean isNUll(String pStr) {
        if (pStr == null || "".equals(pStr) || "null".equals(pStr) || pStr.isEmpty()) {
            return true;
        }
        return false;
    }


    /**
     * 금액 콤마 제거
     *
     * @param amount 문자열 금액
     * @return 숫자 금액
     */
    public static String getCurrencyNoComma(String amount) {
        if (!TextUtils.isEmpty(amount)) {
            try {
                return amount.replaceAll("\\D", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 스트링 널값 처리
     *
     * @param value
     * @return
     */
    public static String getNullConvert(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }

        return value;
    }

    /**
     * 화면 캡쳐 방지 설정.
     *
     * @param act 실행하려는 Activity
     */
    public static void diableCaption(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //IMLog.d("diableCaption");
            act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    /**
     * 화면 캡쳐 방지 설정 해제.
     *
     * @param act 실행하려는 Activity
     */
    public static void enableCaption(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //IMLog.d("enableCaption");
            act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }


    public static String GetLocationAddress(Context context, Location currLocation) {
        StringBuffer juso = new StringBuffer();

        try {
            Geocoder geoCoder = new Geocoder(context, Locale.KOREAN);
            double latPoint = currLocation.getLatitude();
            double lngPoint = currLocation.getLongitude();
            //  speed = (float)(myLocation.getSpeed() * 3.6);

            // 위도,경도를 이용하여 현재 위치의 주소를 가져온다.
            List<Address> addresses;
            addresses = geoCoder.getFromLocation(latPoint, lngPoint, 1);
            for (Address addr : addresses) {
                int index = addr.getMaxAddressLineIndex();
                for (int i = 0; i <= index; i++) {
                    juso.append(addr.getAddressLine(i));
                    juso.append(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return juso.toString();
    }

    public static String makeStringComma(String str) {
        try {
            if (str.length() == 0)
                return "";
            long value = Long.parseLong(str);
            DecimalFormat format = new DecimalFormat("#,###,###,###");
            return format.format(value);

        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    /**
     * 오레오 버전 알림채널 대응
     */
    public static String getNotiChId(Context context, String channel_id, String channelName, String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 사용자에게 보이는 채널의 이름
            CharSequence name = channelName;
            // 사용자에게 보이는 채널의 설명
            String description = channelDescription;
            // 푸시 타입
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
            // Sets whether notifications posted to this channel should display notification lights
            mChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            mChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            mChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            if(channel_id.equals(Global.NotiAlarmChannelID.CHANNEL_LOC)) {
                mChannel.setShowBadge(false);
            }

            mChannel.setDescription(description);

            android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        return channel_id;
    }


    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        MacaronApp.notiIDList.add(id);
        return id;
    }

    public static boolean isValidPhoneNumber(String phoneNum) {
        boolean returnValue = false;

        try {
            String regex = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phoneNum);

            if (m.matches()) {
                returnValue = true;
            }

            return returnValue;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 일단 사용안함
     * 차량 번호 유효 여부 판단
     * 1번째 패턴 12조1234 =>숫자2,한글1,숫자4
     * 2번째 패턴 서울12치1233 한글2,숫자2,한글1,숫자4
     * @param carNum
     * @return
     */
    public static boolean isValidCarNumber(String carNum) {
        boolean returnValue = false;

        try {
            String regex = "^\\d{2}[가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|허|배|호|하\\x20]\\d{4}/*$";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(carNum);
            if (m.matches()) {
                returnValue = true;
            } else {
                //2번째 패턴 처리
                regex = "^[서울|부산|대구|인천|대전|광주|울산|제주|경기|강원|충남|전남|전북|경남|경북|세종]{2}\\d{2}[가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|허|배|호|하\\x20]\\d{4}$";
                p = Pattern.compile(regex);
                m = p.matcher(carNum);
                if (m.matches()) {
                    returnValue = true;
                }
            }

            return returnValue;

        } catch (Exception e) {
            return false;
        }
    }

    public static void showKeyboard(Activity activity, View view) {
        if(activity != null && view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if(imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }
    }

    /**
     * 사용해도 되는 hide
     */
    public static void hideKeyboard(Activity activity, View view) {
        if(activity != null && view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if(imm != null) {
                try {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 사용하면 에러가 발생할수 있는 hide
     */
    public static boolean hideKeyboard(Activity activity) {
        if(activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                try {
                    imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * 키보드 hide 상태인지 체크 하고 hide 상태가 아니라면 hide로 만들어준다.
     */
    public static boolean keyboardCheckAndHide(Activity activity) {
        if(activity == null)
            return false;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm != null && imm.isAcceptingText()) {
            try {
                return imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static void lockViewTouch(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void unLockViewTouch(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static boolean chkGPS(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            if (lm != null) {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception ignored) {}

        return gps_enabled;
    }

    /**
     * GPS 현재상태 리턴
     * @return GPS상태값
     */
    public static int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String readJsonFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte bufferByte[] = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(bufferByte)) != -1) {
                outputStream.write(bufferByte, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    @SuppressLint("DefaultLocale")
    public static String getExceptionError(Throwable throwable) {
        if(throwable == null) return "";

        if(throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            return String.format("%s: %d", exception.getClass().getSimpleName(), exception.code());
        } else {
            return throwable.getClass().getSimpleName();
        }
    }

    public static boolean haslastLocation() {
        return (MacaronApp.lastLocation != null);
    }

    /**
     * 상태바의 높이를 반환
     * @param context context
     * @return statusbar height
     */
    public static int statusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if(resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Tmap에서 전달받은 위치정보를 콜백시켜준다.
     * @param activity activity
     * @param reverseGeocodingInterface Tmap에서 받은 정보를 전달받을 콜백
     * @param tmpParams 추가 입력정보가 있으면 먼저 전달받는다. 없으면 null
     * @param changeStatusInterface 상태변경 콜백정보가 있으면 전달받는다. 없으면 null
     */
    public static void getLocationInfomationParams(Activity activity, final ReverseGeocodingInterface reverseGeocodingInterface, final HashMap<String, Object> tmpParams, final ChangeStatusInterface changeStatusInterface) {
        if(!haslastLocation()) {
            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterface.onGpsError(errorParams, changeStatusInterface);
            return;
        }

        try {
            TMapView mMapView = new TMapView(activity);
            mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);

            new TMapData().reverseGeocoding(MacaronApp.lastLocation.getLatitude(), MacaronApp.lastLocation.getLongitude(), "A04", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    try {
                        HashMap<String, Object> params = new HashMap<>();

                        if(tmpParams != null && tmpParams.size() > 0) {
                            params.putAll(tmpParams);
                        }

                        params.put("lat", MacaronApp.lastLocation.getLatitude());
                        params.put("lon", MacaronApp.lastLocation.getLongitude());

                        if (tMapAddressInfo != null) {
                            if (tMapAddressInfo.strBuildingName != null) {
                                params.put("poi", tMapAddressInfo.strBuildingName);
                            }
                            params.put("address", tMapAddressInfo.strFullAddress);

                            reverseGeocodingInterface.onSuccess(params, changeStatusInterface);

                        } else {
                            reverseGeocodingInterface.onError(params, changeStatusInterface, "TMap 오류");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        HashMap<String, Object> errorParams = new HashMap<>();
                        errorParams.put("lat", 0);
                        errorParams.put("lon", 0);
                        errorParams.put("poi", "");
                        errorParams.put("address", "");

                        if(tmpParams != null && tmpParams.size() > 0) {
                            errorParams.putAll(tmpParams);
                        }

                        reverseGeocodingInterface.onError(errorParams, changeStatusInterface, "TMap 오류");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterface.onError(errorParams, changeStatusInterface, "TMap 오류");
        }
    }

    /**
     * Tmap에서 전달받은 위치정보를 콜백시켜준다.
     * @param Context context
     * @param reverseGeocodingInterface Tmap에서 받은 정보를 전달받을 콜백
     * @param tmpParams 추가 입력정보가 있으면 먼저 전달받는다. 없으면 null
     * @param changeStatusInterface 상태변경 콜백정보가 있으면 전달받는다. 없으면 null
     */
    public static void getLocationInfomationParams(Context context, final ReverseGeocodingInterface reverseGeocodingInterface, final HashMap<String, Object> tmpParams, final ChangeStatusInterface changeStatusInterface) {
        if(!haslastLocation()) {
            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterface.onGpsError(errorParams, changeStatusInterface);
            return;
        }

        try {
            TMapView mMapView = new TMapView(context);
            mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);

            new TMapData().reverseGeocoding(MacaronApp.lastLocation.getLatitude(), MacaronApp.lastLocation.getLongitude(), "A04", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    try {
                        HashMap<String, Object> params = new HashMap<>();

                        if(tmpParams != null && tmpParams.size() > 0) {
                            params.putAll(tmpParams);
                        }

                        params.put("lat", MacaronApp.lastLocation.getLatitude());
                        params.put("lon", MacaronApp.lastLocation.getLongitude());

                        if (tMapAddressInfo != null) {
                            if (tMapAddressInfo.strBuildingName != null) {
                                params.put("poi", tMapAddressInfo.strBuildingName);
                            }
                            params.put("address", tMapAddressInfo.strFullAddress);

                            reverseGeocodingInterface.onSuccess(params, changeStatusInterface);

                        } else {
                            reverseGeocodingInterface.onError(params, changeStatusInterface, "TMap 오류");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        HashMap<String, Object> errorParams = new HashMap<>();
                        errorParams.put("lat", 0);
                        errorParams.put("lon", 0);
                        errorParams.put("poi", "");
                        errorParams.put("address", "");

                        if(tmpParams != null && tmpParams.size() > 0) {
                            errorParams.putAll(tmpParams);
                        }

                        reverseGeocodingInterface.onError(errorParams, changeStatusInterface, "TMap 오류");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterface.onError(errorParams, changeStatusInterface, "TMap 오류");
        }
    }

    /**
     * Tmap에서 전달받은 위치정보를 콜백시켜준다.
     * @param activity activity
     * @param reverseGeocodingInterfaceInoffice Tmap에서 받은 정보를 전달받을 콜백
     * @param tmpParams 추가 입력정보가 있으면 먼저 전달받는다. 없으면 null
     * @param reverseGeocodingInterfaceInoffice 콜백정보가 있으면 전달받는다. 없으면 null
     */
    public static void getLocationInfomationParams(Activity activity, final ReverseGeocodingInterfaceInoffice reverseGeocodingInterfaceInoffice, final HashMap<String, Object> tmpParams, final ResultInoffice resultInoffice) {
        if(!haslastLocation()) {
            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterfaceInoffice.onGpsError(errorParams, resultInoffice);
            return;
        }

        try {
            TMapView mMapView = new TMapView(activity);
            mMapView.setSKTMapApiKey(Global.TMAP_APIKEY);

            new TMapData().reverseGeocoding(MacaronApp.lastLocation.getLatitude(), MacaronApp.lastLocation.getLongitude(), "A04", new TMapData.reverseGeocodingListenerCallback() {
                @Override
                public void onReverseGeocoding(TMapAddressInfo tMapAddressInfo) {
                    try {
                        HashMap<String, Object> params = new HashMap<>();

                        if(tmpParams != null && tmpParams.size() > 0) {
                            params.putAll(tmpParams);
                        }

                        params.put("lat", MacaronApp.lastLocation.getLatitude());
                        params.put("lon", MacaronApp.lastLocation.getLongitude());

                        if (tMapAddressInfo != null) {
                            if (tMapAddressInfo.strBuildingName != null) {
                                params.put("poi", tMapAddressInfo.strBuildingName);
                            }
                            params.put("address", tMapAddressInfo.strFullAddress);

                            reverseGeocodingInterfaceInoffice.onSuccess(params, resultInoffice);

                        } else {
                            reverseGeocodingInterfaceInoffice.onError(params, resultInoffice, "TMap 오류");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();

                        HashMap<String, Object> errorParams = new HashMap<>();
                        errorParams.put("lat", 0);
                        errorParams.put("lon", 0);
                        errorParams.put("poi", "");
                        errorParams.put("address", "");

                        if(tmpParams != null && tmpParams.size() > 0) {
                            errorParams.putAll(tmpParams);
                        }

                        reverseGeocodingInterfaceInoffice.onError(errorParams, resultInoffice, "TMap 오류");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            HashMap<String, Object> errorParams = new HashMap<>();
            errorParams.put("lat", 0);
            errorParams.put("lon", 0);
            errorParams.put("poi", "");
            errorParams.put("address", "");

            if(tmpParams != null && tmpParams.size() > 0) {
                errorParams.putAll(tmpParams);
            }

            reverseGeocodingInterfaceInoffice.onError(errorParams, resultInoffice, "TMap 오류");
        }
    }

    public static int[] getDeviceSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        return new int[]{width, height};
    }

    /**
     * 전달받은 정보로 Tmap 띄움
     * @param tMapTapi tMapTapi
     * @param allocationSchedule 현재 운행중인 예약정보
     * @param isOrg 출발지, 도착지 구분
     */
    public static boolean goTmapInvokeRoute(TMapTapi tMapTapi, AllocationSchedule allocationSchedule, boolean isOrg) {
        if(tMapTapi != null && allocationSchedule != null) {
            String szDestName;
            float lon, lat;

            if(isOrg) {
                szDestName = allocationSchedule.resvOrgAddress;
                if(!TextUtils.isEmpty(allocationSchedule.resvOrgPoi)) {
                    szDestName = allocationSchedule.resvOrgPoi;
                }
                lon = (float) allocationSchedule.resvOrgLon;
                lat = (float) allocationSchedule.resvOrgLat;

            } else {
                szDestName = allocationSchedule.resvDstAddress;
                if(!TextUtils.isEmpty(allocationSchedule.resvDstPoi)) {
                    szDestName = allocationSchedule.resvDstPoi;
                }
                lon = (float) allocationSchedule.resvDstLon;
                lat = (float) allocationSchedule.resvDstLat;
            }

            return tMapTapi.invokeRoute(szDestName, lon, lat);
        }

        return false;
    }

    /**
     * 일반운전
     * 전달받은 정보로 Tmap 띄움
     * @param tMapTapi tMapTapi
     * @param roadsale 현재 운행중인 예약정보
     * @param isOrg 출발지, 도착지 구분
     */
    public static boolean goTmapInvokeRoute(TMapTapi tMapTapi, StartRoadsale roadsale, boolean isOrg) {
        if(tMapTapi != null && roadsale != null) {
            String szDestName;
            float lon, lat;

            if(isOrg) {
                szDestName = roadsale.address;
                if(!TextUtils.isEmpty(roadsale.realOrgPoi)) {
                    szDestName = roadsale.realOrgPoi;
                }
                lon = (float) roadsale.realOrgLon;
                lat = (float) roadsale.realOrgLat;

            } else {
                szDestName = roadsale.resvDstAddress;
                if(!TextUtils.isEmpty(roadsale.resvDstPoi)) {
                    szDestName = roadsale.resvDstPoi;
                }
                lon = (float) roadsale.resvDstLon;
                lat = (float) roadsale.resvDstLat;
            }

            return tMapTapi.invokeRoute(szDestName, lon, lat);
        }

        return false;
    }

    /**
     * 현재시간과의 차이를 "시간:분"으로 나타내준다.
     */
    public static String longTimeMillisToHourMinute(long sec) {
        if (sec == 0)
            return "0시 0분";

        int diffInHour;
        int diffnMin;

        diffInHour = (int) ((sec) / ( 60 * 60 ));
        int remaining = (int) ((sec) % ( 60 * 60 ));
        diffnMin = remaining / ( 60 );

        return String.valueOf(diffInHour) + "시 " + String.valueOf(diffnMin)  + "분" ;
    }

    /**
     * 요일 구하기
     */
    public static String getDayOfWeek(long milliseconds)
    {
        Date date = new Date(milliseconds);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayNum = cal.get(Calendar.DAY_OF_WEEK);   // 요일을 구해온다.

        String convertedString = "";

        switch (dayNum ) {
            case 1: convertedString = "일"; break;
            case 2: convertedString = "월"; break;
            case 3: convertedString = "화"; break;
            case 4: convertedString = "수"; break;
            case 5: convertedString = "목"; break;
            case 6: convertedString = "금"; break;
            case 7: convertedString = "토"; break;
        }

        return convertedString;
    }

    /** Create a File for saving an image or video */
    public static String getOutputMediaFileName(Context context) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        Logger.d("Environment.DIRECTORY_PICTURES = " + Environment.DIRECTORY_PICTURES);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(""), "macaron");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Logger.d("failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String name = mediaStorageDir.getPath() + File.separator + "CAPTURE_"+ timeStamp + ".jpg";

        return name;
    }

    public static Uri fromFile(Context context, String path) {
        return FileProvider.getUriForFile(context, "kst.ksti.chauffeur.fileprovider", new File(path));
    }

    public static final int REQUEST_CAMERA = 10001;
    public static final int REQUEST_GALLERY = 10002;

    public static void showCamera(Activity activity, Uri srcUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, srcUri);
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }

    public static void showGallery(Activity activity) {
//        Crop.pickImage(activity, REQUEST_GALLERY);
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, Util.REQUEST_GALLERY);
    }


    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { //checking
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String getPath(Context context, Uri uri)
    {
        if (uri.toString().startsWith("content:")) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor == null) return null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String s = cursor.getString(column_index);
            cursor.close();
            return s;
        } else {
            return uri.getPath();
        }
    }

    /**
     * 이미지 회전을 정상적으로 수정하여 파일로 저장
     * @param uri
     * @return
     */
    public static String editImage(Context context, String uri) {
        Bitmap image = BitmapFactory.decodeFile(uri);

        try {
            // 이미지를 상황에 맞게 회전시킨다
            ExifInterface exif = new ExifInterface(uri);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = 0;

            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                exifDegree = 90;
            } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                exifDegree = 180;
            } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                exifDegree = 270;
            }

            if (exifDegree != 0 && image != null) {
                Matrix m = new Matrix();

                m.setRotate(exifDegree, (float)image.getWidth() / 2, (float)image.getHeight() / 2);

                try {
                    Bitmap converted = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);

                    if (image != converted) {
                        image.recycle();
                        image = converted;
                    }
                } catch(OutOfMemoryError ex) {
                    // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveBitmapToFileCache(context, image);
    }

    /**
     * Bitmap을 파일로 저장
     * @param bitmap
     * @return
     */
    public static String saveBitmapToFileCache(Context context, Bitmap bitmap) {
        File fileCacheItem = new File(getOutputMediaFileName(context));
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileCacheItem.getPath();
    }

    public static String convertHyphenPhoneNumber(String str) {
        if(str.startsWith("+82")) str = str.replace("+82", "0");

        String result = str.replace("-", "");
        StringBuilder builder;

        if(result.length() == 11) {
            builder = new StringBuilder(result);
            builder.insert(3, "-");
            builder.insert(8, "-");

            return builder.toString();
        }

        if(result.length() == 10) {
            builder = new StringBuilder(result);

            if(result.startsWith("02")) builder.insert(2, "-");
            else builder.insert(3, "-");

            builder.insert(7, "-");

            return builder.toString();
        }

        return PhoneNumberUtils.formatNumber(result);
    }

    public static String convertHyphenBisNumber(String bis) {
        String str = bis.replace("-", "");
        StringBuilder builder = new StringBuilder(str);

        if(str.length() < 4) return str;

        if(str.length() < 6) {
            builder.insert(3, "-");
            return builder.toString();
        }

        builder.insert(3, "-");
        builder.insert(6, "-");

        return builder.toString();
    }

    public static String convertHyphenBirthdayNumber(String birth) {
        String str = birth.replace("-", "");
        StringBuilder builder = new StringBuilder(str);

        if(str.length() < 5) return str;

        if(str.length() < 7) {
            builder.insert(4, "-");
            return builder.toString();
        }

        builder.insert(4, "-");
        builder.insert(7, "-");

        return builder.toString();
    }
}
