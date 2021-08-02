package com.hhuc.smartdoorterminal;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.RuntimeABI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hhuc.smartdoorterminal.modules.hardware.GPIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class NavigationActivity extends AppCompatActivity {

    public final static String TAG = "NavigationActivity";
    private long mExitTime;//声明一个long类型变量：用于存放上一点击“返回键”的时刻
    public static final String APP_ID = "请在此次填写你所申请到的APP_ID"; //虹软人脸识别离线SDK
    public static final String SDK_KEY = "请在此次填写你所申请到的SDK_KEY"; //虹软人脸识别离线SDK
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so"
    };
    boolean libraryExists = true;
    String[] PERMISSIONS_STORAGE = {
            "android.permission.CAMERA",
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_recognize, R.id.navigation_webrtc)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        hideSystemBar();
        libraryExists = checkSoFile(LIBRARIES);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.i(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
        if (!libraryExists) {
            Toast.makeText(this, "library_not_found", Toast.LENGTH_LONG).show();
        } else {
            VersionInfo versionInfo = new VersionInfo();
            int code = FaceEngine.getVersion(versionInfo);
            Log.i(TAG, "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
        }
        init();
    }

    private void init() {
        try {
            if (!checkOrRequestPermission()) {
                ActivityCompat.requestPermissions(NavigationActivity.this, PERMISSIONS_STORAGE, ACTION_REQUEST_PERMISSIONS);
            }
            GPIO.gpio_crtl("26", "out", 1);
            activeEngine();
        } catch (Exception er) {
            Log.e(TAG, "init" + er.getMessage());
        }
    }

    private void hideSystemBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            int uiOptions =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /* 校验并请求权限 */
    public boolean checkOrRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(NavigationActivity.this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(NavigationActivity.this, "android.permission.READ_PHONE_STATE") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(NavigationActivity.this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, ACTION_REQUEST_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

    /**
     * 激活引擎
     */
    public void activeEngine() {
        if (!libraryExists) {
            Toast.makeText(NavigationActivity.this, "library_not_found", Toast.LENGTH_LONG).show();
            return;
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(NavigationActivity.this, APP_ID, SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            Toast.makeText(NavigationActivity.this, "active_success", Toast.LENGTH_LONG).show();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Toast.makeText(NavigationActivity.this, "already_activated", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(NavigationActivity.this, "active_failed", Toast.LENGTH_LONG).show();
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(NavigationActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(NavigationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

        } catch (Exception er) {
            Log.e(TAG, "onActivityResult: " + er.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            //大于2000ms则认为是误操作，使用Toast进行提示
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            //并记录下本次点击“返回键”的时刻，以便下次进行判断
            mExitTime = System.currentTimeMillis();
        } else {
            //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
            System.exit(0);
        }
    }
}
