package com.cbryancan.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.cbryancan.inventory.data.InventoryContract;

import static android.R.attr.value;


public class ListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = (ListView) findViewById(R.id.list);
        Button sellButton = (Button) findViewById(R.id.sell_item);

        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mCursorAdaptor = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdaptor);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ListActivity.this, DetailActivity.class);

                Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, id);

                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });


        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("ListActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
        case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PIC,
        };

        return new CursorLoader(this,
                InventoryContract.ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
mCursorAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
mCursorAdaptor.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete ALL items?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
