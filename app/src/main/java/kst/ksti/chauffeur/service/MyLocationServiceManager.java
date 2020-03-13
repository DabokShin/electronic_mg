package kst.ksti.chauffeur.service;

public class MyLocationServiceManager {

    private MyLocationService myLocationService;
    private static MyLocationServiceManager instance = null;

    public static synchronized MyLocationServiceManager getInstance(){
        if(null == instance){
            instance = new MyLocationServiceManager();
        }
        return instance;
    }

    public MyLocationService getMyLocationService() {
        return myLocationService;
    }

    public void setMyLocationService(MyLocationService myLocationService) {
        this.myLocationService = myLocationService;
    }

}
