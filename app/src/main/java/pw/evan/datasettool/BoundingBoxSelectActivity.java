package pw.evan.datasettool;

import android.content.Intent;
import android.os.Bundle;

import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.appcompat.app.AppCompatActivity;
import pw.evan.datasettool.dataset.Dataset;

public class BoundingBoxSelectActivity extends AppCompatActivity {
    public static final String EXTRA_DATASET_ENTRY = "datasetEntry";

    private Dataset.Entry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounding_box_select);

        if(savedInstanceState!=null){
            entry = savedInstanceState.getParcelable(EXTRA_DATASET_ENTRY);
        } else {
            Intent intent = getIntent();
            if(intent!=null){
                entry = intent.getParcelableExtra(EXTRA_DATASET_ENTRY);
                if(entry == null){
                    finish();
                }
            } else {
                finish();
            }
        }

        CropImageView cropImageView = findViewById(R.id.cropView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}
