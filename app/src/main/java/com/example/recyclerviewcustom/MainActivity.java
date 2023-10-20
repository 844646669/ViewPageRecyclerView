package com.example.recyclerviewcustom;


import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new CustomLayoutManager(2,4));
        recyclerView.setAdapter(new Adapter2());
        new CustomSnapHelper().attachToRecyclerView(recyclerView);

        ViewPager viewPager = findViewById(R.id.view_page);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager(), 0);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}