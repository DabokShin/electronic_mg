package kst.ksti.chauffeur.common;

import kst.ksti.chauffeur.BuildConfig;

public class Global {

    // 상용 apk 제작시 꼭 확인 해봐야할 Flag ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
    private static boolean isDev = true;                        // 개발 : true / 상용 : false // DEBUG 모드에서만 가능
    private static final boolean isFabricCrashReport = true;   // 리포트 발송 : true / 미발송 : false
    // ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

    // 운영 URL
//    private static final String TRACCAR_ADDRESS_RELEASE = "https://tdcs.macaront.com/osmand/?";
//    private static final String HOST_ADDRESS_RELEASE = "https://chauffeur.macaront.com";
    private static final String TRACCAR_ADDRESS_RELEASE = "http://y.tdcs.macaront.com/osmand/?";
    private static final String HOST_ADDRESS_RELEASE = "http://y.chauffeur.macaront.com";
    private static final String STATISTICS_ADDRESS_RELEASE = HOST_ADDRESS_RELEASE + "/board/serviceStat.do?_token=";   // 운행통계 웹뷰
    private static final String BALANCE_ACCOUNTS_ADDRESS_RELEASE = HOST_ADDRESS_RELEASE + "/settlement/chauffeurSettlementList.do?_token=";            // 정산내역 웹뷰, BALANCE_ACCOUNTS_PARAM와 함께 사용 해야한다.
    private static final String BALANCE_ACCOUNTS_PARAM = "&chauffeurIdx=";            // 정산내역 파라미터

    // 개발 URL
    private static final String TRACCAR_ADDRESS_DEV = "http://y.tdcs.macaront.com/osmand/?";    // 직영
    private static final String HOST_ADDRESS_DEV = "http://y.chauffeur.macaront.com";           // 직영
    private static final String STATISTICS_ADDRESS_DEV = HOST_ADDRESS_DEV + "/board/serviceStat.do?_token=";    // 직영 운행통계 웹뷰
    private static final String BALANCE_ACCOUNTS_ADDRESS_DEV = HOST_ADDRESS_DEV + "/settlement/chauffeurSettlementList.do?_token=";            // 정산내역 웹뷰, BALANCE_ACCOUNTS_PARAM와 함께 사용 해야한다.

    public static final String COMPANY_PHONENO = "18117994";
    public static final String TMAP_APIKEY = "ebb4d991-03c2-4b8b-b575-7cd1a7187a72";

    // 각종 상수값들
    public static final int TMAP_PAGE_LIST_COUNT = 20;
    public static final int LIST_LIMIT_COUNT = 20;
    public static final int FIRST_PAGE = 1;

    public static final int _1HOUR = 1000 * 60 * 60;
    public static final int _1DAY = _1HOUR * 24;

    public Global() {
    }

    public static boolean getDEV() {
        return isDev;
    }

    public static boolean getCrashReport() {
        return isFabricCrashReport;
    }

    public static void setDev(boolean setDev) {
        isDev = setDev;
    }

    public static String getBaseUrl() {
        if (BuildConfig.DEBUG) {
            if (isDev) {
                return HOST_ADDRESS_DEV;
            }
        }

        return HOST_ADDRESS_RELEASE;
    }

    public static String getTDCSUrl() {
        if (BuildConfig.DEBUG) {
            if (isDev) {
                return TRACCAR_ADDRESS_DEV;
            }
        }

        return TRACCAR_ADDRESS_RELEASE;
    }

    public static String getSTATISTICSUrl() {
        if (BuildConfig.DEBUG) {
            if (isDev) {
                return STATISTICS_ADDRESS_DEV;
            }
        }

        return STATISTICS_ADDRESS_RELEASE;
    }

    public static String getBALANCEACCOUNTSUrl() {
        if (BuildConfig.DEBUG) {
            if (isDev) {
                return BALANCE_ACCOUNTS_ADDRESS_DEV;
            }
        }

        return BALANCE_ACCOUNTS_ADDRESS_RELEASE;
    }

    public static String getBALANCE_ACCOUNTS_PARAM() {
        return BALANCE_ACCOUNTS_PARAM;
    }

