package com.cbryancan.inventory.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    private InventoryContract() {}

    public static final String CONTENT_AUTHORITY = "com.cbryancan.inventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS= "products";

    public static final class ProductEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + PATH_PRODUCTS;

        public final static String TABLE_NAME = "inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME = "name";

        public final static String COLUMN_PRODUCT_QUANTITY= "quantity";

        public final static String COLUMN_PRODUCT_SALE = "sale";

        public final static String COLUMN_PRODUCT_PRICE = "price";

        public static final int SALE_NOT_ON_SALE = 0;

        public static final int SALE_ON_SALE = 1;

        public static boolean isValidSale(int sale) {
            return sale == SALE_ON_SALE || sale == SALE_NOT_ON_SALE;
        }

    }
}
