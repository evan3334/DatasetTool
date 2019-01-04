package pw.evan.datasettool.activity;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import pw.evan.datasettool.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class DatasetChooseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_choose);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dataset_choose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private LinearLayout getListContainer() {
        return findViewById(R.id.dataset_list_container);
    }

    private void clearAllDatasetsFromList() {
        getListContainer().removeAllViews();
    }

    private void addDatasetToList(final String name) {
        LayoutInflater inflater = getLayoutInflater();
        View listEntry = inflater.inflate(R.layout.dataset_list_element, null);
        TextView datasetTitle = listEntry.findViewById(R.id.dataset_title);
        datasetTitle.setText(name);
        listEntry.setOnClickListener(view -> onClickItem(name));
        getListContainer().addView(listEntry);
    }

    private void refresh() {
        clearAllDatasetsFromList();
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            for (File current : dir.listFiles()) {
                if (current.isDirectory()) {
                    String name = current.getName();
                    File csv = new File(current, name + ".csv");
                    if (csv.exists()) {
                        //valid dataset (ig)
                        addDatasetToList(name);
                    }
                }
            }
        }
    }

    private void onClickItem(String name) {
        Intent i = new Intent(getApplicationContext(), DatasetEditActivity.class);
        i.putExtra(DatasetEditActivity.EXTRA_DATASET_FILENAME, name);
        startActivity(i);
        finish();
    }

}