    public static String getServerType()
    {
        if (BuildConfig.DEBUG) {
            if (isDev) {
                String dev_pos = "";
                dev_pos = HOST_ADDRESS_DEV.substring(7, 8);

                if (dev_pos != null) {
                    if (dev_pos.equals("t"))
                        return "직영개발";
                    else if (dev_pos.equals("p"))
                        return "가맹개발";
                    else
                        return "개발";
                }
            }
        }

        return "";
    }

    public interface INTENT_EXTRA_NAME {
        String LOG_OUT      = "logout";
        String LOG_OUT_MSG  = "logout_msg";
    }

    public interface NotiAlarmChannelID {
        String CHANNEL_LOC          = "macaron_loc";
//        String CHANNEL_FLOATING   = "macaron_floating";
//        String CHANNEL_DRIVE      = "macaron_drive";
        String CHANNEL_PUSH         = "macaron_push";
        String CHANNEL_PUSH_ALLOC   = "macaron_push_allocation";
    }

    public interface AppVersionCheck {
        String CHAUFFEUR_SYSTEM     = "CHAUFFEUR_SYSTEM";
        String CHAUFFEUR_VERSION    = "CHAUFFEUR_VERSION";
        String VERSION              = "VERSION";
        String MESSAGE              = "MESSAGE";
        String ACTION               = "ACTION";
    }

    // 쇼퍼 회원 타입
    public interface ChauffeurMemberType {
        String NONE         = "NONE";           // 기본
        String DIRECTMNG    = "DIRECTMNG";      // 직영
        String PARTNER      = "PARTNER";        // 가맹
    }

    public interface ErrorCode {
        String EC901    = "EC901";  // 퇴사처리된 쇼퍼
        String EC902    = "EC902";  // 운행중일때 고객이 예약취소시
//        String EC105    = "EC105";  // 다른사람이 해당 차량 사용시
        String EC201    = "EC201";  // 예약상세 데이터 없을때 or 운행임박시 데이터 없을때
        String E500     = "E500";  // 시스템 오류
    }

    public interface FA_EVENT_NAME {
        String CHAUFFEUR_INOFFICE   = "inoffice";       // 출근
        String CHAUFFEUR_DEPART     = "depart";         // 탑승시작
        String CHAUFFEUR_ORIGIN     = "origin";         // 출발지도착
        String CHAUFFEUR_CHECKIN    = "checkin";        // 고객탑승완료
        String CHAUFFEUR_ARRIVAL    = "arrival";        // 운행완료
        String CHAUFFEUR_ROADSALE   = "roadsale";       // 일반운행
        String PAYMENT_REQUEST      = "paymentRequest"; // 결제요청
        String API_REQUEST          = "api_request";    // 기타 API 요청
    }

    public interface DEEP_LINK {
        String NONE     = "";
        String MAIN     = "/main";
        String EXIT     = "/exit";
        String TICKET   = "/reservation/ticket";
        String DETAIL   = "/reservation/detail";
        String NEARBY   = "/reservation/nearby";
        String REQUEST  = "/reservation/request";
    }

    public interface TOP_SCREEN {
        String NONE         = "";                       // 기본 MAIN과 동일하게 잡혀 있다.
        String MAIN         = "MainActivity";           // 메인 액티비티
        String NEARBYDRIVE  = "NearByDriveFragment";    // 운행임박 프래그먼트
        String ALLOCSELECT  = "AllocSelectActivity";    // 배차대기 액티비티
    }

    public interface URI {
        String TMAP_END     = "macaron://tmapend";          // 기본 URI
        String ALLOC_SELECT = "macaron://allocselect";      // 수락배차 URI
        String ALLOC_DETAIL = "macaron://acllocdetail";     // 예약상세 URI
    }



    public interface JOIN_TYPE {
        String CHAUFFEUR    = "chauffeur";  // 쇼퍼
        String COMPANY      = "company";    // 법인
    }

    public interface SVC_STATUS {
        String APPRWAIT     = "APPRWAIT";   // 승인대기
        String AVAILABLE    = "AVAILABLE";  // 이용가능
        String BYE          = "BYE";        // 탈퇴
        String REQUEST      = "REQUEST";    // 승인요청
        String REREQUEST    = "REREQUEST";  // 재승인요청
        String APPROVED     = "APPROVED";   // 승인완료
        String INFORJCT     = "INFORJCT";   // 정보승인보류
        String CONTRJCT     = "CONTRJCT";   // 계약승인보류
    }
}
