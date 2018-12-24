package pw.evan.datasettool.dataset;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

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

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public Dataset(){
        this.entries = new ArrayList<>();
    }

    public Dataset(CSVParser input){
        this();
        try {
            for(CSVRecord record : input.getRecords()){
                try {
                    Entry current = new Entry(record);
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
            for(Entry current : entries){
                current.writeToCSV(printer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Dataset(Parcel in) {
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
        dest.writeTypedList(entries);
    }


    public static class Entry implements Parcelable {
        private int index;
        private String filename;
        private BoundingBox boundingBox;
        private String className;
        private int width;
        private int height;

        public Entry(int index, String filename, @NonNull BoundingBox boundingBox, int width, int height, String className) {
            this.index = index;
            this.filename = filename;
            this.boundingBox = boundingBox.copy();
            this.width = width;
            this.height = height;
            this.className = className;
        }

        public Entry(@NonNull CSVRecord record) {
            this.index = Integer.parseInt(record.get(CSV_KEY_INDEX));
            this.filename = record.get(CSV_KEY_FILENAME);
            int xmin = Integer.parseInt(record.get(CSV_KEY_XMIN));
            int xmax = Integer.parseInt(record.get(CSV_KEY_XMAX));
            int ymin = Integer.parseInt(record.get(CSV_KEY_YMIN));
            int ymax = Integer.parseInt(record.get(CSV_KEY_YMAX));
            this.boundingBox = new BoundingBox(xmin, ymin, xmax, ymax);
            this.width = Integer.parseInt(record.get(CSV_KEY_WIDTH));
            this.height = Integer.parseInt(record.get(CSV_KEY_HEIGHT));
            this.className = record.get(CSV_KEY_CLASS);
        }

        private Entry(@NonNull Parcel in) {
            index = in.readInt();
            filename = in.readString();
            boundingBox = in.readParcelable(BoundingBox.class.getClassLoader());
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(getIndex());
            dest.writeString(getFilename());
            dest.writeParcelable(getBoundingBox(), flags);
            dest.writeString(getClassName());
            dest.writeInt(getWidth());
            dest.writeInt(getHeight());
        }

        public void writeToCSV(CSVPrinter printer) {
            try {
                printer.print(index);
                printer.print(filename);
                printer.print(width);
                printer.print(height);
                printer.print(className);
                printer.print(boundingBox.getLeft());   //xmin
                printer.print(boundingBox.getRight());  //xmax
                printer.print(boundingBox.getTop());    //ymin
                printer.print(boundingBox.getBottom()); //ymax
                printer.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getIndex() {
            return index;
        }

        public String getFilename() {
            return filename;
        }

        public BoundingBox getBoundingBox() {
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
