package martin.app.bitunion.model;

public class BUForum extends BUContent {

    private String name;
    private int fid;
    private int type;

    public BUForum(String name, int fid, int type) {
        this.name = name;
        this.fid = fid;
        this.type = type;
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

    public int getType() {
        return type;
    }

}
