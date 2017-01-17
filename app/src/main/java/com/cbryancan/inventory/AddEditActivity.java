package com.cbryancan.inventory;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.Loader;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.cbryancan.inventory.data.InventoryContract;

import static com.cbryancan.inventory.R.id.sale;

public class AddEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mQuantityEditText;

    private EditText mPriceEditText;

    private Spinner mSaleSpinner;

    private boolean mProductHasChanged = false;

    private int mSale;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle("Add a Product");


        } else {

            setTitle("Edit a Product");

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSaleSpinner = (Spinner) findViewById(R.id.spinner_sale);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSaleSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter saleSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_sale_options, android.R.layout.simple_spinner_item);

        saleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mSaleSpinner.setAdapter(saleSpinnerAdapter);

        mSaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);

                    if (selection.equals("Not on Sale")) {
                        mSale = InventoryContract.ProductEntry.SALE_NOT_ON_SALE;
                    } else if (selection.equals("On Sale")) {
                        mSale = InventoryContract.ProductEntry.SALE_ON_SALE;
                    }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSale = InventoryContract.ProductEntry.SALE_NOT_ON_SALE;
            }
        });
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(quantityString) && mSale == InventoryContract.ProductEntry.SALE_NOT_ON_SALE) {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE, mSale);


        if (mCurrentProductUri == null) {

            Uri newUri = getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Failed to Add Product!",
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Product Added!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);


            if (rowsAffected == 0) {

                Toast.makeText(this, "Product Not Updated!",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "Product Updated!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_addedit, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;

            case R.id.action_delete:

                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddEditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("There are unsaved changes.  Are you sure you want to exit?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
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

    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Failed to delete!",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this,"Successfully Deleted!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        Intent intent = new Intent(AddEditActivity.this, ListActivity.class);
        startActivity(intent);    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;}
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int saleColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_SALE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
int saleStatus = cursor.getInt(saleColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(price);

            switch (saleStatus) {
                case InventoryContract.ProductEntry.SALE_NOT_ON_SALE:
                    mSaleSpinner.setSelection(0);
                    break;
                case InventoryContract.ProductEntry.SALE_ON_SALE:
                    mSaleSpinner.setSelection(1);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSaleSpinner.setSelection(0);
    }
}
