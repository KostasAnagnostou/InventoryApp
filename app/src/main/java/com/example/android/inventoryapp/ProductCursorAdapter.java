package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Kostas on 21/7/2017.
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // initialize the ViewHolder to handle the views
        ViewHolder viewHolder = new ViewHolder(view);

        // Read and store the attributes from the Cursor for the current pet
        String productName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        Double price = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));
        Double offerPrice = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_OFFER_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        byte[] image = cursor.getBlob(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE));
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

        // if image isn't empty, load the product image
        if (image != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            viewHolder.productImageView.setImageBitmap(bitmap);
        }

        // Set the Values on the UI
        viewHolder.nameTextView.setText(productName);
        viewHolder.productPriceTextView.setText(context.getString(R.string.category_price) + " " + price + " " + context.getString(R.string.price_currency));
        viewHolder.productQuantityTextView.setText(context.getString(R.string.category_quantity) + " " + quantity);

        // if the offerPrice has a valid value
        if (offerPrice > 0) {
            // set the special offer image on the product
            viewHolder.hasOfferView.setVisibility(View.VISIBLE);
            // Display the product Offer Price TextView
            viewHolder.productPriceOfferTextView.setVisibility(View.VISIBLE);
            viewHolder.productPriceOfferTextView.setText(context.getString(R.string.category_price_offer) + " " + offerPrice + " " + context.getString(R.string.price_currency));
        } else if (offerPrice == 0.0) {
            viewHolder.hasOfferView.setVisibility(View.GONE);
            viewHolder.productPriceOfferTextView.setVisibility(View.GONE);
        }
        // Set an OnClick Listener to the sales button
        viewHolder.salesButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                reduceQuantity(context, uri, quantity);
            }
        });
    }

    // Reduces the quantity available by one
    private void reduceQuantity(Context context, Uri uri, int quantity) {

        // if quantity is not empty
        if (quantity > 0) {            // reduce the quantity by one
            int newQuantity = quantity - 1;

            // Update the value with the new quantity
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

            // Returns the number of database rows affected by the update statement
            int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

            // If no rows were updated
            if (rowsUpdated == 0) {
                Log.i(LOG_TAG, "The product quantity hasn't updated");
            }
        } else {
            Toast.makeText(context, context.getString(R.string.sale_zero_quantity), Toast.LENGTH_SHORT).show();
        }
    }

    // We use ViewHolder Design Pattern to find & handle the views in list_item.xml
    // This Article (from Vlad) helped --> http://spreys.com/view-holder-design-pattern-for-android/

    private class ViewHolder {
        private TextView nameTextView;
        private TextView productPriceTextView;
        private TextView productQuantityTextView;
        private TextView productPriceOfferTextView;
        private Button salesButtonView;
        private ImageView productImageView;
        private ImageView hasOfferView;

        // Initialize the Views with ViewHolder Pattern
        private ViewHolder(@NonNull View view) {
            this.nameTextView = (TextView) view.findViewById(R.id.product_name);
            this.productPriceTextView = (TextView) view.findViewById(R.id.price);
            this.productQuantityTextView = (TextView) view.findViewById(R.id.quantity);
            this.productPriceOfferTextView = (TextView) view.findViewById(R.id.has_offer_view);
            this.salesButtonView = (Button) view.findViewById(R.id.sale_button);
            this.productImageView = (ImageView) view.findViewById(R.id.list_item_image);
            this.hasOfferView = (ImageView) view.findViewById(R.id.special_offer);

        }
    }
}