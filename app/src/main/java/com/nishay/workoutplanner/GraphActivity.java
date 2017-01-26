package com.nishay.workoutplanner;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nishay.sql.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphActivity extends AppCompatActivity {

    private SQL sql;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private List<ImageView> dots;
    ArrayList<Pair<String, String>> fullExerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sql = new SQL(this);
        sql.open();
       // GraphView graph = (GraphView) findViewById(R.id.graph_view);
        Random r = new Random();

        fullExerciseList = new ArrayList<>();

        //grab list of sets
        ArrayList <String> setList = sql.getAllSetsNames();
        for(String set : setList) {
            //grab all exercises for this set
            ArrayList<WorkoutExercise> exercises = sql.getExercisesList(set);
            for(WorkoutExercise e : exercises) {
                Pair p = Pair.create(set, e.getName());
                fullExerciseList.add(p);
            }
        }

        mPager = (ViewPager) findViewById(R.id.graph_pager);
        mPagerAdapter = new GraphPagerAdapter(getSupportFragmentManager(), fullExerciseList);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        addDots();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.graph_menu_delete) {
            //delete all and recreate
            sql.deleteHistory();

            recreate();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        sql.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        sql.open();
    }

    public void addDots() {
        dots = new ArrayList<>();
        LinearLayout dotsLayout = (LinearLayout)findViewById(R.id.dots);

        for(int i = 0; i < fullExerciseList.size()+1; i++) {
            ImageView dot = new ImageView(this);
            if(i == 0) {
                dot.setImageDrawable(getResources().getDrawable(R.drawable.selected_dot));
            }
            else {
                dot.setImageDrawable(getResources().getDrawable(R.drawable.unselected_dot));
            }


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            dot.setPadding(10,5,10,5);
            dotsLayout.addView(dot, params);

            dots.add(dot);
        }

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectDot(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void selectDot(int idx) {
        Resources res = getResources();
        for(int i = 0; i < fullExerciseList.size()+1; i++) {
            int drawableId = (i==idx)?(R.drawable.selected_dot):(R.drawable.unselected_dot);
            Drawable drawable = res.getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }

}

class GraphPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Pair<String, String>> list;

    public GraphPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public GraphPagerAdapter(FragmentManager fm, ArrayList<Pair<String, String>> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", position);
        bundle.putSerializable("list", list);
        GraphFragment fragment = new GraphFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return list.size()+1;
    }
}

class ZoomOutPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}
