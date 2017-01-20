package com.cbryancan.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {
public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryContract.ProductEntry.TABLE_NAME + " ("
                + InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE + " TEXT, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_PIC + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
