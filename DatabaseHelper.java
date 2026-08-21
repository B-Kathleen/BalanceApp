package com.example.cs360project;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weighttracker.db";
    private static final int VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //Users table
    private static final class UsersTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
        private static final String COL_NAME = "name";
        private static final String COL_PHONE = "phone";
        private static final String COL_GOAL_WEIGHT = "goal_weight";
    }

    //Weights database
    private static final class WeightTable {
        private static final String TABLE = "weights";
        private static final String COL_ID = "_id";
        private static final String COL_USER_ID = "user_id";
        private static final String COL_DATE = "date";
        private static final String COL_WEIGHT = "weight";
    }

    // Create tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + UsersTable.TABLE + " (" +
                UsersTable.COL_ID + " integer primary key autoincrement, " +
                UsersTable.COL_USERNAME + " text not null unique, " +
                UsersTable.COL_PASSWORD + " text not null, " +
                UsersTable.COL_NAME + " text not null, " +
                UsersTable.COL_PHONE + " text not null, " +
                UsersTable.COL_GOAL_WEIGHT + " real)");

        db.execSQL("create table " + WeightTable.TABLE + " (" +
                WeightTable.COL_ID + " integer primary key autoincrement, " +
                WeightTable.COL_USER_ID + " integer, " +
                WeightTable.COL_DATE + " date, " +
                WeightTable.COL_WEIGHT + " real)");
    }

    // Drop tables when upgrading tables
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {

        db.execSQL("drop table if exists " + UsersTable.TABLE);
        db.execSQL("drop table if exists " + WeightTable.TABLE);

        onCreate(db);
    }

    // Add New User
    public long addUser(String username, String password, String name, String phone, float goalWeight) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsersTable.COL_USERNAME, username);
        values.put(UsersTable.COL_PASSWORD, password);
        values.put(UsersTable.COL_NAME, name);
        values.put(UsersTable.COL_PHONE, phone);
        values.put(UsersTable.COL_GOAL_WEIGHT, goalWeight);

        long userId = db.insert(UsersTable.TABLE, null, values);

        return userId;
    }

    // Check username and password credentials
    public long checkLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + UsersTable.COL_ID +
                " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_USERNAME + " = ?" +
                " and " + UsersTable.COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] { username, password });

        long userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getLong(0);}

        cursor.close();

        return userId;
    }

    // Get user's name
    public String getUserName(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + UsersTable.COL_NAME +
                " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_ID + " = ?";

        Cursor cursor = db.rawQuery(
                sql,
                new String[] { String.valueOf(userId) }
        );

        String name = "";

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }

        cursor.close();

        return name;
    }

    // Get user's phone number
    public String getUserPhone(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + UsersTable.COL_PHONE +
                " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_ID + " = ?";

        Cursor cursor = db.rawQuery(
                sql,
                new String[] { String.valueOf(userId) }
        );

        String phone = "";

        if (cursor.moveToFirst()) {
            phone = cursor.getString(0);
        }

        cursor.close();

        return phone;
    }

    // Get user's goal weight
    public float getGoalWeight(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + UsersTable.COL_GOAL_WEIGHT +
                " from " + UsersTable.TABLE +
                " where " + UsersTable.COL_ID + " = ?";

        Cursor cursor = db.rawQuery(
                sql,
                new String[] { String.valueOf(userId) }
        );

        float goalWeight = 0;

        if (cursor.moveToFirst()) {
            goalWeight = cursor.getFloat(0);
        }

        cursor.close();

        return goalWeight;
    }

    // Get all weights for a user
    public Cursor getWeights(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " +
                WeightTable.COL_ID + ", " +
                WeightTable.COL_DATE + ", " +
                WeightTable.COL_WEIGHT +
                " from " + WeightTable.TABLE +
                " where " + WeightTable.COL_USER_ID + " = ?" +
                " order by " + WeightTable.COL_DATE + " desc";

        return db.rawQuery(
                sql,
                new String[] { String.valueOf(userId) }
        );
    }

    // Get the user's most recent weight
    public float getCurrentWeight(long userId) {

        SQLiteDatabase db = getReadableDatabase();

        String sql = "select " + WeightTable.COL_WEIGHT +
                " from " + WeightTable.TABLE +
                " where " + WeightTable.COL_USER_ID + " = ?" +
                " order by " + WeightTable.COL_DATE + " desc" +
                " limit 1";

        Cursor cursor = db.rawQuery(
                sql,
                new String[] { String.valueOf(userId) }
        );

        float currentWeight = 0;

        if (cursor.moveToFirst()) {
            currentWeight = cursor.getFloat(0);
        }

        cursor.close();

        return currentWeight;
    }

    // Weights operations

    // Add weight
    public long addWeight(long userId, String date, float weight) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeightTable.COL_USER_ID, userId);
        values.put(WeightTable.COL_DATE, date);
        values.put(WeightTable.COL_WEIGHT, weight);

        long weightId = db.insert(WeightTable.TABLE, null, values);

        return weightId;
    }

    // Update weight information
    public int updateWeight(long weightId, String date, float weight) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(WeightTable.COL_DATE, date);
        values.put(WeightTable.COL_WEIGHT, weight);

        int rowsUpdated = db.update(
                WeightTable.TABLE,
                values,
                WeightTable.COL_ID + " = ?",
                new String[] { String.valueOf(weightId) }
        );

        return rowsUpdated;
    }

    // Delete weight entry
    public int deleteWeight(long weightId) {

        SQLiteDatabase db = getWritableDatabase();

        int rowsDeleted = db.delete(
                WeightTable.TABLE,
                WeightTable.COL_ID + " = ?",
                new String[] { String.valueOf(weightId) }
        );

        return rowsDeleted;
    }

    // Update user's profile information
    // Update user's profile information
    public int updateUserProfile(
            long userId,
            String name,
            String phone,
            float goalWeight) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(UsersTable.COL_NAME, name);
        values.put(UsersTable.COL_PHONE, phone);
        values.put(UsersTable.COL_GOAL_WEIGHT, goalWeight);

        int rowsUpdated = db.update(
                UsersTable.TABLE,
                values,
                UsersTable.COL_ID + " = ?",
                new String[] { String.valueOf(userId) }
        );

        return rowsUpdated;
    }
}