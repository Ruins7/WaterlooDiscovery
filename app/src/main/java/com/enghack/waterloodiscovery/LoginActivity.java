package com.enghack.waterloodiscovery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enghack.waterloodiscovery.Entity.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by ruins7 on 2017-02-04.
 */

public class LoginActivity extends AppCompatActivity {


    private static EditText usernametext;
    private static EditText passwordtext;
    private static EditText appname;
    private static Intent intent_ivtime;

    private static User loginuser;

    private static HttpClient client;
    private static HttpPost request;
    private static HttpEntity entity;
    private static HttpResponse response;

    private static String urlbase;
    private static String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        urlbase = this.getResources().getString(R.string.url);
        url = urlbase + "login";

        UserLogin userLogin = new UserLogin();

        Button login = (Button) findViewById(R.id.login);

        usernametext = (EditText) findViewById(R.id.username);

        passwordtext = (EditText) findViewById(R.id.password);

        usernametext.setFocusable(true);
        usernametext.setFocusableInTouchMode(true);

        passwordtext.setFocusable(true);
        passwordtext.setFocusableInTouchMode(true);

        login.setOnClickListener(userLogin);

    }

    //login 监听事件
    class UserLogin implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (usernametext.getText().toString().trim().equals("") || passwordtext.getText().toString().trim().equals("")) {
                Toast.makeText(LoginActivity.this, "username and password cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                loginuser = new User();
                loginuser.setUsername(usernametext.getText().toString());
                loginuser.setPassword(passwordtext.getText().toString());

                //启动通信线程
                LoginConn loginConn = new LoginConn();
                loginConn.start();
            }
        }
    }

    //login 与 server 通信 继承Thread类
    class LoginConn extends Thread {

        private String statusMessage = "";

        @Override
        public void run() {
            // 初始化消息循环队列，需要在Handler创建之前
            Looper.prepare();
            client = new DefaultHttpClient();
            HttpPost request;
            try {
                request = new HttpPost(new URI(url));
                //json 封装
                JSONObject json = new JSONObject();
                json.put("email", loginuser.getUsername());
                json.put("password", loginuser.getPassword());

                StringEntity sentity = new StringEntity(json.toString(), "utf-8");
                request.addHeader("Content-Type", "application/json");
                request.setEntity(sentity);

                try {
                    response = client.execute(request);
                } catch (Exception e) {
                    statusMessage = "something wrong with network";
                }

                int statecode = response.getStatusLine().getStatusCode();


                    Log.v("connect status", String.valueOf(statecode));
                    if (statecode == 200) { //请求成功
                        entity = response.getEntity();
                        if (entity != null) {
                            String out = EntityUtils.toString(entity, "UTF-8");
                            Log.i("result from server", out);
                            if (out.trim().equals("{}")) {
                                statusMessage = "Fail to login, wrong email or password";
                            } else {
                                //登录成功提示
                                statusMessage = "Login successfully";

                                //赋值(对象传递)
                                intent_ivtime = new Intent();
                                intent_ivtime.putExtra("loginuser", (Serializable) loginuser);
                                //跳转，将数据发送到下一个页面
                                intent_ivtime.setClass(LoginActivity.this, MapsActivity.class);
                                LoginActivity.this.startActivity(intent_ivtime);
                            }
                        } else if (entity == null) {
                            //未请求到返回数据
                            statusMessage = "There is no return information from server";
                        }
                    }

            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                //e.printStackTrace();
            } catch (URISyntaxException e) {
                //e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, statusMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
