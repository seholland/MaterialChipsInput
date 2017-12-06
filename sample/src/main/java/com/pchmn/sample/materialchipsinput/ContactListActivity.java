package com.pchmn.sample.materialchipsinput;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = ContactListActivity.class.toString();
    @BindView(R.id.chips_input)
    ChipsInput mChipsInput;
    @BindView(R.id.validate)
    Button mValidateButton;
    @BindView(R.id.chip_list)
    TextView mChipListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        // butter knife
        ButterKnife.bind(this);
        // chips listener
        mChipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                Log.e(TAG, "chip added, " + newSize);
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                Log.e(TAG, "chip removed, " + newSize);
            }

            @Override
            public void onTextChanged(CharSequence text) {
                Log.e(TAG, "text changed: " + text.toString());
            }
        });

        // show selected chips
        mValidateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String listString = "";
                for (ContactChip chip : (List<ContactChip>) mChipsInput.getSelectedChipList()) {
                    listString += chip.getLabel() + " (" + (chip.getInfo() != null ? chip.getInfo() : "") + ")" + ", ";
                }

                mChipListText.setText(listString);
            }
        });

        parseLocationModel();

    }

    private void parseLocationModel() {
        InputStream is = getResources().openRawResource(R.raw.locations);
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Gson gson = new GsonBuilder().create();
            Type listType = new TypeToken<ArrayList<SearchLocation>>() {
            }.getType();
            List<ContactChip> locations = gson.fromJson(reader, listType);
            mChipsInput.setFilterableList(locations);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
