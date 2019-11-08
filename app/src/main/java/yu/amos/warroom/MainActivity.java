package yu.amos.warroom;

import yu.amos.warroom.WarRoomRes.PrayerAlgorithm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private SharedPreferences res, prayerList, weekList;
    private NavigationView navView;
    private LinearLayout votdLinearLayout, prayersLinearLayout;
    final public static String delimiter = "/~/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        res = getSharedPreferences("res", 0);
        prayerList = getSharedPreferences("prayer_list", 0);
        weekList = getSharedPreferences("week_list", 0);
        checkSharedPreferences();

        updateDate();
        new updateVOTD().execute();

        votdLinearLayout = findViewById(R.id.votd_linearLayout);
        prayersLinearLayout = findViewById(R.id.prayers_linearLayout);

        dl = (DrawerLayout)findViewById(R.id.drawer_layout);
        t = new ActionBarDrawerToggle(this, dl,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        dl.addDrawerListener(t);
        t.syncState();

        navView = (NavigationView) findViewById(R.id.nav_view);
        updatePrayerList();
        updateDailyPrayers();

        // Listener for when an item on the prayer list is selected
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                // Creates the popup menu located at @menu/popup_menu
                Context context = navView.getContext();
                View view = menuItem.getActionView();

                MenuInflater menuInflater = new MenuInflater(context);
                MenuBuilder menuBuilder =new MenuBuilder(context);
                menuInflater.inflate(R.menu.popup_menu, menuBuilder);
                MenuPopupHelper menuPopupHelper = new MenuPopupHelper(context, menuBuilder, view);
                menuPopupHelper.setForceShowIcon(true);

                // Listener for when an item in the popup menu is selected
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder b, MenuItem m) {
                        switch (m.getItemId()) {

                            // Open EditPersonActivity (see editPersonMain())
                            case R.id.edit_popup:
                                String key = menuItem.getTitle().toString();
                                editPersonMain(key);
                                return true;

                            // Remove the name from the SharedPreferences file and remove the item from the prayer list
                            case R.id.delete_popup:
                                String name = menuItem.getTitle().toString();
                                SharedPreferences.Editor editor = prayerList.edit();
                                editor.remove(name);
                                editor.apply();
                                navView.getMenu().removeItem(menuItem.getItemId());

                                updateWeeklyPrayers();
                                updateDailyPrayers();
                                return true;

                            default:
                                return false;
                        }
                    }
                    @Override
                    public void onMenuModeChange(MenuBuilder menu) {}
                });

                // Show the popup menu
                menuPopupHelper.show();
                return false;
            }
        });
    }

    // Called every time MainActivity resumes from being paused
    // i.e. when the app is re-opened or a secondary activity finishes
    @Override
    protected void onResume() {
        super.onResume();

        updateDate();
        new updateVOTD().execute();
        updateDailyPrayers();
    }

    private void checkSharedPreferences() {
        if(!res.contains("bible_version")) {
            SharedPreferences.Editor editor = res.edit();
            editor.putString("bible_version", "ESV");
            editor.apply();
        }
        if(!weekList.contains("1")) {
            SharedPreferences.Editor editor = weekList.edit();
            for (int i = 1; i <= 7; i++) {
                editor.putString(Integer.toString(i),"");
            }
            editor.apply();
        }
    }

    // Updates the verse of the day by fetching and parsing Bible Gateway's verse of the day
    private class updateVOTD extends AsyncTask<Void, Void, Void> {
        private String verse;
        private String bible_version;
        private boolean clickable = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            bible_version = res.getString("bible_version", "ESV");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Parse the verse of the day out of the bible gateway web page
                String URL = "https://www.biblegateway.com/votd/get/?format=html&version="+bible_version;
                Document document = Jsoup.connect(URL).get();
                verse = document.select("div").text();
                verse = verse.substring(verse.indexOf("“")+1, verse.indexOf("”"));
                verse += "\n\n"+document.select("a").first().text();
                verse += " ("+bible_version+")";

                // Store the verse of the day (for later, if the user doesn't have internet)
                SharedPreferences.Editor editor = res.edit();
                editor.putString("votd", verse);
                editor.apply();

                // Set the votd as clickable
                clickable = true;
            }
            catch (IOException e){
                // Get most recent votd stored in the res SharedPreferences file
                verse = res.getString("votd", "Verse Of The Day could not load.");

                // Set the votd as not clickable
                clickable = false;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    votdLinearLayout.setClickable(clickable);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView VOTD = findViewById(R.id.verse_otd_textView);
            VOTD.setText(verse);
        }
    }

    // Called when the verse of the day layout is clicked
    // Opens a PopupMenu where the user can select a bible version, then updates the verse of the day
    public void setBibleVersion(View v) {
        try {
            // Read the file "list_of_bible_versions.txt" to an ArrayList
            Scanner scanner = new Scanner(getAssets().open("list_of_bible_versions.txt"));
            ArrayList<String> bibleVersionsList = new ArrayList<>();
            while(scanner.hasNext()) {
                bibleVersionsList.add(scanner.nextLine());
            }
            scanner.close();

            // Populate the PopupMenu with the ArrayList of bible versions
            PopupMenu popupMenu = new PopupMenu(this, v);
            int i = 0;
            for(String version : bibleVersionsList) {
                popupMenu.getMenu().add(Menu.NONE, i, i, version);
                i++;
            }

            // Set the menuItem listener, then show the PopupMenu
            // On click, it will change the version in the res SharedPreferences file and call updateVOTD()
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.bible_version_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    SharedPreferences.Editor editor = res.edit();
                    editor.putString("bible_version", item.getTitle().toString());
                    editor.apply();

                    new updateVOTD().execute();
                    return false;
                }
            });
            popupMenu.show();
        }
        catch (IOException e) {}
    }

    // Opens the drawer when the burger button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(t.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    // Opens activity_add_person and runs AddPersonActivity
    static final int ADD_PERSON_REQUEST_CODE = 1;
    public void addPersonMain(View v) {
        Intent intent = new Intent(this, AddPersonActivity.class);
        startActivityForResult(intent, ADD_PERSON_REQUEST_CODE);
    }

    // Opens activity_edit_person and runs EditPersonActivity
    static final int EDIT_PERSON_REQUEST_CODE = 2;
    public void editPersonMain(String key) {
        Intent intent = new Intent(this, EditPersonActivity.class);
        intent.putExtra("old_name", key);
        startActivityForResult(intent, EDIT_PERSON_REQUEST_CODE);
    }

    // Called by the calendar button. Opens WeekViewActivity
    public void openWeekView(View v) {
        Intent intent = new Intent(this, WeekViewActivity.class);
        startActivity(intent);
    }

    // Called when the child activities finish
    // Returned values can be found in the 'Intent data' parameter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Activity result for AddPersonActivity
        if (requestCode == ADD_PERSON_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra("name");

                String msg = "Added prayer request for " + name + ".";
                Snackbar.make(findViewById(R.id.main_linearLayout), msg,
                        Snackbar.LENGTH_LONG).show();

                updatePrayerList();
                updateWeeklyPrayers();
                updateDailyPrayers();
            }
        }

        // Activity result for EditPersonActivity
        if (requestCode == EDIT_PERSON_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the old and new names from the returned Intent
                String oldName = data.getStringExtra("old_name");
                String newName = data.getStringExtra("new_name");

                // Create a notification that the edit was successful
                String msg = "You edited your prayer request for " + newName + ".";
                Snackbar.make(findViewById(R.id.main_linearLayout), msg,
                        Snackbar.LENGTH_LONG).show();

                // Updates the prayer list in the drawer (we have to refresh it in case the user
                // edited the prayer recipient's name
                updatePrayerList();

                // Update the weekList SharedPreference (which stores who is prayed for on which day)
                // by replacing the prayer recipient's old name with their new name (even if it didn't change)
                // without changing the prayer recipient's position in weekList
                SharedPreferences.Editor editor = weekList.edit();
                // Loops through each day of the week
                for (int i = 1; i <=7; i++) {
                    String[] dailyPrayers = weekList.getString(Integer.toString(i), null).split(delimiter);
                    String[] newDailyPrayers = new String[dailyPrayers.length];

                    // Loops through each name in the day
                    int j = 0;
                    for (String name : dailyPrayers) {
                        // If the old name is found, replace it with the new name
                        if (name.equals(oldName))
                            newDailyPrayers[j] = newName;
                        // Otherwise just copy the name
                        else
                            newDailyPrayers[j] = name;
                        j++;
                    }
                    // Replace the day's prayers with newDailyPrayers
                    editor.putString(Integer.toString(i), TextUtils.join(delimiter, newDailyPrayers));
                }
                editor.apply();

                updateDailyPrayers();
            }
        }
    }

    // Updates the date that is shown on the toolbar in body_main
    private void updateDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM. d, yyyy");
        String toolbarDate = simpleDateFormat.format(new Date());
        toolbar.setTitle(toolbarDate);
    }

    // Wipes the prayer list and rebuilds it based on the data in the SharedPreferences file prayerList
    private void updatePrayerList() {
        Menu menu = navView.getMenu();

        // Create an alphabetized TreeMap of prayer recipients
        Map<String,?> map = prayerList.getAll();
        TreeMap<String,?> treeMap = new TreeMap<>(map);

        // Add a MenuItem for each name in the TreeMap
        // each MenuItem gets a unique ID - this is useful in case it is removed by the user
        menu.clear();
        int i = 0;
        for (Map.Entry<String,?> entry : treeMap.entrySet()) {
            MenuItem menuItem = menu.add(Menu.NONE, i, i, entry.getKey());
            menuItem.setActionView(new TextView(navView.getContext()));
            i++;
        }
    }

    // Updates the list of prayer requests that is shown in body_main (the main page of the app)
    private void updateDailyPrayers() {
        prayersLinearLayout.removeAllViews();

        // Get the day of the week (a number from 1-7 stored as a string)
        String weekday = Integer.toString(Integer.parseInt(new SimpleDateFormat("u").format(new Date())));

        // Get the names of the people who are to be prayed for today
        String[] dailyPrayers = weekList.getString(weekday, null).split(delimiter);

        for (String name : dailyPrayers) {
            // Each prayer request consists of a LinearLayout box, and two EditTexts (one for the name
            // and one for the prayer description)
            View box = getLayoutInflater().inflate(R.layout.merge_prayer_box, null);
            EditText name_box = (EditText) box.findViewById(R.id.name_prayer_box);
            name_box.setText(name);

            // Check the dailyPrayers array is not empty (i.e. nobody is on the prayer list)
            if (!TextUtils.join(null, dailyPrayers).isEmpty()) {
                String desc = prayerList.getString(name, null);
                EditText desc_box = (EditText) box.findViewById(R.id.desc_prayer_box);
                // If the prayer request has a description
                if (!desc.isEmpty())
                    desc_box.setText(desc);
                // Otherwise(i.e. just a name), delete the description EditText and adjust the name EditText
                else {
                    ((ViewManager) desc_box.getParent()).removeView(desc_box);
                    name_box.setBackgroundResource(android.R.color.transparent);

                    // Redo the padding in the view (otherwise the name isn't centered right)
                    int padding_in_dp = 8;  // 6 dps
                    final float scale = getResources().getDisplayMetrics().density;
                    int p = (int) (padding_in_dp * scale + 0.5f);
                    name_box.setPadding(p,p,p,p);
                }
            }
            // If the prayer list is empty, create a message telling the user that it is empty
            else {
                name_box.setText("Hmm...It seems that there's nobody here. Go to your prayer list " +
                        "(found in the left drawer) and add someone to it!");
                name_box.setBackgroundResource(android.R.color.transparent);
                name_box.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                name_box.setTextColor(getResources().getColor(android.R.color.darker_gray));
                name_box.setTypeface(null, Typeface.ITALIC);
                box.setBackgroundResource(android.R.color.transparent);
            }
            prayersLinearLayout.addView(box);
        }
    }

    // Updates the weekList SharedPreference (usually called when a prayer request is added or deleted)
    private void updateWeeklyPrayers() {
        // Create an alphabetized TreeMap of prayer recipients
        Map<String,?> map = prayerList.getAll();
        TreeMap<String,?> treeMap = new TreeMap<>(map);

        // Get all the names from the treeMap and put them in a string array namesArray
        String[] namesArray = new String[treeMap.size()];
        int i = 0;
        for(Map.Entry<String,?> entry : treeMap.entrySet()) {
            namesArray[i] = entry.getKey();
            i++;
        }

        // Put namesArray through the algorithm PrayerAlgorithm.weekPrayers (found in the WarRoomRes folder)
        // Outputs a two-dimensional string array - the array is 7 elements long (one for each day of
        // the week) and each element is an array of names (those to be prayed for that day)
        String[][] weeklyPrayers = PrayerAlgorithm.weeklyPrayers(namesArray);

        // Store the two-dimensional array into the weekList sharedPreference
        // In weekList, each key is a number (for the day of the week) and its value is a joined
        // string of names
        SharedPreferences.Editor editor = weekList.edit();
        i = 1;
        for(String[] dailyPrayers : weeklyPrayers) {
            String key = Integer.toString(i);
            String value = TextUtils.join(delimiter, dailyPrayers);
            editor.putString(key, value);
            i++;
        }
        editor.apply();
    }
}