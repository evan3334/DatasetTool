package pw.evan.datasettool.dataset;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import pw.evan.datasettool.DatasetEntryListAdapter;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Dataset implements Parcelable {
    private static final String CSV_KEY_INDEX = "index";
    private static final String CSV_KEY_FILENAME = "filename";
    private static final String CSV_KEY_WIDTH = "width";
    private static final String CSV_KEY_HEIGHT = "height";
    private static final String CSV_KEY_CLASS = "class";
    private static final String CSV_KEY_XMAX = "xmax";
    private static final String CSV_KEY_XMIN = "xmin";
    private static final String CSV_KEY_YMAX = "ymax";
    private static final String CSV_KEY_YMIN = "ymin";

    private static final ArrayList<String> CSV_KEYS = new ArrayList<>(Arrays.asList(
            CSV_KEY_INDEX,
            CSV_KEY_FILENAME,
            CSV_KEY_WIDTH,
            CSV_KEY_HEIGHT,
            CSV_KEY_CLASS,
            CSV_KEY_XMIN,
            CSV_KEY_XMAX,
            CSV_KEY_YMIN,
            CSV_KEY_YMAX
    ));

    private ArrayList<Entry> entries;
    private String name;

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public Dataset(String name){
        this.entries = new ArrayList<>();
        this.name = name;
    }

    public Entry createEntry(String filename, Rect boundingBox, int width, int height, String className){
        return new Entry(this, filename, boundingBox, width, height, className);
    }

    public Dataset(String name, CSVParser input){
        this(name);
        try {
            for(CSVRecord record : input.getRecords()){
                try {
                    Entry current = new Entry(record);
                    current.setOwner(this);
                    this.entries.add(current);
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCSV(CSVPrinter printer){
        try {
            printer.printRecord(CSV_KEYS);
            int index = 0;
            for(Entry current : entries){
                printer.print(index);
                current.writeToCSV(printer);
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Dataset(Parcel in) {
        name = in.readString();
        entries = in.createTypedArrayList(Entry.CREATOR);
        for(Entry entry : entries){
            entry.setOwner(this);
        }
    }

    public static final Creator<Dataset> CREATOR = new Creator<Dataset>() {
        @Override
        public Dataset createFromParcel(Parcel in) {
            return new Dataset(in);
        }

        @Override
        public Dataset[] newArray(int size) {
            return new Dataset[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeTypedList(entries);
    }

    public String getName() {
        return name;
    }



    public static class Entry implements Parcelable {
        private String filename;
        private Rect boundingBox;
        private String className;
        private int width;
        private int height;
        private Dataset owningDataset;

        protected Entry(Dataset owner, String filename, @NonNull Rect boundingBox, int width, int height, String className) {
            this.filename = filename;
            this.boundingBox = boundingBox;
            this.width = width;
            this.height = height;
            this.className = className;
            this.owningDataset = owner;
        }

        protected Entry(@NonNull CSVRecord record) {
            this.filename = record.get(CSV_KEY_FILENAME);
            int xmin = Integer.parseInt(record.get(CSV_KEY_XMIN));
            int xmax = Integer.parseInt(record.get(CSV_KEY_XMAX));
            int ymin = Integer.parseInt(record.get(CSV_KEY_YMIN));
            int ymax = Integer.parseInt(record.get(CSV_KEY_YMAX));
            this.boundingBox = new Rect(xmin, ymin, xmax, ymax);
            this.width = Integer.parseInt(record.get(CSV_KEY_WIDTH));
            this.height = Integer.parseInt(record.get(CSV_KEY_HEIGHT));
            this.className = record.get(CSV_KEY_CLASS);
        }

        private Entry(@NonNull Parcel in) {
            filename = in.readString();
            boundingBox = in.readParcelable(Rect.class.getClassLoader());
            className = in.readString();
            width = in.readInt();
            height = in.readInt();
        }

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel in) {
                return new Entry(in);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        protected void setOwner(@NonNull Dataset dataset){
            this.owningDataset = dataset;
        }

        public Dataset getOwningDataset(){
            return this.owningDataset;
        }

        public Bitmap loadImage(Context context){
            File directory = new File(context.getExternalFilesDir(null),this.getOwningDataset().getName());
            if(directory.isDirectory() && directory.exists()){
                File image = new File(directory,this.filename);
                if(image.exists()){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    return BitmapFactory.decodeFile(image.getAbsolutePath(), options);
                }
            }
            return null;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(getFilename());
            dest.writeParcelable(getBoundingBox(), flags);
            dest.writeString(getClassName());
            dest.writeInt(getWidth());
            dest.writeInt(getHeight());
        }



        public void writeToCSV(CSVPrinter printer) {
            try {
                printer.print(filename);
                printer.print(width);
                printer.print(height);
                printer.print(className);
                printer.print(boundingBox.left);   //xmin
                printer.print(boundingBox.right);  //xmax
                printer.print(boundingBox.top);    //ymin
                printer.print(boundingBox.bottom); //ymax
                printer.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public String getFilename() {
            return filename;
        }

        public Rect getBoundingBox() {
            return boundingBox;
        }

        public String getClassName() {
            return className;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
