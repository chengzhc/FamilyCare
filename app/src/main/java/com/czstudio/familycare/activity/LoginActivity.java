package com.czstudio.familycare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.czstudio.czlibrary.CzLibrary;
import com.czstudio.czlibrary.CzSys_HTTP;
import com.czstudio.familycare.Constant;
import com.czstudio.familycare.R;
import com.czstudio.familycare.WelcomeActivity;
import com.czstudio.familycare.model.ModelUser;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    String TAG=getClass().getSimpleName();
    LoginActivity instance;
    TextView tv_goto_register;
    EditText et_username,et_password;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constant.debugInfo(TAG,"onCreate");
        setContentView(R.layout.activity_login);

        initData();
        initView();

//        et_username.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CzLibrary.alert(instance,"请先登录");
//            }
//        },100);
    }

    void initData(){
        instance=this;
    }

    void initView(){
        et_username=findViewById(R.id.et_username);
        et_password=findViewById(R.id.et_password);
        btn_login=findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        tv_goto_register=findViewById(R.id.tv_goto_register);
        tv_goto_register.setOnClickListener(this);
    }


    //======= Proccess ===========
    void login(){
        String url= Constant.DOMAIN_API+"/monitor/login?"
                +"mobile="+et_username.getText().toString()
                +"&password="+et_password.getText().toString();
        CzSys_HTTP.requestPostCz(instance, url, new HashMap<String, String>(),
                new CzSys_HTTP.HttpListener() {
                    @Override
                    public void onHttpSuccess(String data) {

                    }

                    @Override
                    public void onFeedBackSuccess(JSONObject feedBackData) {
                        try{
                            CzLibrary.alert(instance,"登陆成功");
                            ModelUser.getInstance(instance).saveUserInfo(feedBackData.getJSONObject("data"));
                            et_password.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(instance, WelcomeActivity.class));
                                    finish();
                                }
                            },1000);

                        }catch (Exception e){
                            CzLibrary.alert(instance,"解析令牌异常，请稍后再试:"+e);
                        }
                    }
                });
    }


    //======= Implements==============
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_login:
                login();
                break;
            case R.id.tv_goto_register:
                startActivity(new Intent(instance,RegisterActivity.class));
                finish();
                break;
        }
    }
}
