package com.uhfdemo2longer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MyData.DatabaseHelper;
import com.MyData.MyRunner;
import com.MyData.Tools;
import com.handheld.UHFLonger.UHFLongerManager;
import com.handheld.UHFLongerDemo.Util;
import com.uhfdemo2longer.R.array;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class MainActivity extends Activity implements View.OnClickListener {

    Button buttonconnect;
    Button buttonscan;
    Button buttonsearch;
    Button buttonclear;
    Button buttoninit;
    Button buttonfile;
    ImageView imageview_photo;
    TextView textview_epc;
    Toast toast;
    String root = "";
    UHFLongerManager manager = null;
    InventoryThread thread = null;
    KeyReceiver keyReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        Util.initSoundPool(this);
        thread = new InventoryThread();
        thread.start();

        root = Environment.getExternalStorageDirectory().getPath();

        keyReceiver = new KeyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, intentFilter);
    }

    DatabaseHelper dbHelper;

    @Override
    protected void onStart() {
        dbHelper = new DatabaseHelper(this, "shit", null, 1);
        connect();
        scan();
        super.onStart();
    }

    @Override
    protected void onPause() {

        startFlag = false;
        super.onPause();
    }

    @Override
    protected void onRestart() {
        startFlag = true;
        super.onRestart();
    }



    private void initView() {
        buttonconnect = (Button) findViewById(R.id.buttonconnect);
        buttonscan = (Button) findViewById(R.id.buttonscan);
        buttonsearch = (Button) findViewById(R.id.buttonsearch);
        buttonclear = (Button) findViewById(R.id.buttonclear);
        buttoninit = (Button) findViewById(R.id.buttoninit);
        buttonfile = (Button) findViewById(R.id.buttonfile);

        imageview_photo = (ImageView) findViewById(R.id.imageviewphoto);
        textview_epc = (TextView) findViewById(R.id.textview_epc);
        buttonconnect.setOnClickListener(this);
        buttonscan.setOnClickListener(this);
        buttonsearch.setOnClickListener(this);
        buttonclear.setOnClickListener(this);
        buttoninit.setOnClickListener(this);
        buttonfile.setOnClickListener(this);
    }

    private class KeyReceiver extends BroadcastReceiver {
        private String TAG = "KeyReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            boolean keyDown = intent.getBooleanExtra("keydown", false);
//			Log.e("down", ""+keyDown);
            if (keyDown) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                        showtoast("f1");
                        break;
                    case KeyEvent.KEYCODE_F2:
                        showtoast("f2");
                        break;
                    case KeyEvent.KEYCODE_F3:
                        //手持按键
                        showtoast("f3");
                        break;
                    case KeyEvent.KEYCODE_F5:
                        showtoast("f5");
                        break;
                    case KeyEvent.KEYCODE_F4:
                        showtoast("f4");
                        break;
                }
            }
        }
    }

    private void showtoast(String info) {

        toast = Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.buttonconnect:
                //连接成功
                connect();
                break;
            case R.id.buttonscan:
                //开始扫描
                scan();
                break;
            case R.id.buttonsearch:
                String ok = "1E573734BE50052BE7981E8D";
                MyRunner runner = dbHelper.search("1");
                if (runner != null) {
                    String path = runner.getPhoto();
                    path = root + path;
                    File file = new File(path);
                    if (file.exists()) {
                        Bitmap bitmap = getLoacalBitmap(path);
                        imageview_photo.setImageBitmap(bitmap);
                    } else {
                        showtoast("路径不存在");
                    }
                }
                break;
            case R.id.buttonclear:
                int count = dbHelper.count();
                showtoast(Integer.toString(count));
                dbHelper.clear();
                 count = dbHelper.count();
                showtoast(Integer.toString(count));

                File del = new File(root + "/test");
                deleteDir(del);
//                if (del.delete()) {
//                    showtoast("删除成功");
//                } else {
//                    showtoast("删除失败");
//                }
                break;
            case R.id.buttoninit:
//                dbHelper.insert();

                String source = root + "/temp/ysj.jpg";
//                String density = root + "/test/123.jpg";
//                File sourceFile = new File(source);
//                File densityFile = new File(density);
//                Tools.copyFileUsingStream(sourceFile, densityFile);
//                Tools.copyFile(source, density);
//
//                if (densityFile.exists()) {
//                    showtoast("复制成功");
//                } else {
//                    showtoast("复制失败");
//                }

                for (int i = 0; i <= 10; i++) {
                    String densityFile = root + "/test/" + i + ".jpg";
                    MyRunner runner1 = new MyRunner();
                    runner1.setName(Integer.toString(i));
                    runner1.setCode(Integer.toString(i));
                    runner1.setPhoto("/test/" + i + ".jpg ");
                    dbHelper.insert(runner1);
                    Tools.copyFile(source, densityFile);
                    Log.i("insert", runner1.getPhoto());
                }
                showtoast("初始化结束");
                break;

            case R.id.buttonfile:
                File test = new File(root + "/test");
                if (test.isDirectory()) {
                    File[] array = test.listFiles();
                    showtoast(Integer.toString(array.length));
                    for (File temp : array
                            ) {
                        Log.e("list file", temp.getPath());
//                        System.out.print(temp.getPath());
                    }
                } else {
                    showtoast("不是目录");
                }

                break;
        }
    }

    boolean startFlag = false;

    private void scan() {
        if (manager == null) return;
        if (!startFlag) {
            startFlag = true;
            buttonscan.setText(getString(R.string.stop_inventory));
        } else {
            startFlag = false;
            buttonscan.setText(getString(R.string.inventory));
        }
    }


    private void connect() {
        try {
            manager = UHFLongerManager.getInstance();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    // TODO Auto-generated method stub
                    SharedPreferences shared = getSharedPreferences("settings", 0);
                    final int value = shared.getInt("power", 30);
                    if (manager.setOutPower((short) value)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String temp = getString(R.string._power_now) + value;
//                                showtoast(temp);
                            }
                        });
                    }
                    final int reg = shared.getInt("freband", 1);
                    if (manager.setFreBand((short) reg)) {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                String a = getString(R.string._freBand);
                                String b = getResources().getStringArray(array.freBandArray)[reg];
//                                showtoast(a + b);

                            }
                        });
                    }
                }
            }, 1000);


        } catch (Exception ex) {

            ex.printStackTrace();
        }


        if (manager == null) {

            showtoast(getString(R.string.serialport_init_fail));
            return;
        } else {
            connectFlag = true;
        }

        Util.play(1, 0);

        setButtonClickable(buttonconnect, false);
    }

    private void setButtonClickable(Button button, boolean flag) {
        button.setClickable(flag);
        if (flag) {
            button.setTextColor(Color.BLACK);
        } else {
            button.setTextColor(Color.GRAY);
        }
    }

    private class InventoryThread extends Thread {

        private List<String> epcList;

        @Override
        public void run() {

            super.run();
            while (runFlag) {

                if (startFlag) {
                    epcList = manager.inventoryRealTime();
                    if (epcList != null && !epcList.isEmpty()) {

                        Util.play(1, 0);
                        for (final String epc : epcList) {
                            Log.i("shit", epc + " " + System.currentTimeMillis());
                            Message message = handler.obtainMessage();
                            message.what = 9876;
                            Bundle bundle = new Bundle();
                            bundle.putString("code", epc);
                            message.setData(bundle);
                            handler.sendMessage(message);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    textview_epc.setText(epc + " " + System.currentTimeMillis());
//                                }
//                            });
                        }
                    }

                    epcList = null;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 9876:
                    Bundle bundle = msg.getData();
                    String epc = bundle.getString("code");
                    textview_epc.setText(epc);
                    break;
            }
        }

    };

    @Override
    protected void onDestroy() {
        startFlag = false;
        runFlag = false;
        if (manager != null) {
            manager.close();
            manager = null;
        }
        unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    boolean connectFlag = false;
    boolean runFlag = true;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
