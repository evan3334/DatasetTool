package pw.evan.datasettool.dataset;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class BoundingBox implements Parcelable {
    private Point upperLeft;
    private Point lowerRight;

    public BoundingBox(@NonNull Point upperLeft, @NonNull Point lowerRight){
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        validate();
    }

    public BoundingBox(@NonNull Point upperLeft, int width, int height){
        this.lowerRight = new Point(upperLeft.x()+width,upperLeft.y()+height);
        validate();
    }

    private BoundingBox(Parcel in) {
        upperLeft = in.readParcelable(Point.class.getClassLoader());
        lowerRight = in.readParcelable(Point.class.getClassLoader());
    }

    public static final Creator<BoundingBox> CREATOR = new Creator<BoundingBox>() {
        @Override
        public BoundingBox createFromParcel(Parcel in) {
            return new BoundingBox(in);
        }

        @Override
        public BoundingBox[] newArray(int size) {
            return new BoundingBox[size];
        }
    };

    public int getLeft(){
        return upperLeft.x();
    }

    public int getRight(){
        return lowerRight.x();
    }

    public int getTop(){
        return upperLeft.y();
    }

    public int getBottom(){
        return lowerRight.y();
    }

    public void setLeft(int left){
        this.upperLeft.setX(left);
        validate();
    }

    public void setRight(int right){
        this.lowerRight.setX(right);
        validate();
    }

    public void setTop(int top){
        this.upperLeft.setY(top);
        validate();
    }

    public void setBottom(int bottom){
        this.lowerRight.setY(bottom);
        validate();
    }

    public void setUpperLeft(Point upperLeft){
        this.upperLeft = upperLeft;
    }

    public void setLowerRight(Point lowerRight){
        this.lowerRight = lowerRight;
    }

    public Point getUpperLeft(){
        return upperLeft.copy();
    }

    public Point getLowerRight(){
        return lowerRight.copy();
    }

    public Point getUpperRight(){
        return new Point(getRight(), getTop());
    }

    public Point getLowerLeft(){
        return new Point(getLeft(), getBottom());
    }

    private void validate(){
        if(upperLeft.x()>lowerRight.x()){
            Point newUpperLeft = new Point(lowerRight.x(), upperLeft.y());
            Point newLowerRight = new Point(upperLeft.x(), lowerRight.y());
            this.upperLeft = newUpperLeft;
            this.lowerRight = newLowerRight;
        }
        if(upperLeft.y()>lowerRight.y()){
            Point newUpperLeft = new Point(upperLeft.x(), lowerRight.y());
            Point newLowerRight = new Point(lowerRight.x(), upperLeft.y());
            this.upperLeft = newUpperLeft;
            this.lowerRight = newLowerRight;
        }
    }

    public BoundingBox copy(){
        return new BoundingBox(upperLeft.copy(), lowerRight.copy());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(upperLeft, flags);
        dest.writeParcelable(lowerRight, flags);
    }

    public static class Point implements Parcelable{
        private int x;
        private int y;

        public Point(int x, int y){
            this.x = x;
            this.y = y;
        }

        private Point(Parcel in) {
            x = in.readInt();
            y = in.readInt();
        }

        public static final Creator<Point> CREATOR = new Creator<Point>() {
            @Override
            public Point createFromParcel(Parcel in) {
                return new Point(in);
            }

            @Override
            public Point[] newArray(int size) {
                return new Point[size];
            }
        };

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Point){
                Point p = (Point) obj;
                return this.x() == p.x() && this.y() == p.y();
            } else {
                return super.equals(obj);
            }
        }

        public Point copy(){
            return new Point(this.x, this.y);
        }

        public int x(){
            return this.x;
        }

        public int y(){
            return this.y;
        }

        public void setX(int x){
            this.x = x;
        }

        public void setY(int y){
            this.y = y;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(x);
            dest.writeInt(y);
        }
    }

}
