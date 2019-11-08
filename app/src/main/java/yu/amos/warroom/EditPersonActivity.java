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

public class EditPersonActivity extends AppCompatActivity {

    private Intent intent;
    private SharedPreferences prayerList;
    private EditText editTextName, editTextDesc;
    private Toolbar toolbar;
    private ImageButton bottomButton;
    private String oldName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);

        // Get the name of the prayer recipient from the intent
        intent = getIntent();
        oldName = intent.getStringExtra("old_name");
        prayerList = getSharedPreferences("prayer_list", 0);
        editTextName = findViewById(R.id.name_editText);
        editTextDesc = findViewById(R.id.desc_editText);
        editTextName.setText(oldName);
        // Use the name to add the corresponding prayer request to the editText
        editTextDesc.setText(prayerList.getString(oldName, null));

        // Configure the toolbar (set the title and define the function of the 'back' button)
        toolbar = (Toolbar)findViewById(R.id.merge_toolbar);
        toolbar.setTitle(oldName+"'s Prayer Request");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set the icon and the function of the button on the bottom of the screen
        bottomButton = findViewById(R.id.bottom_imageButton);
        bottomButton.setImageResource(R.drawable.ic_check);
        bottomButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPerson(v);
            }
        });
    }

    // Function to edit the prayer recipient when bottomButton is pressed
    public void editPerson(View v) {
        // Get the name and the prayer request
        String newName = editTextName.getText().toString();
        String newDesc = editTextDesc.getText().toString();

        // Check that the name is not empty
        if(!newName.equals("")) {
            // Check that the new name is not already in the SharedPreference prayerList
            if(!prayerList.contains(newName) || newName.equals(oldName)) {
                // Remove the old prayer request and replace it with the new one
                SharedPreferences.Editor editor = prayerList.edit();
                editor.remove(oldName);
                editor.commit();
                editor = prayerList.edit();
                editor.putString(newName, newDesc);
                editor.commit();

                // Return the Intent with the new name of the new prayer recipient so that
                // MainActivity can display a notification to confirm the action
                Intent returnIntent = new Intent(this, MainActivity.class);
                returnIntent.putExtra("old_name", oldName);
                returnIntent.putExtra("new_name", newName);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            // Displays a notification if the new name is already in prayerList
            else {
                String msg = "Another prayer recipient is already named "+newName+".";
                Snackbar.make(findViewById(R.id.linearlayout_edit_person), msg,
                        Snackbar.LENGTH_LONG).show();
            }
        }
        // Displays a notification if the name is empty
        else {
            String msg = "You may not remove the prayer recipient's name.";
            Snackbar.make(findViewById(R.id.linearlayout_edit_person), msg,
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
