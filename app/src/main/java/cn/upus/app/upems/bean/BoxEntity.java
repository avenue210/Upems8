package cn.upus.app.upems.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 货格列表信息
 */
public class BoxEntity implements Parcelable {

    /**
     * shelfno : A02
     * kindna : 小格
     * price : 0.5000
     */

    //{"kindno":"0","kindna":"小格","price":"0.00","stateno":"1","lockno":"2","posno":"A102","shelfno":"JD13A102","boardno":"1","statena":"可用"}

    private String shelfno;
    private String kindna;
    private String kindno;
    private String price;
    private String boardno;//版号
    private String lockno;//锁号
    private String posno;//货位号
    private int size;//分类柜子的总数量

    private boolean isAdd;

    public BoxEntity() {

    }

    public BoxEntity(String shelfno, String kindna, String kindno, String price, String boardno, String lockno, String posno, int size, boolean isAdd) {
        this.shelfno = shelfno;
        this.kindna = kindna;
        this.kindno = kindno;
        this.price = price;
        this.boardno = boardno;
        this.lockno = lockno;
        this.posno = posno;
        this.size = size;
        this.isAdd = isAdd;
    }

    public String getShelfno() {
        return shelfno;
    }

    public void setShelfno(String shelfno) {
        this.shelfno = shelfno;
    }

    public String getKindna() {
        return kindna;
    }

    public void setKindna(String kindna) {
        this.kindna = kindna;
    }

    public String getKindno() {
        return kindno;
    }

    public void setKindno(String kindno) {
        this.kindno = kindno;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBoardno() {
        return boardno;
    }

    public void setBoardno(String boardno) {
        this.boardno = boardno;
    }

    public String getLockno() {
        return lockno;
    }

    public void setLockno(String lockno) {
        this.lockno = lockno;
    }

    public String getPosno() {
        return posno;
    }

    public void setPosno(String posno) {
        this.posno = posno;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    @Override
    public String toString() {
        return "BoxEntity{" +
                "shelfno='" + shelfno + '\'' +
                ", kindna='" + kindna + '\'' +
                ", kindno='" + kindno + '\'' +
                ", price='" + price + '\'' +
                ", boardno='" + boardno + '\'' +
                ", lockno='" + lockno + '\'' +
                ", posno='" + posno + '\'' +
                ", size=" + size +
                ", isAdd=" + isAdd +
                '}';
    }

    public static Creator<BoxEntity> getCREATOR() {
        return CREATOR;
    }

    protected BoxEntity(Parcel in) {
        shelfno = in.readString();
        kindna = in.readString();
        kindno = in.readString();
        price = in.readString();
        boardno = in.readString();
        lockno = in.readString();
        posno = in.readString();
        size = in.readInt();
        isAdd = in.readByte() != 0;
    }

    public static final Creator<BoxEntity> CREATOR = new Creator<BoxEntity>() {
        @Override
        public BoxEntity createFromParcel(Parcel in) {
            return new BoxEntity(in);
        }

        @Override
        public BoxEntity[] newArray(int size) {
            return new BoxEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shelfno);
        dest.writeString(kindna);
        dest.writeString(kindno);
        dest.writeString(price);
        dest.writeString(boardno);
        dest.writeString(lockno);
        dest.writeString(posno);
        dest.writeInt(size);
        dest.writeByte((byte) (isAdd ? 1 : 0));
    }
}
