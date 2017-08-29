package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Kostas on 20/7/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    // Log Tag
    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "inventory.db";

    // The database version.
    private static final int DATABASE_VERSION = 1;

    // The constant that contains the SQL command
    // for creating the products table into the database
    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                    ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                    ProductEntry.COLUMN_PRODUCT_OFFER_PRICE + " REAL, " +
                    ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0 , " +
                    ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, " +
                    ProductEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL) " +
                    ";";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // When the on create methods runs
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL Command & create the Table
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Constant for the SQL command DROP TABLE (delete table)
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}