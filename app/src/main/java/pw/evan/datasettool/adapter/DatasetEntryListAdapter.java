package pw.evan.datasettool.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import pw.evan.datasettool.R;
import pw.evan.datasettool.activity.BoundingBoxSelectActivity;
import pw.evan.datasettool.dataset.Dataset;
import pw.evan.datasettool.dataset.Dataset.Entry;

public class DatasetEntryListAdapter extends RecyclerView.Adapter<DatasetEntryListAdapter.DatasetEntryViewHolder> {
    private Dataset dataset;
    private AppCompatActivity activity;
    private int requestCode;
    private HashMap<String, Bitmap> thumbnails;

    public DatasetEntryListAdapter(@NonNull Dataset dataset, int requestCode, @NonNull AppCompatActivity activity) {
        this.dataset = dataset;
        this.thumbnails = new HashMap<>();
        this.activity = activity;
        this.requestCode = requestCode;
    }

    public void updateDataset(@NonNull Dataset dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DatasetEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dataset_entry_list_element, parent, false);
        return new DatasetEntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DatasetEntryViewHolder holder, int position) {
        View root = holder.entryView;
        Entry entry = dataset.getEntries().get(position);
        ((TextView) root.findViewById(R.id.index_display)).setText(String.valueOf(position));
        ((TextView) root.findViewById(R.id.filename_display)).setText(entry.getFilename());
        Context c = root.getContext();
        ((TextView) root.findViewById(R.id.class_display))
                .setText(c.getString(R.string.object_class_at, entry.getClassName()));
        Rect box = entry.getBoundingBox();
        //xmin
        ((TextView) root.findViewById(R.id.xmin_display))
                .setText(c.getString(R.string.xmin_format, box.left));
        //xmax
        ((TextView) root.findViewById(R.id.xmax_display))
                .setText(c.getString(R.string.xmax_format, box.right));
        //ymin
        ((TextView) root.findViewById(R.id.ymin_display))
                .setText(c.getString(R.string.ymin_format, box.top));
        //ymax
        ((TextView) root.findViewById(R.id.ymax_display))
                .setText(c.getString(R.string.ymax_format, box.bottom));
        //dimensions (w x h)
        ((TextView) root.findViewById(R.id.size_display))
                .setText(c.getString(R.string.size_display_format, entry.getWidth(), entry.getHeight()));

        /*Bitmap image = entry.loadImage(c);
        if (image != null) {
            ((ImageView) root.findViewById(R.id.thumbnail)).setImageBitmap(image);
        }*/

        Bitmap thumbnail = thumbnails.get(entry.getFilename());
        if (thumbnail == null) {
            Log.d("retrieve", "couldn't retrieve thumbnail " + entry.getFilename());
            LoadThumbnailTask task = new LoadThumbnailTask(this);
            activity.runOnUiThread(() -> task.execute(entry));
        } else {
            Log.d("retrieve", "successfully retrieved thumbnail " + entry.getFilename());
            ((ImageView) root.findViewById(R.id.thumbnail)).setImageBitmap(thumbnail);
        }

        ImageButton deleteEntryButton = root.findViewById(R.id.delete_button);
        deleteEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteHandler(c, entry);
            }
        });

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickHandler(c, entry);
            }
        });
    }

    private void clickHandler(Context context, Entry entry) {
        Intent i = new Intent(context, BoundingBoxSelectActivity.class);
        i.putExtra(BoundingBoxSelectActivity.EXTRA_DATASET_ENTRY, entry);
        i.putExtra(BoundingBoxSelectActivity.EXTRA_ENTRY_INDEX, dataset.getIndex(entry));
        activity.startActivityForResult(i, requestCode);
    }

    private void deleteHandler(Context context, Entry entry) {
        int index = dataset.getIndex(entry);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_entry_title);
        builder.setMessage(R.string.warning_action_irreversible);
        builder.setPositiveButton(R.string.button_text_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File imageFile = new File(entry.getImageURI().getPath());
                if (!imageFile.delete()) {
                    Toast.makeText(context, R.string.image_file_not_deleted_properly, Toast.LENGTH_SHORT).show();
                }
                dataset.getEntries().remove(index);
                dataset.writeToFile(context);
                notifyItemRemoved(index);
                notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    @Override
    public int getItemCount() {
        return dataset.getEntries().size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class DatasetEntryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View entryView;

        public DatasetEntryViewHolder(View v) {
            super(v);
            entryView = v;
        }
    }

    private static class LoadThumbnailTask extends AsyncTask<Entry, Void, Bitmap> {
        private int thumbnailSize;
        public static final int DEFAULT_THUMBNAIL_SIZE = 256;
        private Entry entry;
        DatasetEntryListAdapter adapter;

        public LoadThumbnailTask(DatasetEntryListAdapter adapter, int thumbnailSize) {
            super();
            this.adapter = adapter;
            if (thumbnailSize <= 0) {
                throw new IllegalArgumentException("Thumbnail size may not be 0 or negative!");
            }
            this.thumbnailSize = thumbnailSize;
        }

        public LoadThumbnailTask(DatasetEntryListAdapter adapter) {
            this(adapter, DEFAULT_THUMBNAIL_SIZE);
        }

        @Override
        protected Bitmap doInBackground(@NonNull Entry... entries) {
            if (entries.length > 0) {
                if (entries[0] != null) {
                    entry = entries[0];
                } else {
                    throw new IllegalArgumentException("Entry must not be null!");
                }
            } else {
                throw new IllegalArgumentException("Must have at least one entry to operate on!");
            }

            Log.d("thumbnail", "Loading thumbnail " + entry.getFilename());
            Bitmap thumbnail;

            File thumbFile = getThumbnailFile(entry);
            if (thumbFile != null) {
                if (thumbFile.exists()) {
                    thumbnail = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
                } else {
                    thumbnail = createThumbnail();
                    saveThumbnail(thumbnail);
                }
            } else {
                Log.e("background", "Returning null!");
                return null;
            }

            return thumbnail;
        }

        private Bitmap createThumbnail() {
            Log.d("thumbnail", "create new thumbnail " + entry.getFilename());
            Uri imageURI = entry.getImageURI();
            Bitmap original = BitmapFactory.decodeFile(imageURI.getPath());
            int scaleWidth, scaleHeight;
            if (original.getWidth() > original.getHeight()) {
                scaleWidth = DEFAULT_THUMBNAIL_SIZE;
                scaleHeight = (int) (DEFAULT_THUMBNAIL_SIZE * ((double) original.getHeight() / (double) original.getWidth()));
            } else {
                scaleWidth = (int) (DEFAULT_THUMBNAIL_SIZE * ((double) original.getWidth() / (double) original.getHeight()));
                scaleHeight = DEFAULT_THUMBNAIL_SIZE;
            }
            Log.d("thumbnail", "scaling - w: " + scaleWidth + " h: " + scaleHeight);
            Bitmap scaled = Bitmap.createScaledBitmap(original, scaleWidth, scaleHeight, false);
            original.recycle();
            return scaled;
        }

        private void saveThumbnail(Bitmap bitmap) {
            File thumbFile = getThumbnailFile(entry);
            if (thumbFile != null) {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(thumbFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        private File getThumbnailFile(Entry entry) {
            String filename = entry.getFilename();
            File thumbDir = getThumbnailDirectory();
            if (thumbDir != null) {
                return new File(thumbDir, filename);
            } else {
                return null;
            }
        }

        private File getThumbnailDirectory() {
            String datasetName = adapter.dataset.getName();
            File root = adapter.activity.getExternalCacheDir();
            File thumbnailDir = new File(root, "thumbnails");
            File datasetThumbnailDir = new File(thumbnailDir, datasetName);
            if (datasetThumbnailDir.exists() || datasetThumbnailDir.mkdirs()) {
                return datasetThumbnailDir;
            } else {
                Log.e("background", "mkdirs() failed!");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            adapter.thumbnails.put(entry.getFilename(), bitmap);
            Log.d("hashmap", "entry for " + entry.getFilename() + ": " + adapter.thumbnails.get(entry.getFilename()));
            int index = adapter.dataset.getIndex(entry);
            Log.d("thumbnail", "notify changed for index " + index);
            adapter.notifyItemChanged(index);
        }
    }
}
