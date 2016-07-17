package com.dan.rec;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ListActivity {
    private ListView listView = null;
    private String[] datas = {
            "RecognitionActivity"};
    private Class<?>[] toClasses = {
            RecognitionActivity.class
    };
    private int size;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = getListView();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, datas);
        listView.setAdapter(adapter);
        size = Math.min(datas.length, toClasses.length);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (position < size) {
            jumpTo(toClasses[position]);
        }
    }

    private void jumpTo(Class<?> next) {
        Intent intent = new Intent(this, next);
        startActivity(intent);
    }

}

