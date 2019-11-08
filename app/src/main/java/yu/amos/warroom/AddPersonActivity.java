package yu.amos.warroom;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;

public class AddPersonActivity extends AppCompatActivity {

    private EditText editTextName, editTextDesc;
    private Toolbar toolbar;
    private ImageButton bottomButton;
    private SharedPreferences prayerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        // Get the SharedPreference that contains the prayer list
        prayerList = getSharedPreferences("prayer_list", 0);

        // Configure the toolbar (set the title and define the function of the 'back' button)
        toolbar = (Toolbar)findViewById(R.id.merge_toolbar);
        toolbar.setTitle("New Prayer Recipient");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set the icon and the function of the button on the bottom of the screen
        bottomButton = findViewById(R.id.bottom_imageButton);
        bottomButton.setImageResource(R.drawable.ic_person_add);
        bottomButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson(v);
            }
        });
    }

    // Function to add the prayer recipient to the prayer list when bottomButton is pressed
    public void addPerson(View v) {
        // Get the name and the prayer request
        editTextName = (EditText) findViewById(R.id.name_editText);
        editTextDesc = (EditText) findViewById(R.id.desc_editText);
        String name = editTextName.getText().toString();
        String desc = editTextDesc.getText().toString();

        // Check that the name is not empty
        if(!name.equals("")) {
            // Check that the name is not already in the SharedPreference prayerList
            if(!prayerList.contains(name)) {
                // Add the name to the prayer list
                SharedPreferences.Editor editor = prayerList.edit();
                editor.putString(name, desc);
                editor.commit();

                // Return the Intent with the name of the new prayer recipient so that
                // MainActivity can display a notification to confirm the action
                Intent returnIntent = new Intent(this, MainActivity.class);
                returnIntent.putExtra("name", name);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            // Displays a notification if the name is already in prayerList
            else {
                String msg = "There is already a prayer recipient named "+name+".";
                Snackbar.make(findViewById(R.id.linearlayout_add_person), msg,
                        Snackbar.LENGTH_LONG).show();
            }
        }
        // Displays a notification if the name is empty
        else {
            String msg = "Please give your prayer recipient a name.";
            Snackbar.make(findViewById(R.id.linearlayout_add_person), msg,
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
