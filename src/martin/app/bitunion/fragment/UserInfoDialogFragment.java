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
import martin.app.bitunion.util.BUApiHelper;
import martin.app.bitunion.util.BUAppUtils;
import martin.app.bitunion.model.BUUserInfo;
import martin.app.bitunion.util.HtmlImageGetter;
import martin.app.bitunion.util.PostMethod;
import martin.app.bitunion.util.BUAppUtils.Result;
import martin.app.bitunion.util.VolleyImageLoaderFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

public class UserInfoDialogFragment extends DialogFragment {

    private NetworkImageView mAvatar;
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
    private View readingstatusForm;

    private int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = getArguments().getInt("uid");
        setStyle(R.style.UserProfileDialog, 0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.userinfo_fragment, null);
        userinfoForm = (ScrollView) view.findViewById(R.id.userinfo_contentform);
        readingstatusForm = view.findViewById(R.id.userinfo_reading_status);

        mAvatar = (NetworkImageView) view.findViewById(R.id.userinfo_avatar);
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

        readUserInfo();
        return view;
    }

    public void setTextContent(BUUserInfo info) {
        mAvatar.setImageUrl(BUApiHelper.getImageAbsoluteUrl(info.getAvatar()), VolleyImageLoaderFactory.getImageLoader(getActivity()));
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

    private void readUserInfo() {
        BUApiHelper.getUserProfile(uid, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApiHelper.getResult(response) == Result.SUCCESS && !response.isNull("memberinfo")){
                    BUUserInfo info = new BUUserInfo(response.optJSONObject("memberinfo"));
                    setTextContent(info);
                } else
                    Toast.makeText(getActivity(), "Server Error: " + response.toString(), Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), R.string.network_unknown, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


