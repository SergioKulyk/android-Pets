package com.example.android.pets.data;


import android.provider.BaseColumns;

public final class PetContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PetContract() {
    }

    /* Inner class that defines the table contents */
    public static final class PetEntry implements BaseColumns {

        public final static String TABLE_NAME = "pets";
        
        // Columns name of the pets DB.
        public final static String COLUMN_ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME = "name";
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "gender";
        public final static String COLUMN_PET_WEIGHT = "weight";
        
        // Possible values of gender column.
        public final static int GENDER_UNKNOWN = 0;
        public final static int GENDER_MALE = 1;
        public final static int GENDER_FEMALE = 2;

        public static final String TEXT_TYPE = " TEXT";
        public static final String INTEGER_TYPE = " INTEGER";
        public static final String COMMA_SEP = ",";

        // Create command for creating the new SQLite table.
        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                        PetEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                        PetEntry.COLUMN_PET_NAME + TEXT_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_BREED + INTEGER_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_GENDER + INTEGER_TYPE + COMMA_SEP +
                        PetEntry.COLUMN_PET_WEIGHT + INTEGER_TYPE + " )";

        // DELETE command for deleting the table.
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;
    }
}
