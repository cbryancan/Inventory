package com.cbryancan.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cbryancan.inventory.data.InventoryContract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private TextView mNameText;

    private TextView mQuantityText;

    private TextView mPriceText;

    private TextView mSaleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        mNameText = (TextView) findViewById(R.id.detail_name);
        mQuantityText = (TextView) findViewById(R.id.detail_quantity);
        mPriceText = (TextView) findViewById(R.id.detail_price);
        mSaleText = (TextView) findViewById(R.id.detail_sale);

        Button EditButton = (Button) findViewById(R.id.edit_item);
        Button DeleteButton = (Button) findViewById(R.id.delete_item);
        Button OrderButton = (Button) findViewById(R.id.order_item);
        Button ReceiveButton = (Button) findViewById(R.id.recieve_item);
        Button SellButton = (Button) findViewById(R.id.sell_item);


        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, AddEditActivity.class);
                intent.setData(mCurrentProductUri);
                startActivity(intent);
            }
        });

        DeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();
            }
        });

        OrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String nameString = mNameText.getText().toString();
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL,new String[] { "supplier@supplierco.com" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Reorder " + nameString);
                intent.putExtra(Intent.EXTRA_TEXT, "Please send us a new shipment of " + nameString);

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        ReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
incrementQuantity();
            }
        });

        SellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQuantity();
            }
        });

        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE};


        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int saleColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int sale = cursor.getInt(saleColumnIndex);
            String saleString;
            if (sale == 0) {
                saleString = "Not on Sale";
            } else {
                saleString = "On Sale";
            }

            mNameText.setText(name);
            mQuantityText.setText(Integer.toString(quantity));
            mPriceText.setText(price);
            mSaleText.setText(saleString);

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameText.setText("");
        mQuantityText.setText("");
        mPriceText.setText("");
        mSaleText.setText("");
    }

    private void deleteProduct() {
            getContentResolver().delete(mCurrentProductUri, null, null);

                Toast.makeText(this,"Successfully Deleted!",
                        Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(DetailActivity.this, ListActivity.class);
        startActivity(intent);    }

private void incrementQuantity(){
    int quantity = Integer.valueOf(mQuantityText.getText().toString());
    quantity ++;
    ContentValues cv = new ContentValues();
    cv.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);


    getContentResolver().update(mCurrentProductUri, cv, null, null);
    }

    public void decrementQuantity(){
        int quantity = Integer.valueOf(mQuantityText.getText().toString());
        quantity --;
        if(quantity>=0) {
            ContentValues cv = new ContentValues();
            cv.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            getContentResolver().update(mCurrentProductUri, cv, null, null);
        } else{
            return;
        }
    }
}
