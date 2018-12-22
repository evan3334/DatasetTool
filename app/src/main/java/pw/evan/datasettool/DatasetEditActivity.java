package pw.evan.datasettool;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class DatasetEditActivity extends AppCompatActivity {

    public static final String EXTRA_DATASET_FILENAME = "datasetFilename";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_edit);
        String datasetFilename = getIntent().getStringExtra(EXTRA_DATASET_FILENAME);
        if(datasetFilename == null){
            finish();
        }
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(datasetFilename);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.addEntry:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
