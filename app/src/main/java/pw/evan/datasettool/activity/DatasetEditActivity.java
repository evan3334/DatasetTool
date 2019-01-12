package pw.evan.datasettool.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pw.evan.datasettool.R;
import pw.evan.datasettool.adapter.DatasetEntryListAdapter;
import pw.evan.datasettool.dataset.Dataset;

public class DatasetEditActivity extends AppCompatActivity {

    public static final String EXTRA_DATASET_FILENAME = "datasetFilename";

    private static final String KEY_DATASET_FILENAME = "datasetFilename";
    private static final String KEY_DATASET = "dataset";
    private static final String KEY_CACHED_FILENAMES = "cachedFilenames";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_CROP = 2;

    private static final int MAX_IMAGE_DIMENSION = 1280;

    private Dataset dataset;

    private HashMap<Integer, String> cachedFilenames;

    private DatasetEntryListAdapter adapter;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_edit);
        String datasetFilename;
        if (savedInstanceState != null) {
            datasetFilename = savedInstanceState.getString(KEY_DATASET_FILENAME);
            dataset = savedInstanceState.getParcelable(KEY_DATASET);
            cachedFilenames = (HashMap<Integer, String>) savedInstanceState.getSerializable(KEY_CACHED_FILENAMES);
        } else {
            datasetFilename = getIntent().getStringExtra(EXTRA_DATASET_FILENAME);
            if (datasetFilename == null) {
                finish();
            } else {
                dataset = Dataset.loadFromFile(this, datasetFilename);
                cachedFilenames = new HashMap<>();
            }
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
        adapter = new DatasetEntryListAdapter(dataset, REQUEST_IMAGE_CROP, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (isCaptureRequestCode(requestCode) && resultCode == RESULT_OK) {
            Log.d("activityResult", "capture");
            String filename = cachedFilenames.get(requestCode);
            File cachedImage = new File(filename);
            Log.d("current dataset", dataset.toString());
            int index = dataset.getEntries().size();
            File datasetDir = Dataset.getDatasetDirectory(this, dataset.getName());
            String newImageFilename = index + ".jpg";
            File newImage = new File(datasetDir, newImageFilename);
            while(newImage.exists()){
                int i = 0;
                newImageFilename = index + "-"+i+".jpg";
                newImage = new File(datasetDir, newImageFilename);
            }

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Copying and scaling image. This should only take a moment.");
            progressDialog.show();

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(cachedImage.getAbsolutePath());
                int scaledWidth, scaledHeight;
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                //scale cached image down to 720p if necessary, increases performance later
                if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
                    if (width > height) {
                        scaledWidth = MAX_IMAGE_DIMENSION;
                        scaledHeight = (int) (MAX_IMAGE_DIMENSION * ((double) height / (double) width));
                    } else {
                        scaledHeight = MAX_IMAGE_DIMENSION;
                        scaledWidth = (int) (MAX_IMAGE_DIMENSION * ((double) width / (double) height));
                    }

                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap,scaledWidth,scaledHeight,false);
                    bitmap.recycle();
                    bitmap = scaled;
                }

                //save cached image to a real image file
                FileOutputStream outputStream = new FileOutputStream(newImage);
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
                bitmap.recycle();

                //remove the request code
                cachedFilenames.remove(requestCode);

                //get rid of the cached image
                if (!cachedImage.delete()) {
                    Toast.makeText(this, R.string.cached_image_not_deleted, Toast.LENGTH_SHORT).show();
                }

                //create a new dataset entry
                String objectClass = "TennisBall";
                Dataset.Entry newEntry = Dataset.createEntry(newImage, objectClass);
                Log.d("new entry", newEntry.toString());
                dataset.getEntries().add(newEntry);
                adapter.updateDataset(dataset);
                Log.d("dataset updated", dataset.toString());

                //save the dataset
                saveDataset();

                //start the crop selection activity
                Intent i = new Intent(this, BoundingBoxSelectActivity.class);
                i.putExtra(BoundingBoxSelectActivity.EXTRA_DATASET_ENTRY, newEntry);
                i.putExtra(BoundingBoxSelectActivity.EXTRA_ENTRY_INDEX, index);
                progressDialog.dismiss();
                startActivityForResult(i, REQUEST_IMAGE_CROP);

            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        } else if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK) {
            if (data != null) {
                Log.d("activityResult", "crop");
                int index = data.getIntExtra(BoundingBoxSelectActivity.EXTRA_ENTRY_INDEX, -1);
                Dataset.Entry entry = data.getParcelableExtra(BoundingBoxSelectActivity.EXTRA_DATASET_ENTRY);
                Log.d("entry", entry != null ? entry.toString() : "null");
                Log.d("index", String.valueOf(index));
                Log.d("entry==null", String.valueOf(entry == null));
                if (index >= 0 && entry != null) {
                    dataset.getEntries().set(index, entry);
                    adapter.updateDataset(dataset);
                    Log.d("dataset updated", dataset.toString());
                    saveDataset();
                }
            }
        }
    }

    private void saveDataset() {
        dataset.writeToFile(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_DATASET, dataset);
        outState.putSerializable(KEY_CACHED_FILENAMES, cachedFilenames);
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

    private Uri getTempImageUri(File tempImageFile) {
        if (tempImageFile != null && tempImageFile.exists()) {
            return FileProvider.getUriForFile(this, "pw.evan.datasettool.fileprovider", tempImageFile);
        } else {
            return null;
        }
    }

    private File getTempImageFile() {
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
                if (image.exists()) {
                    return image;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private int createCaptureRequestCode() {
        Random r = new Random();
        int random = r.nextInt(256);
        int code = REQUEST_IMAGE_CAPTURE << 8;
        code += random;
        return code;
    }

    private boolean isCaptureRequestCode(int code) {
        code = code >> 8;
        return code == REQUEST_IMAGE_CAPTURE;
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = getTempImageFile();
            if (imageFile != null) {
                Uri imageUri = getTempImageUri(imageFile);
                if (imageUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    int code = createCaptureRequestCode();
                    cachedFilenames.put(code, imageFile.getAbsolutePath());
                    startActivityForResult(takePictureIntent, code);
                }
            }
        }
    }
}
