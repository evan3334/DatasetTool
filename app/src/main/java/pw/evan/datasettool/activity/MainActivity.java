package pw.evan.datasettool.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import pw.evan.datasettool.R;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button createNewButton = findViewById(R.id.create_new);
        Button useExistingButton = findViewById(R.id.use_existing);
        createNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DatasetCreateActivity.class);
                startActivity(i);
            }
        });
        useExistingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DatasetChooseActivity.class);
                startActivity(i);
            }
        });


    }


}
