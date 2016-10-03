/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.core.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nexmo.sdk.conversation.client.Conversation;
import com.nexmo.sdk.conversation.common.util.DateUtil;
import com.nexmo.sdk.conversation.core.persistence.contract.ConversationContract.*;
import com.nexmo.sdk.conversation.core.persistence.contract.MemberContract.*;
import com.nexmo.sdk.conversation.core.persistence.contract.TextEventContract.*;
import com.nexmo.sdk.conversation.core.persistence.contract.ImageEventContract.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Database helper for updating and accessing the cached conversations.
 */
public class CacheDB extends SQLiteOpenHelper {
    public static final String TAG = CacheDB.class.getSimpleName();
    private static CacheDB sInstance;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ConversationCache.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_CONVERSATION_ENTRIES =
            "CREATE TABLE " + ConversationEntry.TABLE_NAME + " (" +
                    ConversationEntry.COLUMN_CID + TEXT_TYPE + " PRIMARY KEY," +
                    ConversationEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    ConversationEntry.COLUMN_CREATED + TEXT_TYPE + COMMA_SEP +
                    ConversationEntry.COLUMN_LAST_EVENT_ID + TEXT_TYPE + COMMA_SEP +
                    ConversationEntry.COLUMN_MEMBER_ID + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_MEMBER_ENTRIES =
            "CREATE TABLE " + MemberEntry.TABLE_NAME + " (" +
                    MemberEntry.COLUMN_MEMBER_ID + TEXT_TYPE + " PRIMARY KEY," +
                    MemberEntry.COLUMN_USERNAME + TEXT_TYPE + COMMA_SEP +
                    MemberEntry.COLUMN_USER_ID + TEXT_TYPE +
                    MemberEntry.COLUMN_STATE + TEXT_TYPE + COMMA_SEP +
                    MemberEntry.COLUMN_INVITEDAT + TEXT_TYPE + COMMA_SEP +
                    MemberEntry.COLUMN_JOINEDAT + TEXT_TYPE + COMMA_SEP +
                    MemberEntry.COLUMN_LEFTAT + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_TEXT_EVENT_ENTRIES =
            "CREATE TABLE " + TextEntry.TABLE_NAME + " (" +
                    TextEntry.COLUMN_EVENT_ID + TEXT_TYPE + " PRIMARY KEY," +
                    TextEntry.COLUMN_CID + TEXT_TYPE + COMMA_SEP +
                    TextEntry.COLUMN_PAYLOAD + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_IMAGE_EVENT_ENTRIES =
            "CREATE TABLE " + ImageEntry.TABLE_NAME + " (" +
                    ImageEntry.COLUMN_EVENT_ID + TEXT_TYPE + " PRIMARY KEY," +
                    ImageEntry.COLUMN_CID + TEXT_TYPE + COMMA_SEP +
                    ImageEntry.COLUMN_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_CONVERSATION_ENTRIES =
            "DROP TABLE IF EXISTS " + ConversationEntry.TABLE_NAME;
    private static final String SQL_DELETE_MEMBER_ENTRIES =
            "DROP TABLE IF EXISTS " + MemberEntry.TABLE_NAME;
    private static final String SQL_DELETE_TEXT_ENTRIES =
            "DROP TABLE IF EXISTS " + TextEntry.TABLE_NAME;
    private static final String SQL_DELETE_IMAGE_ENTRIES =
            "DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME;

    CacheDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //hasConversations();
        //checkDataBase();
    }

    public static synchronized CacheDB getInstance(Context context) {
        if (sInstance == null)
            sInstance = new CacheDB(context.getApplicationContext());

        return sInstance;
    }

    private boolean checkDataBase(){
        SQLiteDatabase checkDB = null;
        try{
            checkDB = SQLiteDatabase.openDatabase("data/data/com.nexmo.sdk.conversation/databases/ConversationCache.db", null, SQLiteDatabase.OPEN_READONLY);
            //SQLiteDatabase: /data/data/com.nexmo.sdk.conversation/databases/ConversationCache.db
        }catch(SQLiteException e){
            //database does't exist yet.
            Log.d(TAG, "database cannot be created");

        }

        if(checkDB != null){
            Log.d(TAG, "database is created");
            checkDB.close();
        }

        return checkDB != null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "ConversationCache.db onCreate");
        db.execSQL(SQL_CREATE_CONVERSATION_ENTRIES);
        db.execSQL(SQL_CREATE_MEMBER_ENTRIES);
        db.execSQL(SQL_CREATE_TEXT_EVENT_ENTRIES);
        db.execSQL(SQL_CREATE_IMAGE_EVENT_ENTRIES);

        //hasConversations();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "ConversationCache.db onUpgrade Old version: " + oldVersion + " to new version: " + newVersion);
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        switch(oldVersion) {
            case 1:
                //fall through
            default:
                db.execSQL(SQL_DELETE_CONVERSATION_ENTRIES);
                db.execSQL(SQL_DELETE_MEMBER_ENTRIES);
                db.execSQL(SQL_DELETE_TEXT_ENTRIES);
                db.execSQL(SQL_DELETE_IMAGE_ENTRIES);
                onCreate(db);
                break;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "ConversationCache.db onDowngrade from new version: " + newVersion + " to Old version: " + oldVersion);
        //onUpgrade(db, oldVersion, newVersion);
    }

    public void insertConversations(List<Conversation> conversationList) {
        for (Conversation conversation : conversationList)
            insertConversation(conversation);
    }

    public void insertConversation(Conversation conversation) {
        Log.d(TAG, "insertConversation " + conversation.toString());
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ConversationEntry.COLUMN_CID, conversation.getConversationId());
        values.put(ConversationEntry.COLUMN_NAME, conversation.getName());
        values.put(ConversationEntry.COLUMN_CREATED,
                   DateUtil.formatIso8601DateString(conversation.getCreationDate()));
        values.put(ConversationEntry.COLUMN_LAST_EVENT_ID, conversation.getLastEventId());
        values.put(ConversationEntry.COLUMN_MEMBER_ID, conversation.getSelf().getMemberId());

        // Insert the new row, returning the primary key value of the new row
        long pk = db.insertWithOnConflict(
                ConversationEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "insertConversation: row id " + pk);
    }

    public void insertConversationDetailed(Conversation conversation) {

    }

    public boolean hasConversations() {
        //try
        SQLiteDatabase db = this.getReadableDatabase();


        Log.d(TAG, db.getPath());
        long count = DatabaseUtils.queryNumEntries(db, ConversationEntry.TABLE_NAME);

        db.close();
        return (count > 0);
    }

    public boolean hasConversation(final String cid) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ConversationEntry.COLUMN_CID
        };

        // Which row to update, based on the ID
        String selection = ConversationEntry.COLUMN_CID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(cid) };

        Cursor c = db.query(
                ConversationEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        int count=0;
        if (c != null) {
            count = c.getCount();
            c.close();
        }

        db.close();
        return (count > 0);
    }

    public void updateConversation(Conversation conversation){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        //get conversation.getCid() row
        Conversation oldConversation = readConversationInfo(conversation.getConversationId());


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ConversationEntry.COLUMN_CID, conversation.getConversationId());
        values.put(ConversationEntry.COLUMN_NAME, conversation.getName());
        //values.put(ConversationEntry.COLUMN_NAME_CONTENT, content);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ConversationEntry.TABLE_NAME,
                ConversationEntry.COLUMN_CID,
                values);

        Log.d(TAG, "new id " + newRowId);

    }

    public ArrayList<Conversation> readConversationsList(){

        return null;
    }

    //the text and images events.
    public Conversation readConversationEvents(final String cid) {
        return null;
    }

    // only members, no events.
    public Conversation readConversationInfo(final String cid){
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ConversationEntry.COLUMN_CID,
                ConversationEntry.COLUMN_NAME,
                ConversationEntry.COLUMN_CREATED,
                ConversationEntry.COLUMN_LAST_EVENT_ID,
                ConversationEntry.COLUMN_MEMBER_ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                ConversationEntry.COLUMN_NAME + " DESC";

        // Which row to update, based on the ID
        String selection = ConversationEntry.COLUMN_CID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(cid) };

        Cursor c = db.query(
                ConversationEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if (c != null) {
            c.moveToFirst();
            String created = c.getString(c.getColumnIndex(ConversationEntry.COLUMN_CREATED));
            Date timestamp = null;
            try {
                timestamp = DateUtil.formatIso8601DateString(created);
            } catch (ParseException e) {

            }
            Conversation conversation = new Conversation(
                    c.getString(c.getColumnIndex(ConversationEntry.COLUMN_NAME)),
                    c.getString(c.getColumnIndex(ConversationEntry.COLUMN_CID)),
                    c.getString(c.getColumnIndex(ConversationEntry.COLUMN_MEMBER_ID)),
                    timestamp,
                    c.getString(c.getColumnIndex(ConversationEntry.COLUMN_LAST_EVENT_ID)));
            c.close();
        }


        return null;
    }

    //app is closing, or use

    //clear cache manually or on explicit logout
    public void clearDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ ConversationEntry.TABLE_NAME);
        db.execSQL("delete from " + MemberEntry.TABLE_NAME);
        db.execSQL("delete from " + TextEntry.TABLE_NAME);
        db.execSQL("delete from " + ImageEntry.TABLE_NAME);

        db.execSQL(SQL_DELETE_CONVERSATION_ENTRIES);
        db.execSQL(SQL_DELETE_MEMBER_ENTRIES);
        db.execSQL(SQL_DELETE_TEXT_ENTRIES);
        db.execSQL(SQL_DELETE_IMAGE_ENTRIES);

        db.close();
    }

    public void dropDB() {
        Log.d(TAG, "dropDB");
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(SQL_DELETE_CONVERSATION_ENTRIES);
        db.execSQL(SQL_DELETE_MEMBER_ENTRIES);
        db.execSQL(SQL_DELETE_TEXT_ENTRIES);
        db.execSQL(SQL_DELETE_IMAGE_ENTRIES);

        db.close();
    }

}
