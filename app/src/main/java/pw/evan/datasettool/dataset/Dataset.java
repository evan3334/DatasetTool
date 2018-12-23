package pw.evan.datasettool.dataset;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Dataset implements Parcelable{
    private ArrayList<Entry> entries;

    public ArrayList<Entry> getEntries(){
        return entries;
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


        private Entry(Parcel in) {
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
