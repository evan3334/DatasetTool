package pw.evan.datasettool;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pw.evan.datasettool.dataset.Dataset;

public class DatasetEditActivity extends AppCompatActivity {

    public static final String EXTRA_DATASET_FILENAME = "datasetFilename";

    private static final String KEY_DATASET_FILENAME = "datasetFilename";
    private static final String KEY_DATASET = "dataset";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.withRecordSeparator('\n').withHeader();

    private Dataset dataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_edit);
        String datasetFilename;
        if (savedInstanceState != null) {
            datasetFilename = savedInstanceState.getString(KEY_DATASET_FILENAME);
            dataset = savedInstanceState.getParcelable(KEY_DATASET);
        } else {
            datasetFilename = getIntent().getStringExtra(EXTRA_DATASET_FILENAME);
            if (datasetFilename == null) {
                finish();
            }
            dataset = loadFromFile(datasetFilename);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(datasetFilename);
        }

        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        DatasetEntryListAdapter adapter = new DatasetEntryListAdapter(dataset);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_DATASET,dataset);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dataset_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addEntry:
                captureImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Uri getTempImageUri() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getExternalCacheDir(), "temp_pics");
        boolean good;
        if (!storageDir.exists()) {
            good = storageDir.mkdirs();
        } else {
            good = true;
        }
        if (good) {
            File image;
            try {
                image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",   /* suffix */
                        storageDir      /* directory */
                );
                return FileProvider.getUriForFile(this, "pw.evan.datasettool.fileprovider", image);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private File getCSV(String datasetName) {
        File directory = new File(getExternalFilesDir(null), datasetName);
        if (directory.exists() && directory.isDirectory()) {
            File csvFile = new File(directory, datasetName + ".csv");
            if (csvFile.exists()) {
                return csvFile;
            }
        }
        return null;
    }

    private Dataset loadFromFile(String datasetName) {
        File csvFile = getCSV(datasetName);
        if (csvFile != null) {
            try {
                CSVParser parser = CSVParser.parse(csvFile, Charset.forName("UTF-8"), FORMAT);
                Dataset dataset = new Dataset(datasetName, parser);
                parser.close();
                return dataset;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }


    //private void

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri imageUri = getTempImageUri();
            if (imageUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
}
