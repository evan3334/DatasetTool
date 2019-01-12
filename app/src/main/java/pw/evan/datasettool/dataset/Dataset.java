package pw.evan.datasettool.dataset;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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

    private static final CSVFormat FORMAT = CSVFormat.INFORMIX_UNLOAD_CSV.withHeader().withIgnoreEmptyLines(true);

    private ArrayList<Entry> entries;
    private String name;

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public Dataset(String name) {
        this.entries = new ArrayList<>();
        this.name = name;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append(" - ");
        sb.append(entries.size());
        sb.append(" entries: ");
        for(Entry entry : entries){
            sb.append("\n");
            sb.append(entry.toString());
        }
        return sb.toString();
    }

    public static Entry createEntry(Uri imageUri, Rect boundingBox, int width, int height, String className) {
        return new Entry(imageUri, boundingBox, width, height, className);
    }

    public static Entry createEntry(File image, String objectClass){
        Uri imageUri = Uri.fromFile(image);
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap.recycle();
        Rect boundingBox = new Rect(0,0,width, height);
        return createEntry(imageUri, boundingBox,width,height,objectClass);
    }

    public int getIndex(Entry entry){
        return entries.indexOf(entry);
    }

    private Dataset(Context context, String name, CSVParser input) {
        this(name);
        try {
            for (CSVRecord record : input.getRecords()) {
                try {
                    Entry current = new Entry(context, this, record);
                    this.entries.add(current);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getCSV(@NonNull Context context, @NonNull String datasetName) {
        Log.d("context==null", String.valueOf(context==null));
        Log.d("datasetName==null",String.valueOf(datasetName==null));
        File directory = new File(context.getExternalFilesDir(null), datasetName);
        if (directory.exists() && directory.isDirectory()) {
            File csvFile = new File(directory, datasetName + ".csv");
            if (csvFile.exists()) {
                return csvFile;
            }
        }
        return null;
    }

    public static Dataset loadFromFile(Context context, String datasetName) {
        File csvFile = getCSV(context, datasetName);
        if (csvFile != null) {
            try {
                CSVParser parser = CSVParser.parse(csvFile, Charset.forName("UTF-8"), FORMAT);
                Dataset dataset = new Dataset(context, datasetName, parser);
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

    public void writeToFile(Context context){
        File csvFile = getCSV(context, getName());
        try {
            FileWriter writer = new FileWriter(csvFile);
            CSVPrinter printer = new CSVPrinter(writer, FORMAT);
            writeToCSV(printer);
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getDatasetDirectory(Context context, String datasetName){
        return new File(context.getExternalFilesDir(null), datasetName);
    }

    public void writeToCSV(CSVPrinter printer) {
        try {
            printer.printRecord(CSV_KEYS);
            int index = 0;
            for (Entry current : entries) {
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
        private Uri imageURI;
        private Rect boundingBox;
        private String className;
        private int width;
        private int height;

        protected Entry(@NonNull Uri imageURI, @NonNull Rect boundingBox, int width, int height, String className) {
            this.setImageURI(imageURI);
            this.setBoundingBox(boundingBox);
            this.setWidth(width);
            this.setHeight(height);
            this.setClassName(className);
        }

        protected Entry(@NonNull Context context, @NonNull Dataset parent, @NonNull CSVRecord record) {
            String filename = record.get(CSV_KEY_FILENAME);
            File directory = new File(context.getExternalFilesDir(null), parent.getName());
            File imageFile = new File(directory, filename);
            this.setImageURI(Uri.fromFile(imageFile));
            int xmin = Integer.parseInt(record.get(CSV_KEY_XMIN));
            int xmax = Integer.parseInt(record.get(CSV_KEY_XMAX));
            int ymin = Integer.parseInt(record.get(CSV_KEY_YMIN));
            int ymax = Integer.parseInt(record.get(CSV_KEY_YMAX));
            this.setBoundingBox(new Rect(xmin, ymin, xmax, ymax));
            this.setWidth(Integer.parseInt(record.get(CSV_KEY_WIDTH)));
            this.setHeight(Integer.parseInt(record.get(CSV_KEY_HEIGHT)));
            this.setClassName(record.get(CSV_KEY_CLASS));
        }

        private Entry(@NonNull Parcel in) {
            setImageURI(in.readParcelable(Uri.class.getClassLoader()));
            setBoundingBox(in.readParcelable(Rect.class.getClassLoader()));
            setClassName(in.readString());
            setWidth(in.readInt());
            setHeight(in.readInt());
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

        public Bitmap loadImage(Context context) {
            File image = new File(imageURI.getPath());
            if (image.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            } else {
                return null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(imageURI, flags);
            dest.writeParcelable(getBoundingBox(), flags);
            dest.writeString(getClassName());
            dest.writeInt(getWidth());
            dest.writeInt(getHeight());
        }


        public void writeToCSV(CSVPrinter printer) {
            try {
                printer.print(getFilename());
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

        public String toString(){
            return getClass().getName() +
                    " - file: " +
                    getFilename() +
                    ", bbox: " +
                    boundingBox.toShortString() +
                    ", dims: (" +
                    width +
                    ", " +
                    height +
                    ")";
        }


        public String getFilename() {
            return imageURI.getLastPathSegment();
        }

        public Uri getImageURI() {
            return imageURI;
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

        public void setImageURI(@NonNull Uri imageURI) {
            this.imageURI = imageURI;
        }

        public void setBoundingBox(@NonNull Rect boundingBox) {
            this.boundingBox = boundingBox;
            validateBoundingBox();
        }

        public void setClassName(@NonNull String className) {
            this.className = className;
        }

        public void setWidth(int width) {
            if (width > 0) {
                this.width = width;
            } else {
                throw new IllegalArgumentException("Width can't be less than 1!");
            }
        }

        public void setHeight(int height) {
            if (height > 0) {
                this.height = height;
            } else {
                throw new IllegalArgumentException("Height can't be less than 1!");
            }
        }

        private void validateBoundingBox(){
            int left = boundingBox.left;
            int right = boundingBox.right;
            int top = boundingBox.top;
            int bottom = boundingBox.bottom;
            if(left > right){
                left = boundingBox.right;
                right = boundingBox.left;
            }
            if(top > bottom){
                top = boundingBox.bottom;
                bottom = boundingBox.top;
            }
            boundingBox.set(left,top,right,bottom);
        }
    }
}
