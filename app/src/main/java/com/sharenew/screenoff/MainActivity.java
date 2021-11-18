package com.sharenew.screenoff;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private Button buttonOff;
    private Button buttonOn;

    private Button m_btnTimeout;
    private EditText m_etTimeout;
    private Handler mHandler;
    private DevicePolicyManager policyManager;
    private ComponentName adminReceiver;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    turnOnScreen();
                    break;
                case 0x03:
                    Log.d("qytech", "off!");

                    policyManager.lockNow();
                    break;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonOff = (Button) findViewById(R.id.button_off);
        m_btnTimeout = (Button) findViewById(R.id.btn_timeout);
        m_etTimeout = (EditText) findViewById(R.id.et_timeout);
        mHandler = new Handler(mCallback);
        adminReceiver = new ComponentName(MainActivity.this, ScreenOffAdminReceiver.class);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean admin = policyManager.isAdminActive(adminReceiver);
                if (admin) {
                    mHandler.sendEmptyMessage(0x03);
                } else {
                    Toast.makeText(MainActivity.this, "没有设备管理权限", Toast.LENGTH_LONG).show();
                }
            }
        });
        buttonOn = (Button) findViewById(R.id.button_on);
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnScreen();
            }
        });
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        policyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二

        startActivityForResult(intent, 0);

        m_btnTimeout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = Long.parseLong(m_etTimeout.getText().toString());
                mHandler.sendEmptyMessageDelayed(0x03, time * 1000);
                Toast.makeText(MainActivity.this, String.format("lock %s after s ", time), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void turnOnScreen() {
        // turn on screen
        Log.d("qytech", "ON!");
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, getClass().getSimpleName());
        mWakeLock.acquire(1000L);
        mWakeLock.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isOpen();
    }

    /**
     * 检测用户是否开启了超级管理员
     */
    private void isOpen() {
        if (policyManager.isAdminActive(adminReceiver)) {/*判断超级管理员是否激活*/
            Toast.makeText(MainActivity.this, "设备已被激活", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "设备没有被激活", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

}
