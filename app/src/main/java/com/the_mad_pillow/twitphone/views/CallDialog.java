package com.the_mad_pillow.twitphone.views;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.daasuu.ei.Ease;
import com.daasuu.ei.EasingInterpolator;
import com.the_mad_pillow.twitphone.R;
import com.the_mad_pillow.twitphone.activities.CallActivity;
import com.the_mad_pillow.twitphone.activities.MainActivity;
import com.the_mad_pillow.twitphone.others.FButton;
import com.twitter.sdk.android.core.models.User;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallDialog extends Dialog {
    @SuppressLint("ClickableViewAccessibility")
    public CallDialog(@NonNull Context context, User user) {
        super(context);
        setContentView(R.layout.custompopup);

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        CircleImageView profileImage = findViewById(R.id.profileImage);
        Glide.with(context)
                .load(user.profileImageUrlHttps.replace("_normal", ""))
                .apply(RequestOptions.overrideOf(120, 120))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        profileImage.setImageDrawable(resource);
                    }
                });
        final ObjectAnimator animator = ObjectAnimator.ofFloat(profileImage, "rotation", 0, 360);
        animator.setInterpolator(new EasingInterpolator(Ease.CUBIC_IN_OUT));
        animator.setDuration(1000);
        profileImage.setOnTouchListener((view, motionEvent) -> {
            if (!animator.isRunning()) {
                animator.start();
            }
            return false;
        });

        TextView txtClose = findViewById(R.id.txtClose);
        txtClose.setOnClickListener(v -> dismiss());

        TextView screenName = findViewById(R.id.screenName);
        screenName.setText(user.screenName);

        TextView userID = findViewById(R.id.userID);
        userID.setText(user.name);

        TextView tweets = findViewById(R.id.tweets);
        tweets.setText(String.valueOf(user.statusesCount));

        TextView followers = findViewById(R.id.followerCount);
        followers.setText(String.valueOf(user.followersCount));

        TextView friends = findViewById(R.id.friendCount);
        friends.setText(String.valueOf(user.friendsCount));

        FButton callButton = findViewById(R.id.call);
        callButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("peerImageUrl", user.profileImageUrlHttps);
            context.startActivity(intent);
            ((MainActivity) context).getPeer().call(user.screenName);
        });

        TextView openOtherApp = findViewById(R.id.openOtherApp);
        openOtherApp.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://twitter.com/" + user.screenName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        });
    }
}
