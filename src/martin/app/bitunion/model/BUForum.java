package martin.app.bitunion.model;

public class BUForum extends BUContent {

    private String name;
    private int fid;
    private int type;

    public BUForum() {
        this.name = "";
        this.fid = 0;
        this.type = 0;
    }

    public BUForum(String name, int fid, int type) {
        this.name = name;
        this.fid = fid;
        this.type = type;
    }

    public BUForum(BUForum forum) {
        this.name = forum.name;
        this.fid = forum.fid;
        this.type = forum.type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
