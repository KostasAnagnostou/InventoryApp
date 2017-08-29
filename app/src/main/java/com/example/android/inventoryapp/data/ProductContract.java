package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Kostas on 20/7/2017.
 * The Structure of the database schema
 */

public class ProductContract {

    /**
     * The "Content authority" is a name for the entire content provider.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCTS = "products";

    // Empty private constructor
    private ProductContract() {
    }

    /* Inner class that defines the table constants for each Product */
    public static final class ProductEntry implements BaseColumns {

        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * Name of database table for products
         */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table). *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the Product.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Price of the Product.
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Offer Price if the product is on offer
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_OFFER_PRICE = "offer_price";

        /**
         * Quantity of the product.
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Supplier Name
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME = "supplier";

        /**
         * Supplier Email
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_EMAIL = "supplier_email";

        /**
         * Product Image
         * Type: BLOB
         */
        public final static String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    }
}
