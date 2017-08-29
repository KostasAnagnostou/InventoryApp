package com.example.android.inventoryapp;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by Kostas on 20/7/2017.
 */

public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /*  Log Tag  */
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();

    /*  Constants for Loader and Image request  */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int REQUEST_IMAGE_CODE = 1;
    private static final String STATE_IMG_URI = "STATE_URI";

    /*  UI Elements */
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mPriceOfferEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierEmailEditText;
    private Button mInsertImageButton;
    private ImageView mProductImageView;
    private Button mOrderFromSupplierButton;
    private Button mDecreaseQuantity;
    private Button mIncreaseQuantity;

    /*  Uri variables for current Product Uri & Image Uri */
    private Uri mCurrentProductUri;
    private Uri mImageUri;

    /*  Bitmap variable to store the image in the database */
    private Bitmap mImageBitmap;

    /* Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
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
        setContentView(R.layout.activity_detail);

        // Examine the Intent that was used to launch this Activity
        // in order to figure out if we are creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // if the intent DOES NOT contain a product content URI, then we know
        // that we are creating a new product
        if (mCurrentProductUri == null) {
            // This is a new pet, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        initializeUIViews();
    }

    // Initialize all relevant views that we will need to read user input from
    public void initializeUIViews() {
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceOfferEditText = (EditText) findViewById(R.id.edit_offer_price);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mInsertImageButton = (Button) findViewById(R.id.edit_product_image);
        mProductImageView = (ImageView) findViewById(R.id.product_image);
        mOrderFromSupplierButton = (Button) findViewById(R.id.order_from_supplier);
        mDecreaseQuantity = (Button) findViewById(R.id.decrease_quantity);
        mIncreaseQuantity = (Button) findViewById(R.id.increase_quantity);

        /* Setup OnTouchListeners on all the input fields, so we can determine if the user
         has touched or modified them.
        */
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceOfferEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);

        // Set the button to insert the product image
        mInsertImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertImageButton();
            }
        });

        // Set the button to decrease quantity by one
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Store the current quantity when the button is clicked on
                String currentQuantity = mQuantityEditText.getText().toString();
                int newQuantity;

                // if current quantity is empty
                if (TextUtils.isEmpty(currentQuantity)) {
                    // set the new quantity to 0
                    newQuantity = 0;
                } else { // Otherwise, get the current Quantity as Integer
                    newQuantity = Integer.parseInt(currentQuantity);

                    // if current quantity is greater than zero
                    if (newQuantity > 0) {
                        // Decrease quantity by one
                        newQuantity--;
                    } else {
                        // Otherwise, display a toast message to inform the user
                        // that is not possible to decrease the number from 0.
                        Toast.makeText(getBaseContext(), getString(R.string.toast_decrease_button), Toast.LENGTH_SHORT).show();
                    }
                }

                // Set the new Quantity to the Edit Text View
                mQuantityEditText.setText(String.format("%s", newQuantity));
            }
        });

        // Set the button to increase quantity by one
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store the current quantity when the button is clicked on
                String currentQuantity = mQuantityEditText.getText().toString();
                int newQuantity;

                // if current quantity is empty
                if (TextUtils.isEmpty(currentQuantity)) {
                    // set the new quantity to 0
                    newQuantity = 0;
                } else { // Otherwise, get the current Quantity
                    newQuantity = Integer.parseInt(currentQuantity);
                    // Increase Quantity by one
                    newQuantity++;
                }

                // Set the new Quantity to the Edit Text View
                mQuantityEditText.setText(String.format("%s", newQuantity));
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null)
            outState.putString(STATE_IMG_URI, mImageUri.toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMG_URI) &&
                !savedInstanceState.getString(STATE_IMG_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMG_URI));

            ViewTreeObserver viewTreeObserver = mProductImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mProductImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mProductImageView.setImageBitmap(getBitmapFromUri(mImageUri));
                }
            });
        }
    }

    /* Select an image from the user device */
    public void insertImageButton() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_image)), REQUEST_IMAGE_CODE);
        mProductImageView.setVisibility(View.VISIBLE);
    }

    /* Set the chosen image to the ImageView */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if requestCode, resultCode and data is ok
        if (requestCode == REQUEST_IMAGE_CODE && resultCode == RESULT_OK && data != null) {
            try {
                // Pull the uri
                mImageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                // Get image file path
                String[] filePatchColumn = {MediaStore.Images.Media.DATA};

                // Create cursor object and query image
                Cursor cursor = getContentResolver().query(mImageUri, filePatchColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePatchColumn[0]);

                // Get image path from cursor
                String imagePath = cursor.getString(columnIndex);

                // Close cursor to avoid memory leaks
                cursor.close();

                // Set the image to a Bitmap object
                mImageBitmap = BitmapFactory.decodeFile(imagePath);
                mImageBitmap = getBitmapFromUri(mImageUri);

                mProductImageView.setImageBitmap(mImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Method to get the ImageUri and display the Image -
     * example here: https://github.com/crlsndrsjmnz/MyShareImageExample/blob/master/app/src/main/java/co/carlosandresjimenez/android/myshareimageexample/MainActivity.java
     * */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImageView.getWidth();
        int targetH = mProductImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image into a Bitmap
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns
        // from the table we care about
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_OFFER_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL
        };

        // This loader will execute the ContentProvider's query method
        // on a background thread
        return new CursorLoader(this,    // Parent Activity context
                mCurrentProductUri,      // Query the content URI for the current product
                projection,              // The Columns to return for each row
                null,                    // Selection Criteria
                null,                    // Selection Criteria
                null);                   // The sort order for the returned rows
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor (data) is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {

            // Extract out the values from the Cursor for the given column index
            String productName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
            Double price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
            Double offerPrice = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE));
            final int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
            byte[] image = cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
            String supplierName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME));
            String supplierEmail = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL));

            // Update the views on the screen with the values from the database
            mNameEditText.setText(productName);
            // String.format("%s", ) --> can format Double arguments
            // and display the decimal value (.99) without crashes :D
            mPriceEditText.setText(String.format("%s", price));
            mPriceOfferEditText.setText(String.format("%s", offerPrice));
            mQuantityEditText.setText(String.valueOf(quantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);

            // set the image on the ImageView
            mImageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            mProductImageView.setVisibility(View.VISIBLE);
            mProductImageView.setImageBitmap(mImageBitmap);

            // If the current URI is not null
            if (mCurrentProductUri != null) {
                // Show the button order more from the Supplier
                mOrderFromSupplierButton.setVisibility(View.VISIBLE);

                // set a click listener on the button
                mOrderFromSupplierButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Get the supplier email
                        String email = mSupplierEmailEditText.getText().toString();

                        // Make a String Array to hold the email of the supplier
                        String[] TO = {email};

                        // Set the implicit Intent to send the Email
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse(getString(R.string.mail_to)));
                        intent.setType(getString(R.string.type_text_plain));
                        // Pass the Recipient
                        intent.putExtra(Intent.EXTRA_EMAIL, TO);
                        // Set Subject message is "New Order For: " + Product Name
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_supplier_order) + mNameEditText.getText().toString());
                        startActivity(Intent.createChooser(intent, getString(R.string.create_chooser_email)));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        ;
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mPriceOfferEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
    }

    /**
     * Get user input from editor and add new product into database.
     */
    private void saveProduct() {
        /** Read from input fields
         * Use trim to eliminate leading or trailing white space
         */
        String nameProduct = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String offerPriceString = mPriceOfferEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierEmail = mSupplierEmailEditText.getText().toString().trim();

        // Validate the name & price
        if (TextUtils.isEmpty(nameProduct)) {
            mNameEditText.requestFocus();
            Toast.makeText(this, getString(R.string.empty_name_product), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.requestFocus();
            Toast.makeText(this, getString(R.string.empty_price_value), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.requestFocus();
            Toast.makeText(this, getString(R.string.empty_quantity_value), Toast.LENGTH_SHORT).show();
        } else { // If name & price is ok, continue with code below

        /* If Offer Price is Empty, set default = 0.0
         * Double can't be a null
         */
            if (TextUtils.isEmpty(offerPriceString)) {
                offerPriceString = Double.toString(0.0);
            }

            // Store the priceString as a Double
            Double price = Double.valueOf(priceString);
            // Store the offerPriceString as a Double
            Double offerPrice = Double.valueOf(offerPriceString);
            // variable to compare the two Double values  (e.g. 15.98 < 15.99)
            int compareDouble = Double.compare(price, offerPrice);
            int quantity = Integer.valueOf(quantityString);

            // Continue Validate the user Inputs (nested validate)
            if (TextUtils.isEmpty(supplierName)) {
                mSupplierNameEditText.requestFocus();
                Toast.makeText(this, getString(R.string.empty_supplier_name), Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(supplierEmail) || (!android.util.Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches())) {
                mSupplierEmailEditText.requestFocus();
                Toast.makeText(this, getString(R.string.empty_supplier_email), Toast.LENGTH_SHORT).show();
            } else if (mImageBitmap == null) {
                mProductImageView.requestFocus();
                Toast.makeText(this, getString(R.string.empty_image), Toast.LENGTH_SHORT).show();
            } else if (compareDouble <= 0) {
                mPriceOfferEditText.requestFocus();
                Toast.makeText(this, getString(R.string.invalid_offer_price), Toast.LENGTH_SHORT).show();
            } else {// Otherwise everything is fine, add the product

                // OutputStream to write the data into a byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Write a compressed version of the bitmap to the specified outputstream (stream)
                mImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                // Retrieve the data from the Bitmap Object
                byte[] imageByteArray = stream.toByteArray();

                // Create a ContentValues object where column names are the keys,
                // and product attributes from the editor are the values.
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameProduct);
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                values.put(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE, offerPrice);
                values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
                values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmail);
                values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);

                // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
                if (mCurrentProductUri == null) {
                    // This is a NEW product, so insert a new product into the provider,
                    // return the content URI for the new product.
                    Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                    // Show a toast message depending on whether or not the insertion was successful.
                    if (newUri == null) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                                Toast.LENGTH_SHORT).show();
                        // Exit activity
                        finish();
                    }
                } else {
                    // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
                    // and pass in the new ContentValues.
                    int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(this, getString(R.string.editor_update_product_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_update_product_successful),
                                Toast.LENGTH_SHORT).show();
                        // Exit activity
                        finish();
                    }
                }
            }
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}