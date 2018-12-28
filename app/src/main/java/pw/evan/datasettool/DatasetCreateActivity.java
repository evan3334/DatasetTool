package pw.evan.datasettool;

import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class DatasetCreateActivity extends AppCompatActivity {

    public static final String KEY_STATUS_VISIBILITY = "statusVisibility";
    public static final String KEY_CURRENT_INPUT = "currentInput";
    public static final String RESERVED_CHARS_REGEX = ".*[|?*<\":>+\\[\\]/'\\n].*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_create);
        final EditText nameInput = findViewById(R.id.nameInput);
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            nameInput.setText(savedInstanceState.getString(KEY_CURRENT_INPUT, ""));
            statusView.setVisibility(savedInstanceState.getInt(KEY_STATUS_VISIBILITY, View.INVISIBLE));

            if (statusView.getVisibility() == View.VISIBLE) {
                checkInput(nameInput.getText());
            }
        }

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ignore
            }

            @Override
            public void afterTextChanged(Editable newText) {
                checkInput(newText);
            }
        });

        nameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Button nextButton = findViewById(R.id.nextButton);
                    if (nextButton.isEnabled()) {
                        String text = formatFilename(nameInput.getText().toString());
                        next(text);
                    }
                    handled = true;
                }
                return handled;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameInput = findViewById(R.id.nameInput);
                String text = formatFilename(nameInput.getText().toString());
                next(text);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        EditText nameInput = findViewById(R.id.nameInput);
        TextView statusView = findViewById(R.id.statusView);
        outState.putInt(KEY_STATUS_VISIBILITY, statusView.getVisibility());
        outState.putString(KEY_CURRENT_INPUT, nameInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void checkInput(Editable s) {
        String text = formatFilename(s.toString());
        if (text.length() == 0) {
            youMustEnterSomething();
        } else if (containsReservedChars(text)) {
            invalidCharacters();
        } else if (doesFileExist(text)) {
            nameExists();
        } else {
            willBeSavedTo(text);
        }
    }


    private void youMustEnterSomething() {
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
        statusView.setVisibility(View.VISIBLE);
        statusView.setTextColor(Color.RED);
        statusView.setText(R.string.you_must_enter_name);
        nextButton.setEnabled(false);
    }

    private void willBeSavedTo(String text) {
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
        statusView.setVisibility(View.VISIBLE);
        statusView.setTextColor(Color.BLACK);
        statusView.setText(getString(R.string.will_be_saved_to, formatFilename(text)));
        nextButton.setEnabled(true);
    }

    private void invalidCharacters() {
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
        statusView.setVisibility(View.VISIBLE);
        statusView.setTextColor(Color.RED);
        statusView.setText(R.string.invalid_characters_used);
        nextButton.setEnabled(false);
    }

    private void nameExists() {
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
        statusView.setVisibility(View.VISIBLE);
        statusView.setTextColor(Color.RED);
        statusView.setText(R.string.dataset_already_exists);
        nextButton.setEnabled(false);
    }

    private boolean doesFileExist(String name) {
        File file = new File(getExternalFilesDir(null), name);
        return file.exists();
    }

    private String formatFilename(String input) {
        String output = input.toLowerCase();
        output = output.trim();
        output = output.replaceAll(" ", "_");
        return output;
    }

    private boolean containsReservedChars(String input) {
        return input.matches(RESERVED_CHARS_REGEX);
    }

    private void errFilesExist(){
        Toast.makeText(this, R.string.err_files_exist, Toast.LENGTH_SHORT).show();
    }

    private void errException(Exception e){
        Toast.makeText(this, getString(R.string.err_unexpected_exception, e.toString()), Toast.LENGTH_SHORT).show();
    }

    private void next(String name) {
        File directory = new File(getExternalFilesDir(null), name);
        if(directory.mkdir()){
            File csv = new File(directory, name+".csv");
            try {
                if (csv.createNewFile()){
                    Intent i = new Intent(this, DatasetEditActivity.class);
                    i.putExtra(DatasetEditActivity.EXTRA_DATASET_FILENAME, name);
                    startActivity(i);
                    finish();
                } else {
                    errFilesExist();
                }
            } catch (IOException e) {
                e.printStackTrace();
                errException(e);
            }
        } else {
            errFilesExist();
        }
    }

}
