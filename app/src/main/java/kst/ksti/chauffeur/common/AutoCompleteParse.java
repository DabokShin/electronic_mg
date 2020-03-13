package kst.ksti.chauffeur.common;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import kst.ksti.chauffeur.adapter.RecyclerViewAdapter;
import kst.ksti.chauffeur.model.autosearch.Poi;
import kst.ksti.chauffeur.model.autosearch.TMapSearchInfo;
import kst.ksti.chauffeur.utility.Logger;

/**
 * Created by KJH on 2017-09-06.
 */

public class AutoCompleteParse extends AsyncTask<String, Void, ArrayList<SearchEntity>> {
    private static final String TMAP_API_KEY = Global.TMAP_APIKEY;
    private ArrayList<SearchEntity> mListData;
    private RecyclerViewAdapter mAdapter;
    private int serchListTotalCount = 0;         // 검색된 리스트 총 개수
    private int page = 1;                       // T맵 목적지 리스트 검색 페이지

    public AutoCompleteParse(RecyclerViewAdapter adapter, int page) {
        this.mAdapter = adapter;
        mListData = new ArrayList<>();
        this.page = page;
    }

    @Override
    protected ArrayList<SearchEntity> doInBackground(String... word) {
        return getAutoComplete(word[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<SearchEntity> autoCompleteItems) {
        if(autoCompleteItems == null)
        {
            Logger.e("LOG2## TMap 목적지 리스트 결과값이 없다.");
            if(mAdapter.getItemCount() == 0) {
                serchListTotalCount = 0;
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }
            return;
        }

        mAdapter.setData(autoCompleteItems);
        mAdapter.setDestinationListTotalCount(serchListTotalCount);

        for(int i=0; i<autoCompleteItems.size(); i++) {
            Log.d("<PHD>", "## ["+i+"] // getTitle = " + autoCompleteItems.get(i).getTitle() + " / getAddress = " + autoCompleteItems.get(i).getAddress());
        }

        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<SearchEntity> getAutoComplete(String word) {

        try {
            String encodeWord = URLEncoder.encode(word, "UTF-8");

            String url = "https://api2.sktelecom.com/tmap/pois?version=1&searchType=all&resCoordType=WGS84GEO&format=json&appKey=" + TMAP_API_KEY + "&count=" + Global.TMAP_PAGE_LIST_COUNT + "&searchKeyword=" + encodeWord + "&page=" + page;

            URL acUrl = new URL(url);

            HttpURLConnection acConn = (HttpURLConnection) acUrl.openConnection();
            acConn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(acConn.getInputStream()));

            String line = reader.readLine();
            if (line == null) {
                return null;
            }

            reader.close();

            // 첫페이지 로딩 하는게 아니라면 리스트를 추가로 받는 것이다.
            if(page <= 1)
                mListData.clear();

            TMapSearchInfo searchPoiInfo = new Gson().fromJson(line, TMapSearchInfo.class);

            // 검색된 리스트 총 개수
            try
            {
                serchListTotalCount = Integer.parseInt(searchPoiInfo.getSearchPoiInfo().getTotalCount());
            }
            catch(Exception e)
            {
                Logger.e("LOG1## : " + e.getMessage());
                e.printStackTrace();

                serchListTotalCount = 0;
            }

            ArrayList<Poi> poi = searchPoiInfo.getSearchPoiInfo().getPois().getPoi();

            for (int i = 0; i < poi.size(); i++) {
                String fullAddr = poi.get(i).getUpperAddrName() + " " + poi.get(i).getMiddleAddrName() +
                        " " + poi.get(i).getLowerAddrName() + " " + poi.get(i).getDetailAddrName();
                mListData.add(new SearchEntity(poi.get(i).getName(), fullAddr, poi.get(i)));
            }

            acConn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mListData;
    }
}
