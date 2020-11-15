package com.czstudio.familycare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG="RegisterActivity";
    static int COUNT_DOWN_SECOND=30;
    RegisterActivity instance;
    TextView tv_goto_login;
    Button btn_verify_code,btn_register;
    EditText et_username,et_verify_code,et_password,et_repassword;

    String verify_token;
    boolean isCountDown=false;
    int countDown=COUNT_DOWN_SECOND;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initData();
        initView();
    }

    void initData(){
        instance=this;
    }

    void initView(){
        tv_goto_login=findViewById(R.id.tv_goto_login);
        tv_goto_login.setOnClickListener(this);
        et_username=findViewById(R.id.et_username);
        et_verify_code=findViewById(R.id.et_verify_code);
        et_password=findViewById(R.id.et_password);
        et_repassword=findViewById(R.id.et_repassword);
        btn_register=findViewById(R.id.btn_register);
        btn_register.setOnClickListener(this);
        btn_verify_code=findViewById(R.id.btn_verify_code);
        btn_verify_code.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_verify_code:
                getVerifyCode();
                break;
            case R.id.btn_register:
                register();
                break;
            case R.id.tv_goto_login:
                startActivity(new Intent(this,LoginActivity.class));
                finish();
                break;
        }
    }

    void resetBtnVerifyCode(){
        btn_verify_code.setText("获取");
        countDown=COUNT_DOWN_SECOND;
        isCountDown=false;
    }

    void getVerifyCode(){
        if(isCountDown){
            CzLibrary.alert(this,"验证码发送中，请勿反复点击");
            return;
        }
        isCountDown=true;
        new Thread(){
            public void run(){
               while (isCountDown&& countDown>0){
                    try {
                        Thread.sleep(1000);
                        instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_verify_code.setText(""+countDown);
                                countDown--;
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "getVerifyCode CountDown Exp:" + e);
                    }
               }
                instance.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetBtnVerifyCode();
                    }
                });

            }
        }.start();

        String url= Constant.DOMAIN_API+"/monitor/get_verify_code?"
                +"mobile="+et_username.getText().toString();


        CzSys_HTTP.requestPostCz(instance, url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {
                resetBtnVerifyCode();
            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    CzLibrary.alert(instance,"验证码已发送");
                    verify_token=feedBackData.getString("data");
                }catch (Exception e){
                    CzLibrary.alert(instance,"解析令牌异常，请稍后再试:"+e);
                    Log.e(TAG, "getVerifyCode Exp:" + e);
                }
            }
        });
    }

    void register(){
        String url=Constant.DOMAIN_API+"/monitor/register?"
                +"mobile="+et_username.getText().toString()
                +"&code="+et_verify_code.getText().toString()
                +"&password="+et_password.getText().toString()
                +"&repassword="+et_repassword.getText().toString()
                +"&verify_token="+verify_token;
        CzSys_HTTP.requestPostCz(instance, url, new HashMap<String, String>(), new CzSys_HTTP.HttpListener() {
            @Override
            public void onHttpSuccess(String data) {

            }

            @Override
            public void onFeedBackSuccess(JSONObject feedBackData) {
                try{
                    CzLibrary.alert(instance,"注册成功");
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
}
