package com.simple.superscrollview;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.simple.superscrollview.data.Country;
import com.simple.superscrollview.data.CountryHelper;

import java.util.List;

public class MainActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<Country> countries = CountryHelper.readInCountries(this);
        
        ArrayAdapter<Country> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, countries);
        
        setListAdapter(adapter);
    }
}
