package com.cbryancan.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.cbryancan.inventory.data.InventoryContract;

import static com.cbryancan.inventory.R.id.quantity;

public class ProductCursorAdapter extends CursorAdapter {


    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView saleTextView = (TextView) view.findViewById(R.id.sale);
        Button sellButton = (Button) view.findViewById(R.id.sell_item);
        int cursor_id = cursor.getInt(cursor.getColumnIndex(InventoryContract.ProductEntry._ID));
        final Uri currentUri = Uri.withAppendedPath(InventoryContract.ProductEntry.CONTENT_URI, String.valueOf(cursor_id));

        final int idColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int saleColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE);

        final int productQuantity = cursor.getInt(quantityColumnIndex);

        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        int productSale = cursor.getInt(saleColumnIndex);
        String saleString;
        if (productSale == 0) {
            saleString = "Not on Sale";
        } else {
            saleString = "On Sale";
        }

        nameTextView.setText(productName);
        quantityTextView.setText(String.valueOf(productQuantity));
        priceTextView.setText(productPrice);
        saleTextView.setText(saleString);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(idColumnIndex);
                if (productQuantity > 0) {
                    ContentValues cv = new ContentValues();
                    int newItemQuantity = productQuantity - 1;
                    cv.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newItemQuantity);
                    view.getContext().getContentResolver().update(currentUri, cv, null, null);
                } else {
                    return;
                }            }
        });

    }

}
