package martin.app.bitunion.util;

public class BUAppSettings {
	public int titletextsize;
	public int contenttextsize;
	public String titlebackground;
	public String textbackground;
	public String listbackgrounddark;
	public String listbackgroundlight;

	public String mUsername;
	public String mPassword;
	public String mSession;
	public int mNetType;
	public String ROOTURL;

	public void setNetType(int net) {
		mNetType = net;
		if (net == BUAppUtils.BITNET)
			ROOTURL = "http://www.bitunion.org";
		else if (net == BUAppUtils.OUTNET)
			ROOTURL = "http://out.bitunion.org";
	}
}
