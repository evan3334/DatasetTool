package pw.evan.datasettool.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.theartofdev.edmodo.cropper.CropImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import pw.evan.datasettool.R;
import pw.evan.datasettool.dataset.Dataset;
import pw.evan.datasettool.dataset.Dataset.Entry;

public class BoundingBoxSelectActivity extends AppCompatActivity {
    public static final String EXTRA_DATASET_ENTRY = "datasetEntry";
    public static final String EXTRA_ENTRY_INDEX = "entryIndex";

    public static final String RESULT_ACTION = "pw.evan.datasettool.BoundingBoxResultAction";
    private Entry entry;
    private int index;

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounding_box_select);

        if(savedInstanceState!=null){
            //we are returning from a configuration change
            //the cropimageview should take care of loading the image itself
            entry = savedInstanceState.getParcelable(EXTRA_DATASET_ENTRY);
            index = savedInstanceState.getInt(EXTRA_ENTRY_INDEX);
        } else {
            //the activity is being started for the first time
            //we must get the entry and load the bitmap into the cropimageview
            Intent intent = getIntent();
            if(intent!=null){
                entry = intent.getParcelableExtra(EXTRA_DATASET_ENTRY);
                index = intent.getIntExtra(EXTRA_ENTRY_INDEX, -1);
                if(entry == null){
                    finish();
                } else {
                    Bitmap bitmap = entry.loadImage(this);
                    if(bitmap!=null) {
                        cropImageView = findViewById(R.id.cropView);
                        cropImageView.setImageUriAsync(entry.getImageURI());
                        Log.d("set image uri", entry.getImageURI().toString());
                        cropImageView.setMinCropResultSize(1,1);
                        cropImageView.setOnSetImageUriCompleteListener(new CropImageView.OnSetImageUriCompleteListener() {
                            @Override
                            public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
                                if(error == null){
                                    Log.d("setting crop rect", entry.getBoundingBox().toShortString());
                                    cropImageView.setCropRect(entry.getBoundingBox());
                                    Log.d("set crop rect", cropImageView.getCropWindowRect().toShortString());
                                }
                            }
                        });

                    } else {
                        finish();
                    }
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bounding_box_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.done_button){
            completeCrop();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void completeCrop(){
        Rect cropped = cropImageView.getCropRect();
        Log.d("cropRect", cropped.flattenToString());
        entry.setBoundingBox(cropped);

        Intent result = new Intent(RESULT_ACTION,null);
        result.putExtra(EXTRA_DATASET_ENTRY, entry);
        result.putExtra(EXTRA_ENTRY_INDEX, index);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}
