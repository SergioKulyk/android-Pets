package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PETS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PETS_ID}. This URI is used to provide access to ONE single row
        // of the pets table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    /**
     * Database helper that will provide us access to the database
     */
    private PetDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Cursor object for getting values from database rows.
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Get data from the database by current parameters.
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case PET_ID:
                projection = new String[]{PetContract.PetEntry._ID,
                        PetContract.PetEntry.COLUMN_PET_NAME,
                        PetContract.PetEntry.COLUMN_PET_BREED,
                        PetContract.PetEntry.COLUMN_PET_GENDER,
                        PetContract.PetEntry.COLUMN_PET_WEIGHT};
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Get data from the database by current parameters.
                cursor = db.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so, we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Get code according to uri.
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                // Insert pet into database.
                return insertPet(uri, contentValues);
            default:
                // Uri is not supported.
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert pets into database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Check that name is not null.
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires name");
        }

        // Check that gender is MALE or FEMALE or UNKNOWN.
        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires gender");
        }

        // Check that weight is grater than 0 or equal 0.
        // 0 is the value by default.
        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight == null || weight < 0) {
            throw new IllegalArgumentException("Pet requires weight");
        }

        // Get database in wrote mode.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert pet into database and return pet id.
        long rowInserted = db.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        // If ID is -1, then insertion failed. Log an error and return null;
        if (rowInserted == -1) {
            Log.e(LOG_TAG, "Failed to inset row for " + uri);
            return null;
        } else {
            // If 1 or more rows were updated, than notify all listeners than data at the
            // given URI has changed.
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the new URL with the ID (of the newly insertion row) appended at the end.
        return ContentUris.withAppendedId(uri, rowInserted);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in database.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link PetEntry#COLUMN_PET_NAME} is present,
        // check that name is not null.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} is present
        // check that gender is the {@link GENDER_UNKNOWN} or {@link GENDER_MALE} or {@link GENDER_FEMALE}.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} is present
        // check that weight is grater than 0 or equal 0.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight == null || weight < 0) {
                throw new IllegalArgumentException("Pet requires weight");
            }
        }

        // If there is not values to update, then don't try to update the database.
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data.
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Update data of the current dog in database, return id of row
        // where updating was.
        int rowsUpdated = db.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, than notify all listeners than data at the
        // given URI has changed.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get  writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args.
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PET_ID:
                // Delete a single row given by ID int the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);

                // If 1 or more rows were deleted, then notify all listeners that the data at the
                // given URI has changed
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}