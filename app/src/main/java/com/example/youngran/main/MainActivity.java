package com.example.youngran.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.youngran.R;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.web_view) WebView webView;

    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;
    final int REQUEST_READ_PHONE_STATE = 0;
    String phoneNum = "";
    String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        receiveFCM();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        token = FirebaseInstanceId.getInstance().getToken();
        Log.i("gomgomKim", "Token:" + token);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }


        TelephonyManager telManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneNum = telManager.getLine1Number();
        if(phoneNum.startsWith("+82")){
            phoneNum = phoneNum.replace("+82", "0");
        }

        Log.i("gomgomKim", "phone:" + phoneNum);
//        sendPostToFCM( token,"FCM 테스트");

        setIsNetwork();

//        addUserData(phoneNum, token);
        createWebView();


    }

    public void createWebView(){
        // 자바스크립트 허용
        webView.getSettings().setJavaScriptEnabled(true);
        // 웹뷰 실행
        webView.loadUrl("http://youngran.bitlworks.co.kr/Adm/working/login.php");
        // 크롬기능 추가
        webView.setWebChromeClient(new WebChromeClient());
        // 페이지 이동
        webView.setWebViewClient(new WebViewClientClass());
        // 자바스크립트와 연동
        webView.addJavascriptInterface(new JavaScriptPasser(),"Android");
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("gomgomKim", url);
            view.loadUrl(url);
            return true;
        }
    }


    // 뒤로가기
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 웹뷰에서 뒤로가기 버튼을 누르면 뒤로가짐
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // webview -> android 이벤트 전송받음
    public void getWebData() {
        Log.i("gomgomKim", "get user data");
        addUserData(phoneNum, token);
    }

    public class JavaScriptPasser {

        public JavaScriptPasser() {

        }

        @JavascriptInterface
        public void getUserData() {
            Log.i("gomgomKim", "실행됨");
//            Log.i("gomgomKim", "customer id : "+ customer_id);
            getWebData();
        }


    }

    public void receiveFCM(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 키잠금 해제 및 화면 켜기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name);  // 앱의 이름
        builder.setMessage(getIntent().getStringExtra("msg")); // 넘겨받은 메시지 제목
        builder.setCancelable(false);
        builder.setPositiveButton("내용 보기", (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("닫기", (dialog, which) -> finish());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    public void setIsNetwork(){
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){

        } else{
            // alert
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());
            alert.setMessage("네트워크가 연결되지 않았습니다. 일부 기능이 제한될 수 있습니다.");
            alert.show();
        }
    }

    public void addUserData(String user_phone, String token){
        int version = android.os.Build.VERSION.SDK_INT;
        Log.i("gomgomKim sdk version:",version+"");
        Log.i("gomgomKim", "add user data");
        if(version > 8){
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        }
        try {
            //여기 주소를 바꿔주면 된다. 데이터를 추가하려면 변수를 추가 해서 이어붙이면 된다. &기호로 이어붙인다. ex) name=les&num=20130610&phone=1111
            URL url = new URL("http://youngran.bitlworks.co.kr/API/working/insert.user.uuid.php?user_phone="+user_phone+"&user_uuid="+token);

            //url경로를 열어준다.
            URLConnection conn = url.openConnection();

            //해당 url로 접속한다.
            conn.getInputStream();

            Log.i("gomgomKim","go");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("gomgomKim","no");
        }
    }

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAAm3gP_rU:APA91bG-6v2fhNtO7OoX4Jpyx7YHOlVGogTUtKuADEwt4clCBRFVvFm72cxT4ayxAEmGZxvZvrR33SDCGv_-Udu5VWIAozdUO_GSxv9FreSvAmAtRafdgWDR2EgGsspdPnajR9UwIdaa";
    private void sendPostToFCM(final String to_fcm, final String message) {
        new Thread(() -> {
            try {
                // FMC 메시지 생성 start
                JSONObject root = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("body", message);
                notification.put("title", "Youngran");
                root.put("notification", notification);
                root.put("to", to_fcm);
                // FMC 메시지 생성 end

                URL Url = new URL(FCM_MESSAGE_URL);
                HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(root.toString().getBytes("utf-8"));
                os.flush();
                conn.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    ////////////////////////////////////////////////////////////////////////////////////

}


