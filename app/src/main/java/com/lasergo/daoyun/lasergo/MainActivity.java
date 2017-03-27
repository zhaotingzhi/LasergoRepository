package com.lasergo.daoyun.lasergo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.zjl.autolayout.AutoUtils;

/**
 * Created by zhaotingzhi on 2017/3/27.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        AutoUtils.auto(this);//适配实际屏幕

    }
}
