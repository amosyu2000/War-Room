package yu.amos.warroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeekViewActivity extends AppCompatActivity {
    private SharedPreferences weekList;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private final String delimiter = MainActivity.delimiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);

        weekList = getSharedPreferences("week_list", 0);

        toolbar = findViewById(R.id.week_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewPager = findViewById(R.id.week_viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout = findViewById(R.id.week_tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // Open the tab of the current day of the week
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("u");
        int current_day = Integer.parseInt(simpleDateFormat.format(new Date()));
        int index = current_day%7;
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        tab.select();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        // Returns the fragment located at a given position pos
        public Fragment getItem(int pos) {
            String[] dayNameArray = {"Sunday", "Monday", "Tuesday", "Wednesday",
                    "Thursday", "Friday", "Saturday"};
            String[] dayNumberArray = {"7", "1", "2", "3", "4", "5", "6"};

            String dayNumber = dayNumberArray[pos];
            String dayName = dayNameArray[pos];
            // Fetch the list of names from the weekList SharedPreferences file and put it in a string array
            String raw = weekList.getString(dayNumber, null);
            String[] dayPrayers = raw.split(delimiter);
            return WeekViewFragment.newInstance(dayName, dayPrayers);
        }

        @Override
        public int getCount() {
            // There are 7 fragments, one for each day of the week
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            String[] dayTabArray = {"S", "M", "T", "W", "T", "F", "S"};
            return dayTabArray[pos];
        }
    }
}
