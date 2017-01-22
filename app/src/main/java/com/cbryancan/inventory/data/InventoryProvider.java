package com.cbryancan.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:

                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            Toast.makeText(getContext(), "Product requires a name!", Toast.LENGTH_SHORT).show();
            return null;
        }

        String price = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null) {
            Toast.makeText(getContext(), "Product requires a photo!", Toast.LENGTH_SHORT).show();
            return null;
        }

        Integer sale = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE);
        if (sale == null || InventoryContract.ProductEntry.isValidSale(sale)) {
            Toast.makeText(getContext(), "Product requires a valid sale status!", Toast.LENGTH_SHORT).show();
            return null;
        }

        Integer quantity = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity <= 0) {
            Toast.makeText(getContext(), "Product requires a valid quantity!", Toast.LENGTH_SHORT).show();
            return null;
        }

        String pic = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PIC);
        if (pic == null) {
            Toast.makeText(getContext(), "Product requires a photo!", Toast.LENGTH_SHORT).show();
            return null;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(InventoryContract.ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {

                Toast.makeText(getContext(), "Product requires a name!", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE)) {
            Integer sale = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE);
            if (sale == null || InventoryContract.ProductEntry.isValidSale(sale)) {
                Toast.makeText(getContext(), "Product requires a valid sale status!", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {

            Integer quantity = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                Toast.makeText(getContext(), "Product requires a valid quantity!", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }
        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            String price = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null) {
                Toast.makeText(getContext(), "Product requires a price!", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_PIC)) {
            String pic = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PIC);
            if (pic == null) {
                Toast.makeText(getContext(), "Product requires a photo!", Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
