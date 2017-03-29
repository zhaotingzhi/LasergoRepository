package com.lasergo.daoyun.lasergo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.lasergo.daoyun.conts.Conts;
import com.zjl.autolayout.AutoUtils;

public class LoginActivity extends Activity {

    private Button bt_login;
    private EditText et_username, et_password;
    private SharedPreferences sharedPreferences;
    private CheckBox rem_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoUtils.setSize(this, false, 720, 1280);//没有状态栏,设计尺寸的宽高
        setContentView(R.layout.activity_login);
        AutoUtils.auto(this);//适配实际屏幕

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        sharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        et_username.setText(sharedPreferences.getString("username", ""));
        et_password.setText(sharedPreferences.getString("password", ""));
        rem_number = (CheckBox) this.findViewById(R.id.rem_number);
        bt_login = (Button)this.findViewById(R.id.login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//			finish();
                session_build();
                if (Conts.session != null && Conts.session.isConnected()) {
                    startActivity(new Intent(LoginActivity.this,
                            MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.please_check_wifi,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void session_build() {
        try {
            JSch jsch = new JSch();
            String user = et_username.getText().toString();
            String passwd = et_password.getText().toString();
            if (rem_number.isChecked()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", user);
                editor.putString("password", passwd);
                editor.commit();
            }
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            DhcpInfo di = wm.getDhcpInfo();
            long getewayIpL = di.gateway;
            String getwayIpS = long2ip(getewayIpL);
            String host = getwayIpS;
            Conts.session = jsch.getSession(user, host, 22);
            Conts.session.setPassword(passwd);
            Conts.session.setConfig("StrictHostKeyChecking", "no");
            Conts.session.connect(1000);
        } catch (JSchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
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
