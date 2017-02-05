package com.enghack.waterloodiscovery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enghack.waterloodiscovery.Entity.User;

import java.util.ArrayList;

/**
 * Created by ruins7 on 2017-02-04.
 */

public class LoginActivity extends AppCompatActivity {

    private static String username;
    private static String password;
    private static EditText usernametext;
    private static EditText passwordtext;
    private static EditText appname;

    //private static QuizDB quizDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        UserLogin userLogin = new UserLogin();

        appname = (EditText) findViewById(R.id.appname);
        Button login = (Button) findViewById(R.id.login);

        usernametext = (EditText) findViewById(R.id.username);

        passwordtext = (EditText) findViewById(R.id.password);

        appname.setEnabled(false);

        usernametext.setFocusable(true);
        usernametext.setFocusableInTouchMode(true);

        passwordtext.setFocusable(true);
        passwordtext.setFocusableInTouchMode(true);

        login.setOnClickListener(userLogin);

    }

    class UserLogin implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            username = usernametext.getText().toString();
            password = passwordtext.getText().toString();
            if(username.trim().equals("") || password.trim().equals("")){
                Toast.makeText(LoginActivity.this, "Username or Password can not be empty", Toast.LENGTH_SHORT).show();
            }else{
                User loginUser = new User();
                loginUser.setUsername(username);
                loginUser.setPassword(password);


                if (true) {
                    Toast.makeText(LoginActivity.this, "success", Toast.LENGTH_LONG).show();
                    Intent intent_ivtime = new Intent();
                    intent_ivtime.setClass(LoginActivity.this, MapsActivity.class);
                    LoginActivity.this.startActivity(intent_ivtime);

                } else {
                    //登录失败，重新登录，泡提示
                    Toast.makeText(LoginActivity.this, "wrong username or password", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
