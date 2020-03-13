package kst.ksti.chauffeur.common;

public class AppDef {

    public final static int ACTIVITY_CLOSE = 9000;

    /*
    * LEAVE:퇴근, INOFFICE : 출근, WORK:근무대기, ALLOC:배차, LOAD:승차(고객), REST:휴식, CONNECT:접속, RETIRE:차고지이동
    *  WORK2: 출발지이동, DISCONNECT : 출근전퇴근(로그아웃), (일반운행)
    *  NONE: 상태없음
     */
    public enum ChauffeurStatus {

        NONE, LEAVE, INOFFICE, WORK, REST, LOAD, RETIRE, CONNECT, ALLOC, ACCIDENT, EXIT, DISCONNECT, ROADSALE

    }

    public enum CarStatus{
        NORMAL, ACCIDENT
    }

    //배차상태
    /*REQUESTED:배차요청, ACCEPTED:배차접수, REJECTED:배차거절, ALLOCATED:배차, CHECKIN:탑승,
      *CANCELED:AC,RJ취소, DROPPED:RQ배차요청취소, DEPART:기사출발, NEARBY:에약시간임박, ORIGIN:출발지도착,
      * ARRIVAL:운행완료, CHECKOUT:결제완료, NOSHOW:승객미탑승, ROADSALE:일반운행(클라에서만 사용))
      *   NEARBY는 쇼퍼앱에서 사용안함(푸쉬로 전송)
     */
    public enum AllocationStatus {
        NONE, REQUESTED, ACCEPTED, REJECTED, ALLOCATED, CHECKIN, CANCELED, DROPPED, DEPART, NEARBY, ORIGIN,
        ARRIVAL, CHECKOUT, NOSHOW, ROADSALE,
    }


    //MIDSIZE:중형, FULLSIZE:대형, MOBUM:모범, BLACK:블랙
    public enum ResvCarCat {
        MIDSIZE("중형"), FULLSIZE("대형"), MOBUM("모범"), BLACK("블랙");

        private String size;

        ResvCarCat(String size) {
            this.size = size;
        }

        public static String getSizeByValue(String cat) {
            for(ResvCarCat value : values()) {
                if(value.name().equals(cat)) {
                    return value.size;
                }
            }

            return "";
        }
    }
  /*  평가카테고리 (SATISFY:만족하다, KIND:친절하다, SAFE:안전하다, SERVICE:차량서비스좋다, CLEAN:차량청결하다, UNSATISFY:불만족하다,
    *        UNKIND:불친절하다, UNSAFE:불안전하다, BADSERVICE:차량서비스불만족하다, UNCLEAN:불청결하다)
    */
    public enum FeedBack {
      SATISFY, KIND, SAFE, SERVICE, CLEAN, UNSATISFY, UNKIND, UNSAFE, BADSERVICE, UNCLEAN
     }
    /*취소 개인별 카테고리 (USER:사용자, CHAUFFEUR:쇼퍼, ADMIN:관리자)
     *
     */
     public enum CancelPersonCat{
        USER, CHAUFFEUR, ADMIN
     }

   // confCat : 설정 카테고리 (CHAUFFEUR_VERSION:쇼퍼앱버전, CHAUFFEUR_SYSTEM:쇼퍼앱시스템공지)
     public enum ConfigurationCat{
       CHAUFFEUR_VERSION, CHAUFFEUR_SYSTEM
     }

     // 운임 결제 카테고리 (APPCARD : 앱내카드, OFFLINE : 직접결제)
     public enum FareCat {
         APPCARD, OFFLINE, PAID
     }

     public enum ActivityStatus {
         NONE, LOGIN, MAIN
     }

    // 그룹코드
    /*
     * CAR_CAT:택시종류, BANK_CD:은행사, AUTMAKE_CD:차량제조사, CARMODL_CD:차량모델명
     * SIGUNGU_CD:시군구, SIDO_CD:시도
     */
    public enum GroupCode {
        CAR_CAT, BANK_CD, AUTMAKE_CD, CARMODL_CD,
        SIGUNGU_CD, SIDO_CD
    }

    public enum TermsUrls {
        PRIVATE_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFPERSONAL"),
        ALLOCATION_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFPERSONAL3RDAL"),
        PAY_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFPERSONAL3RDRE"),
        LOCATION_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFLBS"),
        CHAUFFEUR_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFTERMS"),
        CURRENT_TERMS(Global.getBaseUrl() + "/board/termsDetail.do?boardCat=CFTAXILOCATION");

        private String url;

        TermsUrls(String url) {
            this.url = url;
        }

        public static String getUrlByOrdinal(int ordinal) {
            for(TermsUrls value : values()) {
                if(value.ordinal() == ordinal) {
                    return value.url;
                }
            }

            return "";
        }
    }

    public enum AuthType {
        CORPORATE, PRIVATE
    }

    // 쇼퍼등록정보 (MEMBER:기본정보, PROFILE:프로필정보, ETC:부가정보)
    public enum ChauffeurRegInfoCat {
        MEMBER, PROFILE, ETC
    }

    public enum CompanySvcStatus {
        APPRWAIT("법인등록 정보를\n확인 중에 있습니다."),
        AVAILABLE(""),
        BYE(""),
        REQUEST("법인등록 정보를\n확인 중에 있습니다."),
        REREQUEST("법인등록 정보를\n확인 중에 있습니다."),
        APPROVED("법인등록 정보를\n확인 중에 있습니다."),
        INFORJCT(""),
        CONTRJCT(""),;

        private String message;

        CompanySvcStatus(String message) {
            this.message = message;
        }

        public static String getMessageByStatus(String name) {
            for(CompanySvcStatus value : values()) {
                if(value.name().equals(name)) {
                    return value.message;
                }
            }

            return "법인등록 신청이\n완료되었습니다.";
        }

        public static CompanySvcStatus getValue(String name) {
            for(CompanySvcStatus value : values()) {
                if(value.name().equals(name)) {
                    return value;
                }
            }

            return CompanySvcStatus.REQUEST;
        }
    }

    public enum ChauffeurSvcStatus {
        APPRWAIT(""),
        AVAILABLE(""),
        BYE(""),
        REQUEST("쇼퍼등록 정보를\n확인 중에 있습니다."),
        REREQUEST("쇼퍼등록 정보를\n확인 중에 있습니다."),
        APPROVED("쇼퍼등록 정보를\n확인 중에 있습니다."),
        INFORJCT(""),
        CONTRJCT(""),;

        private String message;

        ChauffeurSvcStatus(String message) {
            this.message = message;
        }

        public static String getMessageByStatus(String name) {
            for(ChauffeurSvcStatus value : values()) {
                if(value.name().equals(name)) {
                    return value.message;
                }
            }

            return "쇼퍼등록 신청이\n완료되었습니다.";
        }

        public static ChauffeurSvcStatus getValue(String name) {
            for(ChauffeurSvcStatus value : values()) {
                if(value.name().equals(name)) {
                    return value;
                }
            }

            return ChauffeurSvcStatus.REQUEST;
        }
    }
}
