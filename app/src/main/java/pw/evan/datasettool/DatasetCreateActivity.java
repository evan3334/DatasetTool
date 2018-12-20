package pw.evan.datasettool;

import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;

import java.io.File;

public class DatasetCreateActivity extends AppCompatActivity {

    public static final String KEY_STATUS_VISIBILITY = "statusVisibility";
    public static final String KEY_CURRENT_INPUT = "currentInput";
    public static final String KEY_BUTTON_ENABLED = "buttonEnabled";
    private String reservedChars = "|\\?*<\":>+[]/'";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_create);
        EditText nameInput = findViewById(R.id.nameInput);
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        EditText nameInput = findViewById(R.id.nameInput);
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
        outState.putInt(KEY_STATUS_VISIBILITY, statusView.getVisibility());
        outState.putString(KEY_CURRENT_INPUT, nameInput.getText().toString());
        outState.putBoolean(KEY_BUTTON_ENABLED, nextButton.isEnabled());
        super.onSaveInstanceState(outState);
    }

    private void checkInput(Editable s) {
        String text = s.toString();
        if (text.length() == 0) {
            youMustEnterSomething();
        } else if (containsReservedChars(text)) {
            invalidCharacters();
        } else if (doesFileExist(formatFilename(text))) {
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
    }

    private void nameExists() {
        TextView statusView = findViewById(R.id.statusView);
        Button nextButton = findViewById(R.id.nextButton);
    }

    private boolean doesFileExist(String name) {
        File file = new File(getExternalFilesDir(null), name);
        return file.exists();
    }

    private String formatFilename(String input) {
        if (containsReservedChars(input)) {
            return null;
        }
        String output = input.toLowerCase();
        output = output.trim();
        output = output.replace(" ", "_");
        return output;
    }

    private boolean containsReservedChars(String input) {
        for (String current : reservedChars.split("")) {
            if (input.contains(current)) {
                return true;
            }
        }
        return false;
    }

}
