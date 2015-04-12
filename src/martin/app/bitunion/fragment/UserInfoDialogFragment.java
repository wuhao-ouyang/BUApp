package martin.app.bitunion.fragment;

import org.json.JSONObject;

import martin.app.bitunion.R;
import martin.app.bitunion.util.BUApi;
import martin.app.bitunion.model.BUUser;
import martin.app.bitunion.util.CommonIntents;
import martin.app.bitunion.util.HtmlImageGetter;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

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
    private View readingstatusForm;

    private int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = getArguments().getInt(CommonIntents.EXTRA_UID);
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
        return inflater.inflate(R.layout.userinfo_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userinfoForm = (ScrollView) view.findViewById(R.id.userinfo_contentform);
        readingstatusForm = view.findViewById(R.id.userinfo_reading_status);

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

        readUserInfo();
    }

    public void setTextContent(BUUser info) {
//        mAvatar.setImageUrl(BUApi.getImageAbsoluteUrl(info.getAvatar()), VolleyImageLoaderFactory.getImageLoader(getActivity()));
        Glide.with(this).load(BUApi.getImageAbsoluteUrl(info.getAvatar()))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mAvatar.setImageDrawable(resource);
                        if (resource.isAnimated()) {
                            resource.setLoopCount(GlideDrawable.LOOP_FOREVER);
                            resource.start();
                        }
                    }
                });
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
        BUApi.getUserProfile(uid, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (BUApi.getResult(response) == BUApi.Result.SUCCESS && !response.isNull("memberinfo")) {
                    BUUser info = new BUUser(response.optJSONObject("memberinfo"));
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


