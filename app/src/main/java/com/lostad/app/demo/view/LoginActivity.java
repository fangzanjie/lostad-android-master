package com.lostad.app.demo.view;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lostad.app.base.util.EffectUtil;
import com.lostad.app.base.util.PrefManager;
import com.lostad.app.base.view.BaseActivity;
import com.lostad.app.demo.R;
import com.lostad.app.demo.entity.LoginConfig;
import com.lostad.app.demo.task.LoginTask;
import com.lostad.app.demo.view.mainFragment.SettingsFragment;
import com.lostad.applib.core.MyCallback;
import com.lostad.applib.entity.ILoginConfig;
import com.lostad.applib.util.Validator;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;


public class LoginActivity extends BaseActivity {                 //登录界面活动

    public int pwdresetFlag=0;
    private EditText mAccount;                        //用户名编辑
    private EditText mPwd;                            //密码编辑
    private Button mRegisterButton;                  //注册按钮
    private Button mLoginButton;                      //登录按钮
    private Button mCancleButton;                     //注销按钮
    private CheckBox mRememberCheck;                 //记住密码选择

    private SharedPreferences login_sp;
    private String userNameValue,passwordValue;

    private View loginView;                           //登录
    private View loginSuccessView;
    private TextView loginSuccessShow;
    private TextView mChangepwdText;
    private UserDataManager mUserDataManager;         //用户数据管理类


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle("用户登录注册");
        //通过id找到相应的控件
        mAccount = (EditText) findViewById(R.id.login_edit_account);
        mPwd = (EditText) findViewById(R.id.login_edit_pwd);
        mRegisterButton = (Button) findViewById(R.id.login_btn_register);
        mLoginButton = (Button) findViewById(R.id.login_btn_login);
        mCancleButton = (Button) findViewById(R.id.login_btn_cancle);
        loginView=findViewById(R.id.login_view);
        loginSuccessView=findViewById(R.id.login_success_view);
        loginSuccessShow=(TextView) findViewById(R.id.login_success_show);

        mChangepwdText = (TextView) findViewById(R.id.login_text_change_pwd);

        mRememberCheck = (CheckBox) findViewById(R.id.Login_Remember);

        login_sp = getSharedPreferences("userInfo", 0);
        String name=login_sp.getString("USER_NAME", "");
        String pwd =login_sp.getString("PASSWORD", "");
        boolean choseRemember =login_sp.getBoolean("mRememberCheck", false);
        boolean choseAutoLogin =login_sp.getBoolean("mAutologinCheck", false);
        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if(choseRemember){
            mAccount.setText(name);
            mPwd.setText(pwd);
            mRememberCheck.setChecked(true);
        }

        mRegisterButton.setOnClickListener(mListener);                      //采用OnClickListener方法设置不同按钮按下之后的监听事件
        mLoginButton.setOnClickListener(mListener);
        mCancleButton.setOnClickListener(mListener);
        mChangepwdText.setOnClickListener(mListener);

        ImageView image = (ImageView) findViewById(R.id.logo);             //使用ImageView显示logo
        image.setImageResource(R.drawable.logo);

        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
    }
    View.OnClickListener mListener = new View.OnClickListener() {                  //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_btn_register:                            //登录界面的注册按钮
                    Intent intent_Login_to_Register = new Intent(LoginActivity.this,Register0Activity.class) ;    //切换Login Activity至Register Activity
                    startActivity(intent_Login_to_Register);
                    finish();
                    break;
                case R.id.login_btn_login:                              //登录界面的登录按钮
                    login();
                    break;
                case R.id.login_btn_cancle:                             //登录界面的注销按钮
                    cancel();
                    break;
                case R.id.login_text_change_pwd:                             //登录界面的修改密码
                    Intent intent_Login_to_reset = new Intent(LoginActivity.this,ResetpwdActivity.class) ;    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_reset);
                    finish();
                    break;
            }
        }
    };

    public void login() {                                              //登录按钮监听事件
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
            String userPwd = mPwd.getText().toString().trim();
            SharedPreferences.Editor editor =login_sp.edit();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd);
            if(result==1){                                             //返回1说明用户名和密码均正确
                //保存用户名和密码
                editor.putString("USER_NAME", userName);
                editor.putString("PASSWORD", userPwd);

                //是否记住密码
                if(mRememberCheck.isChecked()){
                    editor.putBoolean("mRememberCheck", true);
                }else{
                    editor.putBoolean("mRememberCheck", false);
                }
                editor.commit();

                Intent intent = new Intent(LoginActivity.this,SettingsFragment.class) ;    //切换Login Activity至User Activity
                startActivity(intent);
                finish();
                Toast.makeText(this, getString(R.string.login_success),Toast.LENGTH_SHORT).show();//登录成功提示
            }else if(result==0){
                Toast.makeText(this, getString(R.string.login_fail),Toast.LENGTH_SHORT).show();  //登录失败提示
            }
        }
    }
    public void cancel() {           //注销
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
            String userPwd = mPwd.getText().toString().trim();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd);
            if(result==1){                                             //返回1说明用户名和密码均正确
//                Intent intent = new Intent(Login.this,User.class) ;    //切换Login Activity至User Activity
//                startActivity(intent);
                Toast.makeText(this, getString(R.string.cancel_success),Toast.LENGTH_SHORT).show();//登录成功提示
                mPwd.setText("");
                mAccount.setText("");
                mUserDataManager.deleteUserDatabyname(userName);
            }else if(result==0){
                Toast.makeText(this, getString(R.string.cancel_fail),Toast.LENGTH_SHORT).show();  //登录失败提示
            }
        }

    }

    public boolean isUserNameAndPwdValid() {
        if (mAccount.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.account_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPwd.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.pwd_empty),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mUserDataManager != null) {
            mUserDataManager.closeDataBase();
            mUserDataManager = null;
        }
        super.onPause();
    }

}



