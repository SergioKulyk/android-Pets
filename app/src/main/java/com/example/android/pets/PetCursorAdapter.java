package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

public class PetCursorAdapter extends CursorAdapter {

    /**
     * Create the new {@link PetCursorAdapter} object.
     * @param context is the context of the app.
     * @param cursor  is the cursor for the database work.
     */
    public PetCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find pet name TextView item by id "pet_name".
        TextView petName = view.findViewById(R.id.pet_name);
        // Find pet name TextView item by id "pet_breed".
        TextView petBreed = view.findViewById(R.id.pet_breed);

        // Figure out the index of each column
        int idNameColumn = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int idBreedColumn = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);

        // Extract pet name from cursor by {@link PetContract.PetEntry.COLUMN_PET_NAME} column name.
        String name = cursor.getString(idNameColumn);
        // Extract pet breed from cursor by {@link PetContract.PetEntry.COLUMN_PET_BREED} column name.
        String breed = cursor.getString(idBreedColumn);

        // Set name for the pet name TextView.
        petName.setText(name);
        // Set breed for the pet breed TextView.
        petBreed.setText(breed);
    }
}
