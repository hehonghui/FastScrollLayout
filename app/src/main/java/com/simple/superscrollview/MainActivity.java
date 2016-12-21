package com.simple.superscrollview;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.nolanlawson.supersaiyan.widget.FastScrollLayout;
import com.simple.superscrollview.data.Country;
import com.simple.superscrollview.data.CountryHelper;

import java.util.List;

public class MainActivity extends ListActivity {

    private FastScrollLayout superSaiyanScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        List<Country> countries = CountryHelper.readInCountries(this);
        
        ArrayAdapter<Country> adapter = new ArrayAdapter<Country>(
                this, android.R.layout.simple_spinner_item, countries);
        
        setListAdapter(adapter);
        
        superSaiyanScrollView = (FastScrollLayout) findViewById(R.id.scroll);
    }
}
