package com.MyData;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shaojie on 2017/9/16.
 */


public class DatabaseHelper extends SQLiteOpenHelper {

    private Context mContext = null;


    public static final String CREATE_RUNNER = "create table Runner (" +
            "id integer primary key autoincrement, " +
            "name text, " +
            "code text, " +
            "photo text)";


    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO 创建数据库后，对数据库的操作

        db.execSQL(CREATE_RUNNER);
        Log.i("ysj", "create table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 更改数据库版本的操作
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // TODO 每次成功打开数据库后首先被执行
    }

    private SQLiteDatabase db;

    public void insert(MyRunner runner) {
        String sql = "insert into runner(name,code,photo) values('" + runner.getName() + "','" + runner.getCode() + "','" + runner.getPhoto() + "')";
        db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public MyRunner search(String code) {

        String sql = "select * from runner where code='" + code + "'";
        db = this.getWritableDatabase();
        db.beginTransaction();

        MyRunner runner = null;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {

            List<String> list = new ArrayList<String>();
            while (cursor.moveToNext()) {

                runner = new MyRunner();
                String name = cursor.getString(1);
                String photo = cursor.getString(3);
                runner.setName(name);
                runner.setPhoto(photo);
                break;
            }
        }
        db.endTransaction();
        return runner;
    }

    public int count() {

        String sql = "select count(*) from runner";
        db = this.getReadableDatabase();
//        db = this.getReadableDatabase();
//        db.execSQL(sql);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();

        int mcount = cursor.getInt(0);

        return mcount;

    }


    public void clear() {
        String sql = "delete from runner";
        db = this.getWritableDatabase();
        db.execSQL(sql);
    }
}

