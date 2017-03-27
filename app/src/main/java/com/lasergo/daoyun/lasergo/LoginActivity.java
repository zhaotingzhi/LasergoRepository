package com.lasergo.daoyun.lasergo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.zjl.autolayout.AutoUtils;

public class LoginActivity extends Activity {

    Button bt_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoUtils.setSize(this, false, 720, 1280);//没有状态栏,设计尺寸的宽高
        setContentView(R.layout.activity_login);
        AutoUtils.auto(this);//适配实际屏幕
        bt_login = (Button)this.findViewById(R.id.login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,MainActivity.class
                ));
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
