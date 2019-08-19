package com.example.youngran.database;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;


public class NetworkTask extends AsyncTask<Void, Void, String>{

    private String url;
    private ContentValues values;
    private String action;

    public NetworkTask(String url, ContentValues values, String action) {
        Log.i("httpreqrespon", "start network task");
        this.url = url;
        this.values = values;
        this.action = action;
    }

    @Override
    protected String doInBackground(Void... params) {
        Log.i("httpreqrespon", "do in background");

        String result; // 요청 결과를 저장할 변수.
        RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
        result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        Log.i("httpreqrespon", "result : "+result);
        switch (action){

        }
    }

}