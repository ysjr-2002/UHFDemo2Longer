package com.uhfdemo2longer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MyData.RunnerDBHelper;
import com.MyData.Runner;
import com.MyData.RunnerDBManager;
import com.MyData.Tools;
import com.handheld.UHFLonger.UHFLongerManager;
import com.handheld.UHFLongerDemo.Util;
import com.uhfdemo2longer.R.array;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.MyData.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView textview_epc;
    private Toast toast;
    private String root = "";
    private UHFLongerManager manager = null;
    private InventoryThread thread = null;
    private KeyReceiver keyReceiver = null;
    private RunnerDBManager dbHelper;
    private boolean connectFlag = false;
    private boolean runFlag = true;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //���Ĵ���.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            //��״̬��������ɫ��������͸����
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

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

    protected void setTranslucentStatus() {
        // 5.0����ϵͳ״̬��͸��
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onStart() {
        dbHelper = RunnerDBManager.getInstance(this);
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

    private void initView() {
        textview_epc = (TextView) findViewById(R.id.textview_epc);
    }

    private class KeyReceiver extends BroadcastReceiver {
        private String TAG = "KeyReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                        showtoast("f1");
                        break;
                    case KeyEvent.KEYCODE_F2:
                        showtoast("f2");
                        break;
                    case KeyEvent.KEYCODE_F3:
                        showtoast("f3");
                        scan();
                        break;
                    case KeyEvent.KEYCODE_F5:
                        showtoast("f5");
                        break;
                    case KeyEvent.KEYCODE_F4:
                        //�ְֳ���
                        startFlag = true;
                        scan();
                        break;
                }
            } else {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F4:
                        startFlag = false;
                        preepc = "";
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
            for (int i = 0; i < children.length; i++) {
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
        }
    }

    boolean startFlag = false;

    private void scan() {
        if (manager == null) return;
        if (!startFlag) {
            startFlag = true;
            Log.i(TAG, "��ʼɨ��");
        } else {
            startFlag = false;
            Log.i(TAG, "ֹͣɨ��");
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
    }

    private void setButtonClickable(Button button, boolean flag) {
        button.setClickable(flag);
        if (flag) {
            button.setTextColor(Color.BLACK);
        } else {
            button.setTextColor(Color.GRAY);
        }
    }

    String preepc = "";

    private class InventoryThread extends Thread {
        private List<String> epcList;

        @Override
        public void run() {

            super.run();
            while (runFlag) {

                if (startFlag && manager != null) {
                    epcList = manager.inventoryRealTime();
                    if (epcList != null && !epcList.isEmpty()) {
                        for (String epc : epcList) {
                            if (preepc.equals(epc)) {
                                Log.e(TAG, "�ظ�����->" + epc);
                                break;
                            }
                            Message message = handler.obtainMessage();
                            message.what = 9876;
                            Util.play(1, 0);
                            preepc = epc;
                            Bundle bundle = new Bundle();
                            bundle.putString("code", epc);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }

                    epcList = null;
                    try {
                        Thread.sleep(20);
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
                    showRunner("25");
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import:
                doImport();
                break;
            case R.id.menu_clear:
                File file = new File(root + "//aa");
                Tools.deleteFile(file);
                dbHelper.clear();
                showtoast("ִ�гɹ�");
                break;
            case R.id.menu_about:
                AboutDialog();
                break;
            case R.id.menu_test:
                showRunner("25");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRunner(String key) {

        Runner runner = dbHelper.getRunner(key);
        if (runner != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("shit");
//            builder.setView(R.layout.dialog_runner);

            View temp = getLayoutInflater().inflate(R.layout.dialog_runner, null);
            TextView name = (TextView) temp.findViewById(R.id.textView_name);
            TextView code = (TextView) temp.findViewById(R.id.textView_code);
            TextView gender = (TextView) temp.findViewById(R.id.textView_gender);
            TextView group = (TextView) temp.findViewById(R.id.textView_group);
            ImageView photo = (ImageView) temp.findViewById(R.id.imageView_photo);
            builder.setView(temp);

            name.setText(runner.getName());
            code.setText(runner.getCode());
            gender.setText(runner.getGender());
            group.setText(runner.getGroup());

            photo.setImageBitmap(getBitmap(runner.getPhoto()));

            builder.show();

        } else {
            showtoast("��ѯʧ��");
        }
    }

    private Bitmap getBitmap(String path) {

        Bitmap bitmap = null;
        path = Environment.getExternalStorageDirectory().getPath() + path;
        bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

    int requestCode = 1;

    private void doImport() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.requestCode) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                String scheme = uri.getScheme();
                String path = GetPathFromUri4kitkat.getPath(this, uri);
                if (path == null) {
                    path = GetPathFromUri4kitkat.getDataColumn(this, uri, null, null);
                    if (path == null) {
                        showtoast("��ȡ�ļ�·������");
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            imporExcel(path);
                        }
                    }
                } else {
                    File file = new File(path);
                    if (file.exists()) {
                        imporExcel(path);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ProgressDialog dialog;
    private Thread threadImport;

    private void imporExcel(String path) {

        //��֧�ֶ�ȡexcel 97-2003
        try {

            String newFolder = root + "//aa";
            File temp = new File(newFolder);
            if (temp.exists() == false) {
                temp.mkdir();
            }

            Workbook book = Workbook.getWorkbook(new File(path));
            final Sheet sheet = book.getSheet(0);
            final int Rows = sheet.getRows();
            int Cols = sheet.getColumns();
            Log.d(TAG, "��ǰ�����������:" + sheet.getName());
            Log.d(TAG, "������:" + Rows + ", ������:" + Cols);

            final int copymax = Rows - 1;

            dialog = new ProgressDialog(this);
            dialog.setTitle("���ݵ���");
            dialog.setIcon(R.mipmap.ic_launcher_round);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax(copymax);
            dialog.show();

            final String source = "/download/ysj.jpg";
            threadImport = new Thread() {
                int step = 1;

                @Override
                public void run() {
                    super.run();
                    try {
                        for (int i = 1; i < Rows; i++) {

                            String name = ReadData(sheet, i, 0);
                            String code = ReadData(sheet, i, 1);
                            String gender = ReadData(sheet, i, 2);
                            String group = ReadData(sheet, i, 3);
                            String photo = ReadData(sheet, i, 4);
                            Log.d(TAG, name + " " + code + " " + photo);

                            String newPath = "//aa//" + Integer.toString(i) + ".jpg";
                            fileCopy(source, newPath);

                            dialog.setProgress(i);
                            Runner runner = new Runner();
                            runner.setName(name);
                            runner.setCode(code);
                            runner.setGender(gender);
                            runner.setGroup(group);
                            runner.setPhoto(newPath);
                            runner.setConfirm(getCurrentDateTime());
                            dbHelper.insert(runner);

                            //Thread.sleep(100);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    dialog.cancel();
                }
            };
            threadImport.start();


        } catch (java.io.IOException ex) {

            ex.printStackTrace();

        } catch (jxl.read.biff.BiffException ex) {
            ex.printStackTrace();
        }
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//�������ڸ�ʽ
        String temp = df.format(new Date());
        return temp;
    }

    private void fileCopy(String source, String density) {

        source = root + source;
        density = root + density;
        Log.d(TAG, density);
        Tools.copyFile(source, density);
    }

    public static String ReadData(Sheet excelSheet, int row, int col) {
        try {
            String CellData = "";
            Cell cell = excelSheet.getRow(row)[col];
            CellData = cell.getContents().toString();
            return CellData;
        } catch (Exception e) {
            return "";
        }
    }

    private void AboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("����ֵ�ȶ�ϵͳ \n V1.0");
        builder.setNegativeButton("ȷ��", null);
        builder.show();
    }
}
