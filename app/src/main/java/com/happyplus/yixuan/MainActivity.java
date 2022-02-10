package com.happyplus.yixuan;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private String host = "localhost";
    private int port = 5578;
    private TextView text;
    private String res;
    private AdbConnection connection;
    private AdbCrypto crypto;

    private void ExecCommand(String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(command);
                        }
                    });
                    res = command + " (结果以实际为准)";
                    AdbStream stream = connection.open("shell:" + command);
                    while (!stream.isClosed())
                        try {
                            // Print each thing we read from the shell stream
                            res = new String(stream.read(), "US-ASCII");
                            Log.e("HAPPYPLUS", res);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                } catch (Exception e) {
                    res = e.toString();
                    Log.e("HAPPYPULS", "SHELL FAILED: " + e.toString());
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(res);
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置屏幕为横屏, 设置后会锁定方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        text = findViewById(R.id.result);

        // adb加密
        try {
            crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public String encodeToString(byte[] data) {
                    return Base64.encodeToString(data, 2);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 打开嘟嘟
        Button startDudu = findViewById(R.id.start_dudu);
        startDudu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "打开嘟嘟车机");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "打开嘟嘟车机", Toast.LENGTH_LONG).show();
                    }
                });
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.wow.carlauncher");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // 关闭高德车机版
        Button killMap = findViewById(R.id.stop_maps);
        killMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "关闭高德地图");
                String packageName = "com.autonavi.amapauto";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "关闭高德地图", Toast.LENGTH_LONG).show();
                    }
                });
                ExecCommand("am force-stop " + packageName);
                ActivityManager activityManager = (ActivityManager) getSystemService(getApplicationContext().ACTIVITY_SERVICE);
                activityManager.killBackgroundProcesses(packageName);
            }
        });

        // 解除安装app限制
        Button allowInstallApp = findViewById(R.id.allow_install_app);
        allowInstallApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "解除安装app限制");
                ExecCommand("settings put secure install_non_market_apps 1");
            }
        });

        // 隐藏左边蓝条
        Button hideBlueBar = findViewById(R.id.hide_blue_bar);
        hideBlueBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "隐藏左边蓝条");
                ExecCommand("settings put global policy_control immersive.full=*");
            }
        });

        // 恢复左边蓝条
        Button showBlueBar = findViewById(R.id.show_blue_bar);
        showBlueBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "恢复左边蓝条");
                ExecCommand("settings put global policy_control null");
            }
        });

        // 恢复左边蓝条
        Button activeShizuku = findViewById(R.id.active_shizuku);
        activeShizuku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "激活shizuku");
                ExecCommand("sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh");
            }
        });

        // 沉浸状态栏
        Button hideStatusBar = findViewById(R.id.hide_status_bar);
        hideStatusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "沉浸状态栏");
                ExecCommand("settings put global policy_control immersive.status=*");
            }
        });

        // 连接adb
        Button adbConnect = findViewById(R.id.adb_connect);
        adbConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "连接adb");
                adbConnect();
            }
        });

        // 断开adb
        Button adbDisconnect = findViewById(R.id.adb_disconnect);
        adbDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "断开adb");
                disconnectAdb();
            }
        });

        // 重启
        Button reboot = findViewById(R.id.reboot);
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("HAPPYPLUS", "重启");
                ExecCommand("reboot");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置屏幕为横屏, 设置后会锁定方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void adbConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port); // put phone IP address here
                    connection = AdbConnection.create(socket, crypto);
                    connection.connect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "adb连接成功", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException | InterruptedException e) {
                    res = e.toString();
                    if (res.contains("Connection failed")) res = host + ":" + port + "连接失败";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, res, Toast.LENGTH_LONG).show();
                            text.setText(res);
                        }
                    });
                }
            }
        }).start();
    }

    private void disconnectAdb() {
        try {
            connection.close();
            Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}