/*
public class LoginActivity extends BaseActivity {
    @ViewInject(R.id.et_phone)
    private TextView et_phone;
    @ViewInject(R.id.et_password)
    private TextView et_password;

    @ViewInject(R.id.btn_login)
    private Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        x.view().inject(this);
        //社会化分享
        // UmengPlatUtil.getInstance(this);
        ILoginConfig login = getLoginConfig();
        if (login != null) {
            et_phone.setText(login.getPhone());
            et_password.setText(login.getPassword());
        }
        //使用背景图片浸染
        setStatusBarStyle(R.color.transparent);
    }

    @Event(R.id.tv_reg)
    private void onClickToReg(View v) {
        Intent i = new Intent(ctx, Register0Activity.class);
        startActivity(i);
    }

    @Event(R.id.tv_find_pwd)
    private void onClickToFindPwd(View v) {
        Intent i = new Intent(ctx, FindPwd0Activity.class);
        startActivity(i);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public void onBtnClick(View v) {
//        if(!validateInternet()){
//            return;
//        }
//        int id = v.getId();
//        switch (id) {
//            case R.id.iv_qq:
//                UmengPlatUtil.getInstance(LoginActivity.this).doOauthVerify(LoginActivity.this, SHARE_MEDIA.QQ,new UmCallback() {
//                    @Override
//                    public void onCallback(String uid, Map info) {
//                        loginQQ(uid, info,"1");
//                    }
//                });
//                break;
//
//            case R.id.iv_weixin:
//                UmengPlatUtil.getInstance(LoginActivity.this).doOauthVerify(LoginActivity.this,SHARE_MEDIA.WEIXIN,new UmCallback() {
//                    @Override
//                    public void onCallback(String uid, Map info) {
//                        loginWixin(uid, info,"2");
//                    }
//                });
//                break;
//            case R.id.iv_weibo:
//                UmengPlatUtil.getInstance(LoginActivity.this).doOauthVerify(LoginActivity.this,SHARE_MEDIA.SINA,new UmCallback() {
//                    @Override
//                    public void onCallback(String uid, Map info) {
//                        loginWeibo(uid, info,"3");
//                    }
//                });
//                break;
//            case R.id.tv_protocol:
//                Intent i = new Intent(LoginActivity.this,ProtocalActivity.class);
//                String url = IConst.URL+"/xieyi.html";
//                i.putExtra("title", "用户使用协议");
//                i.putExtra("url",url);
//                startActivity(i);
//                break;
//            case R.id.btn_login:
//                loginByPhone();
//                break;
//            case R.id.iv_protocol:
//                DialogUtil.showAlertOkCancel(this, "你确定不同意此用户协议吗？取消选中状态后，您将不能使用跑伴儿服务！", new MyCallback<Boolean>() {
//                    @Override
//                    public void onCallback(Boolean yes) {
//                        if (yes) {
//                            finish();
//                        }
//                    }
//                });
//                break;
//
//        }
//    }
//    /**
//     * {sex=1, nickname=a ✨在水一方发盐人✨, unionid=olw_2sqw686P-VO6dmu4Ku5zM41E, province=, openid=oKyqRs__4PNvT2LvKrMByTmi6bo4, language=zh_CN,
//     * headimgurl=http://wx.qlogo.cn/mmopen/WD4FduqfeKJlxGhs5fdn0UCltPaOdNib2aCbObibMq4yKMhVU8vAX3XM3wicaq1ksYYpxiaKoESox36tkIUDPbiaSug/0,
//     * country=, city=}
//     * @param uid
//     * @param info
//     * @param type
//     */
//    private void loginWixin(String uid, Map info,String type) {
//        if(!validateInternet()){
//            return;
//        }
//        String nickname = info.get("nickname").toString();
//        String head = info.get("headimgurl").toString();
//        String sex = info.get("sex")+"";// null+""
//
//        if(mLoginConfig==null){
//            mLoginConfig = new LoginConfig();
//        }
//        mLoginConfig.setLoginType(type);
//        mLoginConfig.setUid3(uid);//第三方id
//        mLoginConfig.setName(nickname);
//        mLoginConfig.setHeadUrl(head);
//        mLoginConfig.setPwd("");
//        mLoginConfig.setSex("1".equals(sex)?"1":"2");
//        LoginTask loginTask = new LoginTask(LoginActivity.this,mLoginConfig);
//        loginTask.execute();
//    }
//    private void loginQQ(String uid, Map info,String type) {
//        if(!validateInternet()){
//            return;
//        }
//        String nickname = info.get("screen_name").toString();
//        String head = info.get("profile_image_url").toString();
//        String gender = info.get("gender").toString();
//        if(mLoginConfig==null){
//            mLoginConfig = new LoginConfig();
//        }
//        mLoginConfig.setLoginType(type+"");
//        mLoginConfig.setUid3(uid);//第三方id
//        mLoginConfig.setName(nickname);
//        mLoginConfig.setHeadUrl(head);
//        mLoginConfig.setPwd("");
//        mLoginConfig.setSex("女".equals(gender)?"1":"2");
//        LoginTask loginTask = new LoginTask(LoginActivity.this,mLoginConfig);
//        loginTask.execute();
//    }
//
//    /**
//     *  following.screenName = String.valueOf(user.get("name"));
//     following.description = String.valueOf(user.get("description"));
//     following.icon = String.valueOf(user.get("profile_image_url"));
//     */
//    private void loginWeibo(String uid, Map info,String type) {
//        if(!validateInternet()){
//            return;
//        }
//        String nickname = info.get("name").toString();
//        String head = info.get("profile_image_url").toString();
//        String gender = info.get("gender").toString();
//        if(mLoginConfig==null){
//            mLoginConfig = new LoginConfig();
//        }
//        mLoginConfig.setLoginType(type);
//        mLoginConfig.setUid3(uid);//第三方id
//        mLoginConfig.setName(nickname);
//        mLoginConfig.setHeadUrl(head);
//        mLoginConfig.setPwd("");
//        mLoginConfig.setSex("女".equals(gender)?"1":"2");
//        LoginTask loginTask = new LoginTask(LoginActivity.this,mLoginConfig);
//        loginTask.execute();
//    }

    /*
    @Event(R.id.btn_login)
    private void onClickLoginByPhone(View v) {

        String username = et_phone.getText().toString();
        String pwd = et_password.getText().toString();
        if (Validator.isBlank(username)) {
            et_phone.requestFocus();
            et_phone.setError(Html.fromHtml("<font color=#FFFFFF>手机号不能为空</font>"));
            EffectUtil.showShake(this, et_phone);
            return;
        }

        if (!Validator.isMobile(username)) {
            et_phone.setError(Html.fromHtml("<font color=#FFFFFF>手机号不正确</font>"));
            et_phone.requestFocus();
            et_phone.setText("");
            return;
        }
        LoginConfig mLoginConfig = new LoginConfig();
        mLoginConfig.setPhone(username);
        if (Validator.isBlank(pwd)) {
            et_password.setError(Html.fromHtml("<font color=#FFFFFF>请输入验密码</font>"));
            et_password.requestFocus();
            return;
        }

        mLoginConfig.setPassword(pwd);

        PrefManager.saveString(this, "phone", et_phone.getText().toString());
        LoginTask loginTask = new LoginTask(LoginActivity.this, mLoginConfig, new MyCallback<Boolean>() {
            @Override
            public void onCallback(Boolean success) {
                if (success) {
                    finish();
                }
            }
        });
        loginTask.execute();
    }

}
*/

