package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Kostas on 20/7/2017.
 * {@link ContentProvider} for Inventory App.
 */

public class ProductProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the products table
     */
    public static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    public static final int PRODUCT_ID = 101;

    /**
     * URI matcher object to match a context URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI of the form "content://com.example.android.inventoryapp/products" will map to the
        // integer code {@link #PRODUCTS}. This URI is used to provide access to MULTIPLE rows
        // of the products table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.inventoryapp/products/#" will map to the
        // integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row
        // of the products table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /* Database Helper Object  */
    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // Create & Initialize ProductDbHelper object to gain access to the products database
        mDbHelper = new ProductDbHelper(getContext());
        return false;
    }

    /**
     * Perform the query for the given URI.
     * Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table based on the _id to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor
        // so we know what content URI the Cursor was created for
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return saveProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a Product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri saveProduct(Uri uri, ContentValues values) {
        // First Validate the inputs


        // Check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a Name");
        }

        // Check that the price is not null or a negative number
        Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Product requires valid Price");
        }

        // Check that the offer_price is not null, a negative number
        // or greater than the starting price
        Double offerPrice = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE);
        int compareDouble = Double.compare(price, offerPrice);
        if (offerPrice < 0 || compareDouble <= 0) {
            throw new IllegalArgumentException("Product requires valid Price Offer");
        }

        // Check that the quantity is not NULL or a negative number
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid Quantity");
        }

        // Check that the Supplier Name is not null
        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Supplier name requires a Name");
        }

        // Check that the Supplier Email is not null
        String supplierEmail = values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Supplier email requires an Email");
        }

        // Check that the Supplier Email is not null
        String productImage = values.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (productImage == null) {
            throw new IllegalArgumentException("Image requires an Image");
        }


        // No need to check the image, any value is valid (including null)

        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long id = db.insert(ProductEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values.
     * Apply the changes to the rows specified in the selection and selection arguments
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // First Validate the inputs

        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_OFFER_PRICE} key is present,
        // check that the offer price value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE)) {
            Double price = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_PRICE);
            Double offerPrice = values.getAsDouble(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE);
            int compareDouble = Double.compare(price, offerPrice);
            if (offerPrice == null || offerPrice < 0 || compareDouble <= 0) {
                throw new IllegalArgumentException("Product requires valid Price Offer");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the quantity is not null or a negative number
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Product requires valid Quantity");
            }
        }

        // If the {@link ProductEntry#COLUMN_SUPPLIER_NAME} key is present,
        // check that the supplier name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Product requires a Supplier Name");
            }
        }

        // If the {@link ProductEntry#COLUMN_SUPPLIER_EMAIL} key is present,
        // check that the supplier email value is not null.
        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierEmail = values.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Product requires Supplier Email");
            }
        }

        // No need to check the image, any value is valid (including null)

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Track the number of rows that were deleted
        int rowsDeleted;

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
