package com.siren.client;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * https访问
 * Created by Siren on 2022/9/7.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "CertSSH";
    private final static String REQUEST_URL = "https://10.150.184.190:8080/request";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_with_cert).setOnClickListener(this);
        findViewById(R.id.btn_without_cert).setOnClickListener(this);
    }

    private void post(OkHttpClient client) {
        client.newCall(new Request.Builder()
                .url(REQUEST_URL)
                .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        String message = e.getMessage();
                        Log.d(TAG, "onFailure: " + message);
                        showToast(message);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: " + body);
                        showToast(body);
                    }
                });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_with_cert:
                post(HttpUtils.getClientWithCert());
                break;
            case R.id.btn_without_cert:
                post(HttpUtils.getClientWithoutCert());
                break;
        }
    }
}
