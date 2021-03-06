package com.simple.superscrollview;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this,NoFastScrollActivity.class));
            }
        });
        setListAdapter(adapter);
    }
}
