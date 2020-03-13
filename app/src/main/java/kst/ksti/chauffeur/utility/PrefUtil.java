package kst.ksti.chauffeur.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {
    private static String PREF_NAME             = "macaron_chauffeur";
    private static String FLOATING_LOCATION_X   = "floating_location_x";
    private static String FLOATING_LOCATION_Y   = "floating_location_y";
    private static String BACK_KEY_CEHCK        = "back_key_check";
    private static String LOGIN_ID              = "login_id";
    private static String LOGIN_PHONE_NO        = "login_phone_no";
    private static String LOGIN_CAR_NO          = "login_car_no";
    private static String LOGIN_PWD             = "login_pwd";
    private static String START_TIME            = "start_time";
    private static String PUSH_KEY              = "push_key";
    private static String EXIT_STATUS           = "exit_status";
    private static String ACTIVITY_STATUS       = "activity_status";
    private static String OPTION_FLOATING       = "option_floating";
    private static String OPTION_TTS            = "option_tts";
    private static String REG_PHONE_NO          = "reg_phone_no";
    private static String REG_JOIN_TYPE         = "reg_join_type";
    private static String SIGNUP_COMPANY_TYPE   = "signup_company_type";
    private static String AREA                  = "area";

    public static void setFloatingLocationX(Context context, int loc_x) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(FLOATING_LOCATION_X, loc_x).apply();
    }

    public static int getFloatingLocationX(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(FLOATING_LOCATION_X, 0);
    }

    public static void setFloatingLocationY(Context context, int loc_y) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putInt(FLOATING_LOCATION_Y, loc_y).apply();
    }

    public static int getFloatingLocationY(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(FLOATING_LOCATION_Y, 0);
    }

    public static void setBackKeyCheck(Context context, boolean clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(BACK_KEY_CEHCK, clicked).apply();
    }

    public static boolean getBackKeyCheck(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(BACK_KEY_CEHCK, false);
    }

    public static void setLoginId(Context context, String clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(LOGIN_ID, clicked).apply();
    }

    public static String getLoginId(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(LOGIN_ID,"");
    }

    public static void setLoginPhoneNo(Context context, String clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(LOGIN_PHONE_NO, clicked).apply();
    }

    public static String getLoginPhoneNo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(LOGIN_PHONE_NO,"");
    }

    public static void setLoginCarNo(Context context, String clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(LOGIN_CAR_NO, clicked).apply();
    }

    public static String getLoginCarNo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(LOGIN_CAR_NO,"");
    }

    public static void setLoginPwd(Context context, String clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(LOGIN_PWD, clicked).apply();
    }

    public static String getLoginPwd(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(LOGIN_PWD,"");
    }

    public static void setStartTime(Context context, long startTime) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putLong(START_TIME, startTime).apply();
    }

    public static long getStartTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getLong(START_TIME,0);
    }


    public static void setPushKey(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(PUSH_KEY, key).apply();
    }

    public static String getPushKey(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(PUSH_KEY,"");
    }

    public static void setExitStatus(Context context, String clicked) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(EXIT_STATUS, clicked).apply();
    }

    public static String getExitStatus(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(EXIT_STATUS,"");
    }

    public static void setActivityStatus(Context context, String status) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(ACTIVITY_STATUS, status).apply();
    }

    public static String getActivityStatus(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(ACTIVITY_STATUS,"NONE");
    }

    public static void setOptionFloating(Context context, boolean isCheck) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(OPTION_FLOATING, isCheck).apply();
    }

    public static boolean getOptionFloating(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(OPTION_FLOATING, true);
    }

    public static void setOptionTTS(Context context, boolean isCheck) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(OPTION_TTS, isCheck).apply();
    }

    public static boolean getOptionTTS(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(OPTION_TTS, true);
    }

    public static void setRegPhoneNo(Context context, String phoneNo) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(REG_PHONE_NO, phoneNo).apply();
    }

    public static String getRegPhoneNo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(REG_PHONE_NO,"");
    }

    public static void setRegJoinType(Context context, String type) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(REG_JOIN_TYPE, type).apply();
    }

    public static String getRegJoinType(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(REG_JOIN_TYPE,"");
    }

    public static void setSignupCompanyType(Context context, String type) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(SIGNUP_COMPANY_TYPE, type).apply();
    }

    public static String getSignupCompanyType(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(SIGNUP_COMPANY_TYPE,"");
    }

    public static void setArea(Context context, String type) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(AREA, type).apply();
    }

    public static String getArea(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(AREA,"");
    }
}
