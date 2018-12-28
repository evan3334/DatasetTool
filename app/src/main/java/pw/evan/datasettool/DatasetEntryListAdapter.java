package pw.evan.datasettool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

import pw.evan.datasettool.dataset.Dataset;
import pw.evan.datasettool.dataset.Dataset.Entry;

public class DatasetEntryListAdapter extends RecyclerView.Adapter<DatasetEntryListAdapter.DatasetEntryViewHolder> {
    private Dataset dataset;
    //TODO use this later
    private HashMap<Entry, Bitmap> thumbnails;

    public DatasetEntryListAdapter(@NonNull Dataset dataset) {
        this.dataset = dataset;
        this.thumbnails = new HashMap<>();
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

        Bitmap image = loadImage(c, entry.getFilename());
        if(image != null) {
            ((ImageView) root.findViewById(R.id.thumbnail)).setImageBitmap(image);
        }
    }

    private Bitmap loadImage(Context context, String filename){
        File directory = new File(context.getExternalFilesDir(null),dataset.getName());
        if(directory.isDirectory() && directory.exists()){
            File image = new File(directory,filename);
            if(image.exists()){
                return loadBitmap(image);
            }
        }
        return null;
    }

    private Bitmap loadBitmap(File file){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
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
}
