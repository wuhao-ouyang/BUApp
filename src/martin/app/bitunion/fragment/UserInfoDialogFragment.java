package martin.app.bitunion.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.MainActivity;
import martin.app.bitunion.R;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.model.BUUserInfo;
import martin.app.bitunion.util.HtmlImageGetter;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class UserInfoDialogFragment extends DialogFragment {

    private ImageView mAvatar;
    private TextView mUsername;
    private TextView mGroup;
    private TextView mCredit;
    private TextView mUid;
    private TextView mThreadnum;
    private TextView mPostnum;
    private TextView mBirth;
    private TextView mRegdate;
    private TextView mLastactive;
    private TextView mSite;
    private TextView mEmail;
    private TextView mSignt;

    private ScrollView userinfoForm;
    private LinearLayout readingstatusForm;

    private int uid;
    private Drawable imageDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = getArguments().getInt("uid");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.userinfo_fragment, null);
        userinfoForm = (ScrollView) view.findViewById(R.id.userinfo_contentform);
        readingstatusForm = (LinearLayout) view.findViewById(R.id.userinfo_reading_status);

        mAvatar = (ImageView) view.findViewById(R.id.userinfo_avatar);
        mUsername = (TextView) view.findViewById(R.id.userinfo_username);
        mGroup = (TextView) view.findViewById(R.id.userinfo_group);
        mCredit = (TextView) view.findViewById(R.id.userinfo_credit);
        mUid = (TextView) view.findViewById(R.id.userinfo_uid);
        mThreadnum = (TextView) view.findViewById(R.id.userinfo_threadnum);
        mPostnum = (TextView) view.findViewById(R.id.userinfo_postnum);
        mBirth = (TextView) view.findViewById(R.id.userinfo_birth);
        mRegdate = (TextView) view.findViewById(R.id.userinfo_regdate);
        mLastactive = (TextView) view.findViewById(R.id.userinfo_lastvisit);
        mSite = (TextView) view.findViewById(R.id.userinfo_site);
        mEmail = (TextView) view.findViewById(R.id.userinfo_email);
        mSignt = (TextView) view.findViewById(R.id.userinfo_signature);

        builder.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        new MyinfoReadTask().execute();
        Log.d("UserinfoDialog", "Dialog created");
        return builder.create();
    }

    public void setTextContent(BUUserInfo info){
        new GetAvatarTask(mAvatar, info.getAvatar()).execute();
        mUsername.setText(info.getUsername());
        mGroup.setText("用户组：" + info.getStatus());
        mCredit.setText("积分：" + info.getCredit());
        mUid.setText("UID：" + info.getUid());
        mThreadnum.setText("主题数：" + info.getThreadnum());
        mPostnum.setText("发帖数：" + info.getPostnum());
        mBirth.setText("生日：" + info.getBday());
        mRegdate.setText("注册日期：" + info.getRegdateLong());
        mLastactive.setText("上次登录：" + info.getLastvisitLong());
        mSite.setText("个人主页：" + info.getSite());
        mEmail.setText("E-mail：" + info.getEmail());
        mSignt.setText(Html.fromHtml("签名：<br>" + info.getSignature(),
                new HtmlImageGetter(getActivity(), mSignt), null));
        mSignt.setMovementMethod(LinkMovementMethod.getInstance());
        readingstatusForm.setVisibility(View.GONE);
        userinfoForm.setVisibility(View.VISIBLE);
    }

    class GetAvatarTask extends AsyncTask<Void, Void, Boolean> {

        String path;
        ImageView mView;

        public GetAvatarTask(ImageView v, String url) {
            mView = v;
            BUApplication.settings.setNetType(BUApplication.settings.mNetType);
            path = url;
            path = path.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org",
                    BUApplication.settings.ROOTURL);
            path = path.replaceAll("^images/", BUApplication.settings.ROOTURL
                    + "/images/");
            path = path.replaceAll("^attachments/",
                    BUApplication.settings.ROOTURL + "/attachments/");
            Log.v("Userinfo", "GetAvatarTask>>" + path);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            InputStream is = null;
            try {
                is = BUAppUtils.getImageVewInputStream(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            Drawable drawable = Drawable.createFromStream(is, path);
            if (drawable == null){
                drawable = getResources().getDrawable(R.drawable.noavatar);}
            float asratio = (float) drawable.getIntrinsicWidth()
                    / drawable.getIntrinsicHeight();
            if (asratio > 0.7)
                drawable.setBounds(0, 0, mAvatar.getWidth(),
                        (int) (mAvatar.getWidth() / asratio));
            else
                drawable.setBounds(0, 0, (int) (mAvatar.getHeight() * asratio),
                        mAvatar.getHeight());
            imageDrawable = drawable;
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (getActivity() != null)
                updateImage();
        }

    }

    private void updateImage() {
        mAvatar.setImageDrawable(imageDrawable);
    }

    private class MyinfoReadTask extends AsyncTask<Void, Void, Result> {

        PostMethod postMethod = new PostMethod();

        @Override
        protected Result doInBackground(Void... params) {
            Log.d("ReadInfoTask", "Userinfo requested");
            JSONObject postReq = new JSONObject();
            try {
                postReq.put("action", "profile");
                postReq.put("username", URLEncoder.encode(
                        BUApplication.settings.mUsername, "utf-8"));
                postReq.put("session", BUApplication.settings.mSession);
                postReq.put("uid", uid);
                return postMethod.sendPost(BUAppUtils.getUrl(BUApplication.settings.mNetType, BUAppUtils.REQ_PROFILE), postReq);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            Log.d("ReadInfoTask", "Result>>" + result);
            switch (result) {
                default:
                    return;
                case FAILURE:
                    return;
                case NETWRONG:
                    return;
                case UNKNOWN:
                    return;
                case SUCCESS:
            }
            try {
                BUUserInfo info = new BUUserInfo(
                        postMethod.jsonResponse.getJSONObject("memberinfo"));
                if (getActivity() != null)
                    setTextContent(info);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
