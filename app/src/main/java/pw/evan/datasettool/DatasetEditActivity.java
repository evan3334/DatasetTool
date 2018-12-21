package pw.evan.datasettool;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DatasetEditActivity extends AppCompatActivity {

    public static final String EXTRA_DATASET_FILENAME = "datasetFilename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_edit);
        String datasetFilename = getIntent().getStringExtra(EXTRA_DATASET_FILENAME);
        if(datasetFilename == null){
            finish();
        }
    }
}
