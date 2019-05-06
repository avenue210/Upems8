package cn.upus.app.upems.bean;

import java.util.List;

/**
 * 货格列表 分组数据
 */
public class BoxgroupEntity {

    /**
     * usecnt : 5
     * groupno : 1
     * sumcnt : 6
     * detail : [{"kindna":"小格","price":"0.5","lockno":"2","posno":"B02","shelfno":"B02","boardno":"2"},{"kindna":"小格","price":"0.5","lockno":"5","posno":"B05","shelfno":"B05","boardno":"2"},{"kindna":"中格","price":"0.6","lockno":"6","posno":"B06","shelfno":"B06","boardno":"2"},{"kindna":"大格","price":"0.7","lockno":"3","posno":"B03","shelfno":"B03","boardno":"2"},{"kindna":"大格","price":"0.7","lockno":"1","posno":"B01","shelfno":"B01","boardno":"2"}]
     */

    private String usecnt;
    private String groupno;
    private String sumcnt;
    private List<DetailBean> detail;

    public String getUsecnt() {
        return usecnt;
    }

    public void setUsecnt(String usecnt) {
        this.usecnt = usecnt;
    }

    public String getGroupno() {
        return groupno;
    }

    public void setGroupno(String groupno) {
        this.groupno = groupno;
    }

    public String getSumcnt() {
        return sumcnt;
    }

    public void setSumcnt(String sumcnt) {
        this.sumcnt = sumcnt;
    }

    public List<DetailBean> getDetail() {
        return detail;
    }

    public void setDetail(List<DetailBean> detail) {
        this.detail = detail;
    }

    public static class DetailBean {

        /**
         * kindno : 0
         * kindna : 小格
         * price : 0.5
         * lockno : 5
         * posno : A05
         * shelfno : A05
         * boardno : 1
         */

        private String kindno;
        private String kindna;
        private String price;
        private String lockno;
        private String posno;
        private String shelfno;
        private String boardno;

        private String groupno;//分组
        private int type;//选择的组
        private int size;//数量
        private int addSize;//已添加数量

        public String getGroupno() {
            return groupno;
        }

        public void setGroupno(String groupno) {
            this.groupno = groupno;
        }

        public String getKindno() {
            return kindno;
        }

        public void setKindno(String kindno) {
            this.kindno = kindno;
        }

        public String getKindna() {
            return kindna;
        }

        public void setKindna(String kindna) {
            this.kindna = kindna;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
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

        public String getShelfno() {
            return shelfno;
        }

        public void setShelfno(String shelfno) {
            this.shelfno = shelfno;
        }

        public String getBoardno() {
            return boardno;
        }

        public void setBoardno(String boardno) {
            this.boardno = boardno;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getAddSize() {
            return addSize;
        }

        public void setAddSize(int addSize) {
            this.addSize = addSize;
        }

        @Override
        public String toString() {
            return "DetailBean{" +
                    "kindno='" + kindno + '\'' +
                    ", kindna='" + kindna + '\'' +
                    ", price='" + price + '\'' +
                    ", lockno='" + lockno + '\'' +
                    ", posno='" + posno + '\'' +
                    ", shelfno='" + shelfno + '\'' +
                    ", boardno='" + boardno + '\'' +
                    ", type=" + type +
                    ", size=" + size +
                    ", addSize=" + addSize +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BoxgroupEntity{" +
                "usecnt='" + usecnt + '\'' +
                ", groupno='" + groupno + '\'' +
                ", sumcnt='" + sumcnt + '\'' +
                ", detail=" + detail +
                '}';
    }

}
