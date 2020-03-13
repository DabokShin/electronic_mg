package kst.ksti.chauffeur.utility;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import kst.ksti.chauffeur.R;


public class DateUtils {

    public static final String DATE_FORMATTED_1 = "yyyyMMdd";
    public static final String DATE_FORMATTED_2 = "HHmmss";
    public static final String DATE_FORMATTED_3 = "yyyyMMddHHmmss";
    public static final String DATE_FORMATTED_4 = "yyyyMMddHHmmssSSS";
    public static final String DATE_FORMATTED_5 = "yyyyMMddHH24miss";
    public static final String DATE_FORMATTED_6 = "MM/dd/E a hh:mm";
    public static final String HHmmssSSS = "HHmmssSSS";
    public static final String HHmm = "HH:mm";
    public static SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DATE_FORMATTED_1);
    public static SimpleDateFormat HHmmss = new SimpleDateFormat(DATE_FORMATTED_2);
    public static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat(DATE_FORMATTED_3);
    private String TAG = this.getClass().getSimpleName();

    private static final long ONE_DAY = 1000 * 60 * 60 * 24;

    private static Locale _locale;


    /**
     * 날자를 얻고자 하는 지역을 설정한다.
     *
     * @param pLocale 지역정보
     */
    public static void setLocale(Locale pLocale) {
        _locale = pLocale;
    }

    /**
     * 지역 정보를 가져온다.
     *
     * @return Locale
     */
    public static Locale getLocale() {
        return _locale;
    }

    // /**
    // * 원하는 날짜를 더한 Date String을 리턴한다.
    // *
    // * @param basicDate
    // * @param addDay
    // * @return +된 날짜
    // */
    // public static String addDate(String basicDate, int addDay) throws Exception {
    // basicDate = FormatUtil.toNumber(basicDate);
    //
    // Calendar calendar = new GregorianCalendar();
    // calendar.setTime(getStringToDate(basicDate));
    // calendar.add(Calendar.DATE, addDay);
    //
    // return getDate(calendar.getTime(), "yyyyMMdd");
    // }

    /**
     * 해당달의 마지막 날자를 얻는다.
     *
     * @param pIntYear  년
     * @param pIntMonth 월
     * @return int 마지막날짜
     */
    public static int getEndDayOfMonth(int pIntYear, int pIntMonth) {
        Calendar calendar = Calendar.getInstance();
        // --------------------------------------------------------------------------------
        calendar.set(Calendar.YEAR, pIntYear);
        calendar.set(Calendar.MONTH, pIntMonth);
        calendar.add(Calendar.MONTH, -1);
        // --------------------------------------------------------------------------------
        return calendar.getActualMaximum(Calendar.DATE);
    }

    /**
     * 해당달의 마지막 날자를 얻는다.
     *
     * @param pStrYear  년
     * @param pStrMonth 월
     * @return String 마지막날짜
     * @throws Exception
     */
    public static String getEndDayOfMonth(String pStrYear, String pStrMonth) throws Exception {
        if (Util.getNullConvert(pStrYear).equals(""))
            return "";
        return String.valueOf(DateUtils.getEndDayOfMonth(Integer.parseInt(pStrYear), Integer.parseInt(pStrMonth)));
    }

    /**
     * <pre>
     * 오늘 날자를 기준으로 format형식에 따라서 값을 반환해준다..
     * format 형식은 YYYYMMDDHH24MISS 형식으로 가져올수 있다.
     * </pre>
     *
     * @param pStrFmt String 포멧문자열
     * @return String
     */
    public static String getDate(String pStrFmt) {
        return getDate(Calendar.getInstance().getTime(), pStrFmt.replaceAll("-", ""));
    }

    /**
     * 사용자가 입력한 String Date 를 기준으로 format형식으로 반환 입력 가능 Date 형식 YYYYMMDDHHMISS,YYYYMMDD,HHMISS 3가지의 형식 잘못된 date를 입력하면 null 을반환한다.
     *
     * @param pStrDate String 사용자가 입력한 Date
     * @param pStrFmt  String 반환 받고자 하는 Date Format
     * @return String
     */
    public static String getDate(String pStrDate, String pStrFmt) {
        if (Util.isNUll(pStrDate)) {
            return pStrDate;
        } else {
            Date pDate = getStringToDate(pStrDate);

            if (pDate == null) return pStrDate;
            return getDate(getStringToDate(pStrDate), pStrFmt);
        }
    }

    /**
     * <pre>
     * 사용자가 입력한 Date 를 format 형식에따라 값을 반환한다.
     *  format 형식은 YYYYMMDDHH24MISS 형식으로 가져올수 있다.
     * </pre>
     *
     * @param pDate   Date 사용자가 Date
     * @param pStrFmt String Date Format
     * @return String
     */
    public static String getDate(Date pDate, String pStrFmt) {
        StringBuffer sbFormat = new StringBuffer();
        if (Util.getNullConvert(pStrFmt).equals(""))
            return "";
        pStrFmt = pStrFmt.toLowerCase();
        int i;
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("mmm")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("MMM").append(pStrFmt.substring(i + 3));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);
        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("eee")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("EEE").append(pStrFmt.substring(i + 3));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);

        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("g")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("G").append(pStrFmt.substring(i + 1));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);

        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("hh24")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("HH").append(pStrFmt.substring(i + 4));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);
        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("ms")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("S").append(pStrFmt.substring(i + 2));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);
        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("mm")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("MM").append(pStrFmt.substring(i + 2));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);
        }
        // --------------------------------------------------------------------------------
        if ((i = pStrFmt.indexOf("mi")) != -1) {
            sbFormat.append(pStrFmt.substring(0, i)).append("mm").append(pStrFmt.substring(i + 2));
            pStrFmt = sbFormat.toString();
            sbFormat.setLength(0);

        }
        // --------------------------------------------------------------------------------
        if (getLocale() != null) {
            return new SimpleDateFormat(pStrFmt, getLocale()).format(pDate);
        } else {
            return new SimpleDateFormat(pStrFmt).format(pDate);
        }
    }

    /**
     * 입력받은 날짜 형식의 문자열을 영문 형식으로 날짜를 가져온다.
     *
     * @param pStrDate YYYYMMDD
     * @return String
     */
    public static String getEngFormatDate(String pStrDate) {
        if (Util.getNullConvert(pStrDate).equals(""))
            return "";
        if (pStrDate.length() != 8) {
            return pStrDate;
        }
        // --------------------------------------------------------------------------------
        String strYear = "";
        String strMonth = "";
        String strDate = "";
        // --------------------------------------------------------------------------------
        strYear = pStrDate.substring(0, 4);
        strMonth = pStrDate.substring(4, 6);
        strDate = pStrDate.substring(6, 8);
        // --------------------------------------------------------------------------------
        HashMap<String, String> hm = new HashMap<String, String>();
        // --------------------------------------------------------------------------------
        hm.put("1", "January");
        hm.put("2", "February");
        hm.put("3", "March");
        hm.put("4", "April");
        hm.put("5", "May");
        hm.put("6", "June");
        hm.put("7", "July");
        hm.put("8", "August");
        hm.put("9", "September");
        hm.put("10", "October");
        hm.put("11", "November");
        hm.put("12", "December");
        // --------------------------------------------------------------------------------
        strMonth = hm.get(String.valueOf(Integer.parseInt(strMonth)));
        // --------------------------------------------------------------------------------
        return strMonth + "," + strDate + " " + strYear;
    }

    /**
     * 문자열 날짜를 Date로 변환한다.
     *
     * @param pStrDate 날짜형식문자열
     * @return Date
     */
    public static Date getStringToDate(String pStrDate) {
        Calendar calendar = Calendar.getInstance();
        // --------------------------------------------------------------------------------
        pStrDate = pStrDate.trim();
        // --------------------------------------------------------------------------------
        if (pStrDate == null) {
            return null;
        }
        // --------------------------------------------------------------------------------
        if (pStrDate.length() == 8) {
            if (pStrDate.equals("00000000")) {
                return null;
            }
            // --------------------------------------------------------------------------------
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6)));
        } else if (pStrDate.length() == 14)
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6, 8)), Integer.parseInt(pStrDate.substring(8, 10)),
                    Integer.parseInt(pStrDate.substring(10, 12)), Integer.parseInt(pStrDate.substring(12)));
        else if (pStrDate.length() == 6) {
            pStrDate = getDate("YYYYMMDD") + pStrDate;
            // --------------------------------------------------------------------------------
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6, 8)), Integer.parseInt(pStrDate.substring(8, 10)),
                    Integer.parseInt(pStrDate.substring(10, 12)), Integer.parseInt(pStrDate.substring(12)));
        } else {
            return null;
        }
        // --------------------------------------------------------------------------------
        return calendar.getTime();
    }

    /**
     * 오늘날자를 기준으로 flag에 해당하는 값을 vlaue만큼 더하거나 빼서 format형식으로 반환한다.
     *
     * @param pStrFmt      String Format 형식
     * @param pChrTermFlag char 기준 Flag('Y':년,'M':월,'W':주,'D'일)
     * @param pIntVal      int 가감 계산값
     */
    public static String getDate(String pStrFmt, char pChrTermFlag, int pIntVal) {
        if (Util.getNullConvert(pStrFmt).equals(""))
            return "";
        return getDate(getDate("YYYYMMDD"), pStrFmt, pChrTermFlag, pIntVal);
    }

    /**
     * 사용자가 입력한 String Date 를 flag에 해당하는 값을 vlaue만큼 더하거나 빼서 format형식으로 반환한다. 입력 가능 Date 형식 YYYYMMDDHHMISS,YYYYMMDD,HHMISS 3가지의 형식 잘못된 date를 입력하면 null 을반환한다.
     *
     * @param pStrDate     String 사용자가 입력한 Date
     * @param pStrFmt      String 반환 받고자 하는 Date Format
     * @param pChrTermFlag char 기준 Flag('Y':년,'M':월,'W':주,'D'일)
     * @param pIntVal      int 가감 계산값
     */
    public static String getDate(String pStrDate, String pStrFmt, char pChrTermFlag, int pIntVal) {
        Calendar calendar = Calendar.getInstance();
        if (pStrDate == null) {
            return "";
        }
        if (pStrDate.length() == 8) {
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6)));
        } else if (pStrDate.length() == 14) {
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6, 8)), Integer.parseInt(pStrDate.substring(8, 10)),
                    Integer.parseInt(pStrDate.substring(10, 12)), Integer.parseInt(pStrDate.substring(12)));
        } else if (pStrDate.length() == 6) {
            pStrDate = getDate("YYYYMMDD") + pStrDate;
            calendar.set(Integer.parseInt(pStrDate.substring(0, 4)), Integer.parseInt(pStrDate.substring(4, 6)) - 1,
                    Integer.parseInt(pStrDate.substring(6, 8)), Integer.parseInt(pStrDate.substring(8, 10)),
                    Integer.parseInt(pStrDate.substring(10, 12)), Integer.parseInt(pStrDate.substring(12)));
        } else {
            return pStrDate;
        }
        // --------------------------------------------------------------------------------
        switch (pChrTermFlag) {
            case 89: /* 'Y' */
                calendar.add(Calendar.YEAR, pIntVal);
                if (pIntVal < 0) {
                    calendar.add(Calendar.DATE, 1);
                }
                break;
            case 77: /* 'M' */
                calendar.add(Calendar.MONTH, pIntVal);
                if (pIntVal < 0) {
                    calendar.add(Calendar.DATE, 1);
                }
                break;
            case 87: /* 'W' */
                calendar.add(Calendar.WEEK_OF_MONTH, pIntVal);
                break;
            case 68: /* 'D' */
                calendar.add(Calendar.DATE, pIntVal);
                break;
        }
        // --------------------------------------------------------------------------------
        return getDate(calendar.getTime(), pStrFmt);
    }


    /**
     * 두 날짜사이의 일수차이 구함
     *
     * @param pFromDate Date 시작일자
     * @param pToDate   Date 끝일자
     */
    public static int getDayBetween(Date pFromDate, Date pToDate) {
        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();
        Calendar tmpCal = Calendar.getInstance();
        // --------------------------------------------------------------------------------
        fromCal.setTime(pFromDate);
        toCal.setTime(pToDate);
        // --------------------------------------------------------------------------------
        int nFromYear = fromCal.get(Calendar.YEAR);
        int nToYear = toCal.get(Calendar.YEAR);
        int nFromDate = fromCal.get(Calendar.DAY_OF_YEAR);
        int nToDate = toCal.get(Calendar.DAY_OF_YEAR);
        int nCheckDate = 0;
        // --------------------------------------------------------------------------------
        // 이전 년도에대한 일자를 계산한다.
        // --------------------------------------------------------------------------------
        for (int i = nFromYear; i < nToYear; i++) {
            tmpCal.set(i, Calendar.DECEMBER, 31);
            nCheckDate += tmpCal.get(Calendar.DAY_OF_YEAR);
        }
        // --------------------------------------------------------------------------------
        return (nCheckDate + nToDate - nFromDate);
    }

    /**
     * 포멧에 맞는 현재 날짜를 반환한다.
     *
     * @param pStrFmt 문자열포멧
     * @return String
     */
    public static String getCurrentDate(String pStrFmt) {
        if (Util.getNullConvert(pStrFmt).equals(""))
            return "";
        SimpleDateFormat sf = new SimpleDateFormat(pStrFmt);
        // --------------------------------------------------------------------------------
        return sf.format(new Date());
    }

    /**
     * 포멧에 맞는 날짜를 반환한다.
     *
     * @param pStrFmt 문자열포멧
     * @return String
     */
    public static String getDate(String pStrFmt, Date date) {
        if (Util.getNullConvert(pStrFmt).equals(""))
            return "";
        SimpleDateFormat sf = new SimpleDateFormat(pStrFmt);
        // --------------------------------------------------------------------------------
        return sf.format(date);
    }


    /**
     * 입력받은 날짜 형식의 문자열을 영문 형식으로 날짜를 가져온다.
     *
     * @param pStrDate YYYYMMDD
     * @param pStrFmt  -
     * @return String
     */
    public static String getFormatDate(String pStrDate, String pStrFmt, int pStrSize) {
        String sReturn;

        if (Util.getNullConvert(pStrDate).equals(""))
            return "";
        if (pStrDate.length() != 6 && pStrDate.length() != 8) {
            return pStrDate;
        }
        // --------------------------------------------------------------------------------
        String strYear = "";
        String strMonth = "";
        String strDate = "";
        // --------------------------------------------------------------------------------
        if(pStrSize == 6) {
            strYear = pStrDate.substring(0, 2);
            strMonth = pStrDate.substring(2, 4);
            strDate = pStrDate.substring(4, 6);
        }
        else if(pStrSize == 8) {
            strYear = pStrDate.substring(0, 4);
            strMonth = pStrDate.substring(4, 6);
            strDate = pStrDate.substring(6, 8);
        }

        sReturn = strYear + pStrFmt + strMonth + pStrFmt + strDate;
        return sReturn;

    }

    /**
     * 입력받은 날짜 형식의 숫자를 YYYYMMDD 날짜를 가져온다.
     *
     * @param pYear
     * @param pMonth
     * @param pDate
     * @return String
     */
    public static String getFormatDate(int pYear, int pMonth, int pDate) {
        String sReturn;

        if (pYear < 0 || pMonth < 1 || pDate < 1)
            return "";
        // --------------------------------------------------------------------------------
        String strYear = "";
        String strMonth = "";
        String strDate = "";
        // --------------------------------------------------------------------------------
        strYear = pYear + "";

        if (pMonth < 10) {
            strMonth = "0" + String.valueOf(pMonth);
        } else {
            strMonth = String.valueOf(pMonth);
        }

        if (pDate < 10) {
            strDate = "0" + String.valueOf(pDate);
        } else {
            strDate = String.valueOf(pDate);
        }

        sReturn = strYear + strMonth + strDate;
        return sReturn;

    }

    public static int getCurrentYear() {
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.YEAR);

    }

    public static int getCurrentMonth() {
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.MONTH) + 1;

    }

    public static int getCurrentDay() {
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.DAY_OF_MONTH);

    }

    public static int getAMPM(){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.AM_PM);

    }

    public static int getCurrentHour(){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.HOUR);
    }

    public static int getCurrentMin(){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        return calendar.get(Calendar.MINUTE);
    }



    public static long getDiffofCurrentTimeFrom(long allocTime) {

        long currTime = System.currentTimeMillis();
        return (currTime - allocTime);

    }

    public static String getTime(String DateFormat, long timeMillis) {
        Date date = new Date(timeMillis);

        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat);
        return sdf.format(date);
    }

    public static String getAcceptAllocationTime(Context context, long timeMillis) {
        String time = "";
        String days = "";
        String resvDate = DateUtils.getTime("MM/dd", timeMillis);    // 예약 날짜
        String nowDate = DateUtils.getCurrentDate("MM/dd");             // 현재 날짜

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, 1);
        Date tomorrow = calendar.getTime();
        String tomorrowDate = DateUtils.getDate("MM/dd", tomorrow);
        String afterDay = "" + afterDay(timeMillis);

        if(resvDate.equals(nowDate))
            days = "[오늘] ";
        else if(resvDate.equals(tomorrowDate))
            days = "[내일] ";
        else
            days = "[D+" + afterDay + "] ";

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/E a hh:mm");
        Date date = new Date(timeMillis);

        //time = days + sdf.format(date);

        SimpleDateFormat sdfMonthDate = new SimpleDateFormat("MM/dd/E ");
        SimpleDateFormat sdfAmpm = new SimpleDateFormat("a");
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm");

        String strMonthDate = sdfMonthDate.format(date);
        String ampm;
        String strTime = sdfTime.format(date);

        if(sdfAmpm.format(date).equals("오전")) {
            ampm = context.getResources().getString(R.string.am);
            strTime = strTime.replace("12", "00");
        }
        else {
            ampm = context.getResources().getString(R.string.pm);
        }

        time = days + strMonthDate + ampm + strTime;

        return time;
    }

    /*
     * 현재 날짜보다 며칠 뒤인지 날짜 가져오기
     */
    public static int afterDay(long date) {
        String resvDate = DateUtils.getTime("MM/dd", date);    // 예약 날짜
        String nowDate = DateUtils.getCurrentDate("MM/dd");       // 현재 날짜
        Date d1 = new Date();
        Date d2 = new Date();
        long diff = 0;
        int day = 0;

        try {
            SimpleDateFormat f1 = new SimpleDateFormat("MM/dd", Locale.KOREA);
            SimpleDateFormat f2 = new SimpleDateFormat("MM/dd", Locale.KOREA);
            d1 = f1.parse(resvDate);
            d2 = f2.parse(nowDate);
        }
        catch(Exception e)
        {
            Logger.e("LOG1 : d1 - " + resvDate + ", d2 - " + nowDate);
        }

        diff = d1.getTime() - d2.getTime();
        day = (int)(diff / ONE_DAY);

        return day;
    }

    /*
    * 해당 milliseconds를 날짜로 변환해준다.
    * ex) 2019년 1월 22일 오전 9시 48분
     */
    public static void getDate(long milliSeconds){
        String amPm = "";
        if (milliSeconds == 0) {
            return ;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH + 1);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);


        if (calendar.get(Calendar.AM_PM) == 0)
            amPm = "오전";
        else
            amPm = "오후";
    }

  //  Log.d(TAG, year + "년 " + month + "월 " + day + "일 " + amPm + " " + hour + "시 " + min + "분");

}
