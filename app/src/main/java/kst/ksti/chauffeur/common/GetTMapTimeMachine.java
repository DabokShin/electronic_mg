package kst.ksti.chauffeur.common;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import kst.ksti.chauffeur.MacaronApp;
import kst.ksti.chauffeur.listner.TMapTimeMachineCallback;
import kst.ksti.chauffeur.model.StartRoadsale;
import kst.ksti.chauffeur.model.TMapTimeMachine.Properties;
import kst.ksti.chauffeur.model.TMapTimeMachine.TmapTimeMachine;
import kst.ksti.chauffeur.utility.DateUtils;

public class GetTMapTimeMachine extends AsyncTask<Void, Void, String> {

    private TMapTimeMachineCallback mCallback;

    private StartRoadsale roadsale = new StartRoadsale();

    public GetTMapTimeMachine(HashMap<String, Object> params, final String recvPoi, final String recvAdress, final double recvLat, final double recvLon, TMapTimeMachineCallback callback)
    {
        mCallback = callback;

        try
        {
            // 일반운행 현재 내 위치 정보 셋팅
            if(params.containsKey("poi"))
                roadsale.realOrgPoi = params.get("poi").toString();
            else
                roadsale.realOrgPoi = params.get("address").toString();

            roadsale.realOrgLat = MacaronApp.lastLocation.getLatitude();
            roadsale.realOrgLon = MacaronApp.lastLocation.getLongitude();
            roadsale.realOrgAddress = params.get("address").toString();
        }
        catch(Exception e)
        {
            Log.e("오류", "내 위치 정보 오류");
        }

        // 도착지 정보
        if(recvPoi == null)
            roadsale.resvDstPoi = recvAdress;
        else
            roadsale.resvDstPoi = recvPoi;

        roadsale.resvDstAddress = recvAdress;
        roadsale.resvDstLat = recvLat;
        roadsale.resvDstLon = recvLon;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            // TMap 타임머신 api 호출
            String url = "https://api2.sktelecom.com/tmap/routes/prediction?version=1&reqCoordType=WGS84GEO&resCoordType=EPSG3857&format=json&totalValue=1";

            URL acUrl = new URL(url);

            HttpURLConnection acConn = (HttpURLConnection) acUrl.openConnection();
            acConn.setDoOutput(true);
            acConn.setDoInput(true);

            // 헤더
            acConn.setRequestMethod("POST");
            acConn.setRequestProperty("Accept", "application/json");
            acConn.setRequestProperty("Accept-Charset", "UTF-8");
            acConn.setRequestProperty("Content-Type", "application/json");
            acConn.setRequestProperty("appKey", Global.TMAP_APIKEY);

            // 바디(Json)
            HashMap jsonBody = new HashMap();
            HashMap routesInfo = new HashMap();
            HashMap departure = new HashMap();
            HashMap destination = new HashMap();

            // 내 위치
            departure.put("name", roadsale.realOrgPoi);
            departure.put("lon", roadsale.realOrgLon);
            departure.put("lat", roadsale.realOrgLat);

            // 도착지
            destination.put("name", roadsale.resvDstPoi);
            destination.put("lon", roadsale.resvDstLon);
            destination.put("lat", roadsale.resvDstLat);

            // Json 구조 세팅
            routesInfo.put("departure", departure);
            routesInfo.put("destination", destination);
            routesInfo.put("predictionType", "arrival");
            routesInfo.put("predictionTime", DateUtils.getCurrentDate("yyyy-MM-dd'T'HH:mm:ssZ"));
            jsonBody.put("routesInfo", routesInfo);

            // HashMap to Json 맵핑
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writeValueAsString(jsonBody);

            OutputStream os = acConn.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();

            int status = acConn.getResponseCode();
            InputStream error = acConn.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(acConn.getInputStream(), "UTF-8"));

            String jsonRecvData = reader.readLine();
            if (jsonRecvData == null) {
                return "FAIL";
            }

            reader.close();
            acConn.disconnect();

            // 받아온 T맵 타임머신 정보 셋팅
            Properties properties = new Gson().fromJson(jsonRecvData, TmapTimeMachine.class).getFeatures().get(0).getProperties();

            roadsale.estmTime = properties.getTotalTime();      // 일반운행: 예상 시간
            roadsale.estmDist = properties.getTotalDistance();  // 일반운행: 예상 거리
            roadsale.estmTaxiFare = properties.getTaxiFare();   // 일반운행: 예상 택시 운임

        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }

        return "OK";
    }

    @Override
    protected void onPostExecute(String result) {

        switch(result)
        {
            case "OK":
                mCallback.onSuccess(roadsale);
                break;
            case "FAIL":
                mCallback.onFailure();
                break;
        }
    }

}
