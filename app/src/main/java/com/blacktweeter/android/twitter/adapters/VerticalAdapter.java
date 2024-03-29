package com.blacktweeter.android.twitter.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blacktweeter.android.twitter.activities.MainActivity;
import com.blacktweeter.android.twitter.activities.compose.ComposeActivity;
import com.blacktweeter.android.twitter.activities.tweet_viewer.ViewPictures;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.utils.EmojiUtils;
import com.blacktweeter.android.twitter.utils.ImageUtils;
import com.blacktweeter.android.twitter.utils.SDK11;
import com.blacktweeter.android.twitter.utils.TweetLinkUtils;
import com.blacktweeter.android.twitter.utils.Utils;
import com.blacktweeter.android.twitter.utils.text.TouchableMovementMethod;
import com.blacktweeter.android.twitter.views.NetworkedCacheableImageView;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.data.App;
import com.blacktweeter.android.twitter.activities.profile_viewer.ProfilePager;
import com.blacktweeter.android.twitter.activities.tweet_viewer.TweetPager;
import com.blacktweeter.android.twitter.activities.photo_viewer.PhotoViewerActivity;
import com.blacktweeter.android.twitter.utils.text.TextUtils;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import twitter4j.*;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * Created by benakinlosotuwork on 2/12/18.
 */

public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder>{


    Context context;
    public ArrayList<Status> statuses;
    public LayoutInflater inflater;
    public AppSettings settings;
    public int border;

    public static final String REGEX = "(http|ftp|https):\\/\\/([\\w\\-_]+(?:(?:\\.[\\w\\-_]+)+))([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    public static Pattern pattern = Pattern.compile(REGEX);

    public boolean hasKeyboard = false;
    public boolean isProfile = false;

    public int layout;
    public XmlResourceParser addonLayout = null;
    public Resources res;
    public int talonLayout;
    public BitmapLruCache mCache;

    public ColorDrawable transparent;

    public Handler[] mHandler;
    public Handler emojiHandler;
    public int currHandler = 0;

    public int type;
    public String username;

    public java.text.DateFormat dateFormatter;
    public java.text.DateFormat timeFormatter;

    public static final int NORMAL = 0;
    public static final int RETWEET = 1;
    public static final int FAVORITE = 2;
    private static final String TAG = "VerticalAdapter";
    DisplayMetrics metrics = new DisplayMetrics();

    public VerticalAdapter(Context context, ArrayList<Status> statuses) {
       // super(context, R.layout.tweet);
        this.statuses = statuses;
        this.context = context;
        this.settings = AppSettings.getInstance(context);

        this.inflater = LayoutInflater.from(context);


        this.type = NORMAL;

        this.username = "";

        setUpLayout();
    }

    @SuppressLint("ResourceAsColor")
    public void setUpLayout() {
        mHandler = new Handler[4];

        dateFormatter = android.text.format.DateFormat.getDateFormat(context);
        timeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        if (settings.militaryTime) {
            timeFormatter = new SimpleDateFormat("kk:mm");
        }

        talonLayout = settings.layout;

        if (settings.addonTheme) {
            try {
                res = context.getPackageManager().getResourcesForApplication(settings.addonThemePackage);
                addonLayout = res.getLayout(res.getIdentifier("tweet", "layout", settings.addonThemePackage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //THIS IS NOT WERE WE INFLATE THE TWEET CELL, WE GO TO the_latest_tweet
        switch (talonLayout) {
            case AppSettings.LAYOUT_TALON:
                layout = R.layout.tweet;
                break;
            case AppSettings.LAYOUT_HANGOUT:
                layout = R.layout.tweet_hangout;
                break;
            case AppSettings.LAYOUT_FULL_SCREEN:
                layout = R.layout.tweet_full_screen; //R.layout.the_latest_tweet;
                break;
        }

        TypedArray b;
        if (settings.roundContactImages) {
            b = context.getTheme().obtainStyledAttributes(new int[]{R.attr.circleBorder});
          //  b = context.getTheme().obtainStyledAttributes(new int[]{R.attr.squareBorder});
        } else {
            b = context.getTheme().obtainStyledAttributes(new int[]{R.attr.squareBorder});
        }
        border = b.getResourceId(0, 0);
        b.recycle();

        mCache = App.getInstance(context).getBitmapCache();

        transparent = new ColorDrawable(android.R.color.transparent);

        mHandler = new Handler[10];
        for (int i = 0; i < 10; i++) {
            mHandler[i] = new Handler();
        }
        currHandler = 0;
    }

    @Override
    public VerticalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.the_latest_tweet, parent, false );

        return new VerticalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VerticalViewHolder holder, int position) {
       // holder.textView.setText(statuses.get(position));


        if (holder.expandArea.getVisibility() == View.VISIBLE) {
            removeExpansionNoAnimation(holder);
        }

        Status thisStatus;

        String retweeter;
        final long time = statuses.get(position).getCreatedAt().getTime();
        long originalTime = 0;

        if (statuses.get(position).isRetweet()) {
            retweeter = statuses.get(position).getUser().getScreenName();

            thisStatus = statuses.get(position).getRetweetedStatus();
            originalTime = thisStatus.getCreatedAt().getTime();
        } else {
            retweeter = "";

            thisStatus = statuses.get(position);
        }

        final long fOriginalTime = originalTime;

        holder.profilePic.setImageDrawable(context.getResources().getDrawable(border));
        holder.image.setVisibility(View.VISIBLE);

        User user = thisStatus.getUser();

        holder.tweetId = thisStatus.getId();
        final long id = holder.tweetId;
        final String profileImageRegURL = user.getBiggerProfileImageURL();
        final String profilePicOriURL = user.getOriginalProfileImageURL();
        Picasso.with(context).load(profileImageRegURL).into(holder.profilePic);




        String tweetTexts = thisStatus.getText();
        final String name = user.getName();
        final String screenname = user.getScreenName();

        String[] html = TweetLinkUtils.getLinksInStatus(thisStatus);

        final String tweetText = html[0];
        final String picUrl = html[1];
        holder.picUrl = picUrl;
        final String otherUrl = html[2];
        final String hashtags = html[3];
        final String users = html[4];

        final boolean inAConversation = thisStatus.getInReplyToStatusId() != -1;

        holder.gifUrl = TweetLinkUtils.getGIFUrl(statuses.get(position), otherUrl);

        if (holder.isAConversation != null) {
            if (inAConversation) {
                if (holder.isAConversation.getVisibility() != View.VISIBLE) {
                    holder.isAConversation.setVisibility(View.VISIBLE);
                }
            } else {
                if (holder.isAConversation.getVisibility() != View.GONE) {
                    holder.isAConversation.setVisibility(View.GONE);
                }
            }
        }

        if(!settings.reverseClickActions) {
            final String fRetweeter = retweeter;
            holder.background.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    String link;

                    boolean hasGif = holder.gifUrl != null && !holder.gifUrl.isEmpty();
                    boolean displayPic = !holder.picUrl.equals("") && !holder.picUrl.contains("youtube") && !(hasGif);
                    if (displayPic) {
                        link = holder.picUrl;
                    } else {
                        link = otherUrl.split("  ")[0];
                    }

                    Log.v("tweet_page", "clicked");
                    Intent viewTweet = new Intent(context, TweetPager.class);
                    viewTweet.putExtra("name", name);
                    viewTweet.putExtra("screenname", screenname);
                    viewTweet.putExtra("time", time);
                    viewTweet.putExtra("tweet", tweetText);
                    viewTweet.putExtra("retweeter", fRetweeter);
                    viewTweet.putExtra("webpage", link);
                    viewTweet.putExtra("other_links", otherUrl);
                    viewTweet.putExtra("picture", displayPic);
                    viewTweet.putExtra("tweetid", holder.tweetId);
                    viewTweet.putExtra("proPic", profilePicOriURL);
                    viewTweet.putExtra("users", users);
                    viewTweet.putExtra("hashtags", hashtags);
                    viewTweet.putExtra("animated_gif", holder.gifUrl);

                    context.startActivity(viewTweet);

                    return true;
                }
            });

            holder.background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.preventNextClick) {
                        holder.preventNextClick = false;
                        return;
                    }
                    if (holder.expandArea.getVisibility() == View.GONE) {
                        addExpansion(holder, screenname, users, otherUrl.split("  "), holder.picUrl, id);
                    } else {
                        removeExpansionWithAnimation(holder);
                        removeKeyboard(holder);
                    }
                }
            });

        } else {
            final String fRetweeter = retweeter;
            holder.background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.preventNextClick) {
                        holder.preventNextClick = false;
                        return;
                    }

                    String link;

                    boolean hasGif = holder.gifUrl != null && !holder.gifUrl.isEmpty();
                    boolean displayPic = !holder.picUrl.equals("") && !holder.picUrl.contains("youtube") && !(hasGif);
                    if (displayPic) {
                        link = holder.picUrl;
                    } else {
                        link = otherUrl.split("  ")[0];
                    }

                    Log.v("tweet_page", "clicked");
                    Intent viewTweet = new Intent(context, TweetPager.class);
                    viewTweet.putExtra("name", name);
                    viewTweet.putExtra("screenname", screenname);
                    viewTweet.putExtra("time", time);
                    viewTweet.putExtra("tweet", tweetText);
                    viewTweet.putExtra("retweeter", fRetweeter);
                    viewTweet.putExtra("webpage", link);
                    viewTweet.putExtra("other_links", otherUrl);
                    viewTweet.putExtra("picture", displayPic);
                    viewTweet.putExtra("tweetid", holder.tweetId);
                    viewTweet.putExtra("proPic", profilePicOriURL);
                    viewTweet.putExtra("users", users);
                    viewTweet.putExtra("hashtags", hashtags);
                    viewTweet.putExtra("animated_gif", holder.gifUrl);

                    context.startActivity(viewTweet);
                }
            });

            holder.background.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (holder.expandArea.getVisibility() == View.GONE) {
                        addExpansion(holder, screenname, users, otherUrl.split("  "), holder.picUrl, id);
                    } else {
                        removeExpansionWithAnimation(holder);
                        removeKeyboard(holder);
                    }

                    return true;
                }
            });
        }

        if (!screenname.equals(username)) {
            holder.profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewProfile = new Intent(context, ProfilePager.class);
                    viewProfile.putExtra("name", name);
                    viewProfile.putExtra("screenname", screenname);
                    viewProfile.putExtra("proPic", profilePicOriURL);
                    viewProfile.putExtra("tweetid", holder.tweetId);
                    viewProfile.putExtra("retweet", holder.retweeter.getVisibility() == View.VISIBLE);
                    viewProfile.putExtra("long_click", false);

                    context.startActivity(viewProfile);
                }
            });

            holder.profilePic.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {

                    Intent viewProfile = new Intent(context, ProfilePager.class);
                    viewProfile.putExtra("name", name);
                    viewProfile.putExtra("screenname", screenname);
                    viewProfile.putExtra("proPic", profilePicOriURL);
                    viewProfile.putExtra("tweetid", holder.tweetId);
                    viewProfile.putExtra("retweet", holder.retweeter.getVisibility() == View.VISIBLE);
                    viewProfile.putExtra("long_click", true);

                    context.startActivity(viewProfile);

                    return false;
                }
            });
        } else {
            // need to clear the click listener so it isn't left over from another profile
            holder.profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            holder.profilePic.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
        }

        if (talonLayout == AppSettings.LAYOUT_FULL_SCREEN ||
                (settings.nameAndHandleOnTweet && settings.addonTheme)) {
            if (holder.screenTV.getVisibility() == View.GONE) {
                holder.screenTV.setVisibility(View.VISIBLE);
            }
            holder.screenTV.setText("@" + screenname);
            holder.name.setText(name);
        } else {
            if (!settings.showBoth) {
                holder.name.setText(settings.displayScreenName ? "@" + screenname : name);
            } else {
                if (holder.screenTV.getVisibility() == View.GONE) {
                    holder.screenTV.setVisibility(View.VISIBLE);
                }
                holder.name.setText(name);
                holder.screenTV.setText("@" + screenname);
            }
        }

        if (!settings.absoluteDate) {
            holder.time.setText(Utils.getTimeAgo(time, context));
        } else {
            Date date = new Date(time);
            holder.time.setText(timeFormatter.format(date).replace("24:", "00:") + ", " + dateFormatter.format(date));
        }

        holder.tweet.setText(tweetText);

        boolean picture = false;

        if(settings.inlinePics) {
            if (holder.picUrl.equals("")) {
                if (holder.image.getVisibility() != View.GONE) {
                    holder.image.setVisibility(View.GONE);
                }


                if (holder.playButton.getVisibility() == View.VISIBLE) {
                    holder.playButton.setVisibility(View.GONE);
                }
            } else {
                if (holder.picUrl.contains("youtube") || (holder.gifUrl != null && !android.text.TextUtils.isEmpty(holder.gifUrl))) {

                    if (holder.playButton.getVisibility() == View.GONE) {
                        holder.playButton.setVisibility(View.VISIBLE);
                    }

                    final String myRetweeter = retweeter;

                    holder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String link;

                            boolean hasGif = holder.gifUrl != null && !holder.gifUrl.isEmpty();
                            boolean displayPic = !holder.picUrl.equals("") && !holder.picUrl.contains("youtube") && !(hasGif);
                            if (displayPic) {
                                link = holder.picUrl;
                            } else {
                                link = otherUrl.split("  ")[0];
                            }

                            Intent viewTweet = new Intent(context, TweetPager.class);
                            viewTweet.putExtra("name", name);
                            viewTweet.putExtra("screenname", screenname);
                            viewTweet.putExtra("time", time);
                            viewTweet.putExtra("tweet", tweetText);
                            viewTweet.putExtra("retweeter", myRetweeter);
                            viewTweet.putExtra("webpage", link);
                            viewTweet.putExtra("other_links", otherUrl);
                            viewTweet.putExtra("picture", displayPic);
                            viewTweet.putExtra("tweetid", holder.tweetId);
                            viewTweet.putExtra("proPic", profilePicOriURL);
                            viewTweet.putExtra("users", users);
                            viewTweet.putExtra("hashtags", hashtags);
                            viewTweet.putExtra("clicked_youtube", true);
                            viewTweet.putExtra("animated_gif", holder.gifUrl);

                            context.startActivity(viewTweet);
                        }
                    });

                    holder.image.setImageDrawable(transparent);

                    picture = true;

                } else {
                    holder.image.setImageDrawable(transparent);

                    picture = true;

                    if (holder.playButton.getVisibility() == View.VISIBLE) {
                        holder.playButton.setVisibility(View.GONE);
                    }

                    holder.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (holder.picUrl.contains(" ")) {
                                context.startActivity(new Intent(context, ViewPictures.class).putExtra("pictures", holder.picUrl));
                                Log.d(TAG, "multiple pics?");
                            } else {
                                context.startActivity(new Intent(context, PhotoViewerActivity.class).putExtra("url", holder.picUrl));
                                Log.d(TAG, "one pic?");
                            }

                        }
                    });


                    holder.viewMoreText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(new Intent(context, ViewPictures.class).putExtra("pictures", holder.picUrl));
                        }
                    });


                }

                if (holder.image.getVisibility() == View.GONE) {
                    holder.image.setVisibility(View.VISIBLE);
                }
            }
        }

        if (type == NORMAL) {
            if (retweeter.length() > 0) {
                holder.retweeter.setText(context.getResources().getString(R.string.retweeter) + retweeter);
                holder.retweeterName = retweeter;
                holder.retweeter.setVisibility(View.VISIBLE);
            } else if (holder.retweeter.getVisibility() == View.VISIBLE) {
                holder.retweeter.setVisibility(View.GONE);
            }
        } else if (type == RETWEET) {

            int count = statuses.get(position).getRetweetCount();

            if (count > 1) {
                holder.retweeter.setText(statuses.get(position).getRetweetCount() + " " + context.getResources().getString(R.string.retweets_lower));
                holder.retweeter.setVisibility(View.VISIBLE);
            } else if (count == 1) {
                holder.retweeter.setText(statuses.get(position).getRetweetCount() + " " + context.getResources().getString(R.string.retweet_lower));
                holder.retweeter.setVisibility(View.VISIBLE);
            }
        }

        if (picture) {
            CacheableBitmapDrawable wrapper = mCache.getFromMemoryCache(holder.picUrl);
            if (wrapper != null) {
                holder.image.setImageDrawable(wrapper);
                holder.image.setVisibility(View.VISIBLE);
                Picasso.with(context).load(picUrl).into(holder.image);
                if (picUrl.contains(" ")) {
                    String picUrlArray[] = picUrl.split(" ");
                    String firstPicUrl = picUrlArray[0];
                   // int myInt = (int) (MainActivity.screenWidth * 0.5);
                   // holder.image.getLayoutParams().width = myInt;

                    Picasso.with(context).load(firstPicUrl).into(holder.image);
                    holder.viewMoreText.setVisibility(View.VISIBLE);

                }
                Log.d("ben!", "pic url 554: " + holder.picUrl);
                picture = false;
            }
        }

        final boolean hasPicture = picture;
        mHandler[currHandler].removeCallbacksAndMessages(null);
        mHandler[currHandler].postDelayed(new Runnable() {
            @Override
            public void run() {
                if (holder.tweetId == id) {
                    if (hasPicture) {
                        loadImage(context, holder, holder.picUrl, mCache, id);
                    }

                    if (settings.useEmoji && (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || EmojiUtils.ios)) {
                        String text = holder.tweet.getText().toString();
                        if (EmojiUtils.emojiPattern.matcher(text).find()) {
                            final Spannable span = EmojiUtils.getSmiledText(context, Html.fromHtml(tweetText));
                            holder.tweet.setText(span);
                        }
                    }

                    holder.tweet.setSoundEffectsEnabled(false);
                    holder.tweet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!TouchableMovementMethod.touched) {
                                // we need to manually set the background for click feedback because the spannable
                                // absorbs the click on the background
                                if (!holder.preventNextClick) {
                                    holder.background.getBackground().setState(new int[]{android.R.attr.state_pressed});
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.background.getBackground().setState(new int[]{android.R.attr.state_empty});
                                        }
                                    }, 25);
                                }

                                holder.background.performClick();
                            }
                        }
                    });

                    holder.tweet.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!TouchableMovementMethod.touched) {
                                holder.background.performLongClick();
                                holder.preventNextClick = true;
                            }
                            return false;
                        }
                    });

                    if (holder.retweeter.getVisibility() == View.VISIBLE) {
                        holder.retweeter.setSoundEffectsEnabled(false);
                        holder.retweeter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!TouchableMovementMethod.touched) {
                                    if (!holder.preventNextClick) {
                                        holder.background.getBackground().setState(new int[]{android.R.attr.state_pressed});
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder.background.getBackground().setState(new int[]{android.R.attr.state_empty});
                                            }
                                        }, 25);
                                    }

                                    holder.background.performClick();
                                }
                            }
                        });

                        holder.retweeter.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                if (!TouchableMovementMethod.touched) {
                                    holder.background.performLongClick();
                                    holder.preventNextClick = true;
                                }
                                return false;
                            }
                        });
                    }

                    TextUtils.linkifyText(context, holder.tweet, holder.background, true, otherUrl, false);
                    TextUtils.linkifyText(context, holder.retweeter, holder.background, true, "", false);
                }
            }
        }, 400);
        currHandler++;

        if (currHandler == 10) {
            currHandler = 0;
        }

//        if (openFirst && position == 0) {
//            holder.background.performClick();
//        }
    }

    public void bindView(final View view, Status status, int position) {

    }

    public void viewMoreClicked(){

    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }



    class VerticalViewHolder extends RecyclerView.ViewHolder{

        public TextView name;
        public ImageView profilePic;
        public TextView tweet;
        public TextView time;
        public TextView retweeter;
        public EditText reply;
        public ImageButton favorite;
        public ImageButton retweet;
        public TextView favCount;
        public TextView retweetCount;
        public LinearLayout expandArea;
        public ImageButton replyButton;
        public ImageView image;
        public TextView viewMoreText;
        public LinearLayout background;
        public ImageView playButton;
        public TextView screenTV;
        public ImageButton shareButton;
        public ImageButton quoteButton;
        public ImageView isAConversation;

        public long tweetId;
        public boolean isFavorited;
        public String screenName;
        public String picUrl;
        public String retweeterName;
        public String gifUrl;

        public boolean preventNextClick = false;


        public VerticalViewHolder(View itemView) {
            super(itemView);
           // textView = (TextView) itemView.findViewById(R.id.tv);
          //  v = inflater.inflate(layout, viewGroup, false);

            name = (TextView) itemView.findViewById(R.id.name);
            profilePic = (ImageView) itemView.findViewById(R.id.profile_pic);
            time = (TextView) itemView.findViewById(R.id.time);
            tweet = (TextView) itemView.findViewById(R.id.tweet);
            reply = (EditText) itemView.findViewById(R.id.reply);
            favorite = (ImageButton) itemView.findViewById(R.id.favorite);
            retweet = (ImageButton) itemView.findViewById(R.id.retweet);
            favCount = (TextView) itemView.findViewById(R.id.fav_count);
            retweetCount = (TextView) itemView.findViewById(R.id.retweet_count);
            expandArea = (LinearLayout) itemView.findViewById(R.id.expansion);
            replyButton = (ImageButton) itemView.findViewById(R.id.reply_button);
            image = (ImageView) itemView.findViewById(R.id.image);
            viewMoreText = (TextView) itemView.findViewById(R.id.view_more_text);
            retweeter = (TextView) itemView.findViewById(R.id.retweeter);
            background = (LinearLayout) itemView.findViewById(R.id.background);
            playButton = (NetworkedCacheableImageView) itemView.findViewById(R.id.play_button);
            screenTV = (TextView) itemView.findViewById(R.id.screenname);
            try {
                quoteButton = (ImageButton) itemView.findViewById(R.id.quote_button);
                shareButton = (ImageButton) itemView.findViewById(R.id.share_button);
            } catch (Exception x) {
                // theme was made before they were added
            }

            try {
                isAConversation = (ImageView) itemView.findViewById(R.id.is_a_conversation);
            } catch (Exception x) {

            }
            tweet.setTextSize(settings.textSize);
            name.setTextSize(settings.textSize + 4);
            screenTV.setTextSize(settings.textSize - 2);
            time.setTextSize(settings.textSize - 3);
            retweeter.setTextSize(settings.textSize - 3);
            favCount.setTextSize(settings.textSize + 1);
            retweetCount.setTextSize(settings.textSize + 1);
            reply.setTextSize(settings.textSize);

        }
    }

    public void removeExpansionWithAnimation(VerticalViewHolder holder) {
        //ExpansionAnimation expandAni = new ExpansionAnimation(holder.expandArea, 450);
        holder.expandArea.setVisibility(View.GONE);//startAnimation(expandAni);

        holder.retweetCount.setText(" -");
        holder.favCount.setText(" -");
        holder.reply.setText("");
        holder.retweet.clearColorFilter();
        holder.favorite.clearColorFilter();
    }

    public void removeExpansionNoAnimation(VerticalViewHolder holder) {
        //ExpansionAnimation expandAni = new ExpansionAnimation(holder.expandArea, 10);
        holder.expandArea.setVisibility(View.GONE);//startAnimation(expandAni);


        holder.retweetCount.setText(" -");
        holder.favCount.setText(" -");
        holder.reply.setText("");
        holder.retweet.clearColorFilter();
        holder.favorite.clearColorFilter();
    }



    public void addExpansion(final VerticalViewHolder holder, String screenname, String users, final String[] otherLinks, final String webpage, final long id){

        holder.retweet.setVisibility(View.VISIBLE);
        holder.retweetCount.setVisibility(View.VISIBLE);
        holder.favCount.setVisibility(View.VISIBLE);
        holder.favorite.setVisibility(View.VISIBLE);

        //holder.reply.setVisibility(View.GONE);
        try {
            holder.replyButton.setVisibility(View.GONE);
        } catch (Exception e) {

        }

        holder.screenName = screenname;

        // used to find the other names on a tweet... could be optimized i guess, but only run when button is pressed
        final String text = holder.tweet.getText().toString();
        String extraNames = "";

        if (text.contains("@")) {
            for (String s : users.split("  ")) {
                if (!s.equals(settings.myScreenName) && !extraNames.contains(s) && !s.equals(screenname)) {
                    extraNames += "@" + s + " ";
                }
            }
        }

        try {
            if (holder.retweeter.getVisibility() == View.VISIBLE && !extraNames.contains(holder.retweeterName)) {
                extraNames += "@" + holder.retweeterName + " ";
            }
        } catch (NullPointerException e) {

        }

        if (!screenname.equals(settings.myScreenName)) {
            holder.reply.setText("@" + screenname + " " + extraNames);
        } else {
            holder.reply.setText(extraNames);
        }

        holder.reply.setSelection(holder.reply.getText().length());

        if (holder.favCount.getText().toString().length() <= 2) {
            holder.favCount.setText(" -");
            holder.retweetCount.setText(" -");
        }

        //ExpansionAnimation expandAni = new ExpansionAnimation(holder.expandArea, 450);
        holder.expandArea.setVisibility(View.VISIBLE);//startAnimation(expandAni);

        getCounts(holder, id);

        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.isFavorited || !settings.crossAccActions) {
                    new VerticalAdapter.FavoriteStatus(holder, holder.tweetId, TimelineArrayAdapter.FavoriteStatus.TYPE_ACC_ONE).execute();
                } else if (settings.crossAccActions) {
                    // dialog for favoriting
                    String[] options = new String[3];

                    options[0] = "@" + settings.myScreenName;
                    options[1] = "@" + settings.secondScreenName;
                    options[2] = context.getString(R.string.both_accounts);

                    new AlertDialog.Builder(context)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int item) {
                                    new VerticalAdapter.FavoriteStatus(holder, holder.tweetId, item + 1).execute();
                                }
                            })
                            .create().show();
                }
            }
        });

        holder.retweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!settings.crossAccActions) {
                    new VerticalAdapter.RetweetStatus(holder, holder.tweetId, TimelineArrayAdapter.FavoriteStatus.TYPE_ACC_ONE).execute();
                } else {
                    // dialog for favoriting
                    String[] options = new String[3];

                    options[0] = "@" + settings.myScreenName;
                    options[1] = "@" + settings.secondScreenName;
                    options[2] = context.getString(R.string.both_accounts);

                    new AlertDialog.Builder(context)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int item) {
                                    new VerticalAdapter.RetweetStatus(holder, holder.tweetId, item + 1).execute();
                                }
                            })
                            .create().show();
                }
            }
        });

        holder.retweet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.remove_retweet))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new RemoveRetweet(holder.tweetId).execute();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
                return false;
            }

            class RemoveRetweet extends AsyncTask<String, Void, Boolean> {

                private long tweetId;

                public RemoveRetweet(long tweetId) {
                    this.tweetId = tweetId;
                }

                protected void onPreExecute() {
                    holder.retweet.clearColorFilter();

                    Toast.makeText(context, context.getResources().getString(R.string.removing_retweet), Toast.LENGTH_SHORT).show();
                }

                protected Boolean doInBackground(String... urls) {
                    try {
                        Twitter twitter =  Utils.getTwitter(context, settings);
                        ResponseList<twitter4j.Status> retweets = twitter.getRetweets(tweetId);
                        for (twitter4j.Status retweet : retweets) {
                            if(retweet.getUser().getId() == settings.myId)
                                twitter.destroyStatus(retweet.getId());
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                protected void onPostExecute(Boolean deleted) {
                    try {
                        if (deleted) {
                            Toast.makeText(context, context.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // user has gone away from the window
                    }
                }
            }
        });

        holder.reply.requestFocus();
        removeKeyboard(holder);
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent compose = new Intent(context, ComposeActivity.class);
                String string = holder.reply.getText().toString();
                try {
                    compose.putExtra("user", string.substring(0, string.length() - 1));
                } catch (Exception e) {

                }
                compose.putExtra("id", holder.tweetId);
                compose.putExtra("reply_to_text", "@" + holder.screenName + ": " + text);
                context.startActivity(compose);

                removeExpansionWithAnimation(holder);
            }
        });

        if (holder.replyButton != null) {
            holder.replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new VerticalAdapter.ReplyToStatus(holder, holder.tweetId).execute();
                }
            });
        }


        final String name = screenname;

        try {
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String text = holder.tweet.getText().toString();

                    text = TweetLinkUtils.removeColorHtml(text, settings);
                    text = restoreLinks(text);

                    switch (settings.quoteStyle) {
                        case AppSettings.QUOTE_STYLE_TWITTER:
                            text = " " + "https://twitter.com/" + name + "/status/" + id;
                            break;
                        case AppSettings.QUOTE_STYLE_TALON:
                            text = "\"@" + name + ": " + text + "\" ";
                            break;
                        case AppSettings.QUOTE_STYLE_RT:
                            text =  " RT @" + name + ": " + text + "  \n- RT'd using #BlackTweeterApp -";
                            break;
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.menu_share)));
                }

                public String restoreLinks(String text) {
                    String full = text;

                    String[] split = text.split("\\s");
                    String[] otherLink = new String[otherLinks.length];

                    for (int i = 0; i < otherLinks.length; i++) {
                        otherLink[i] = "" + otherLinks[i];
                    }

                    for (String s : otherLink) {
                        Log.v("talon_links", ":" + s + ":");
                    }

                    boolean changed = false;
                    int otherIndex = 0;

                    if (otherLink.length > 0) {
                        for (int i = 0; i < split.length; i++) {
                            String s = split[i];

                            //if (Patterns.WEB_URL.matcher(s).find()) { // we know the link is cut off
                            if (Patterns.WEB_URL.matcher(s).find()) { // we know the link is cut off
                                String f = s.replace("...", "").replace("http", "");

                                f = stripTrailingPeriods(f);

                                try {
                                    if (otherIndex < otherLinks.length) {
                                        if (otherLink[otherIndex].substring(otherLink[otherIndex].length() - 1, otherLink[otherIndex].length()).equals("/")) {
                                            otherLink[otherIndex] = otherLink[otherIndex].substring(0, otherLink[otherIndex].length() - 1);
                                        }
                                        f = otherLink[otherIndex].replace("http://", "").replace("https://", "").replace("www.", "");
                                        otherLink[otherIndex] = "";
                                        otherIndex++;

                                        changed = true;
                                    }
                                } catch (Exception e) {

                                }

                                if (changed) {
                                    split[i] = f;
                                } else {
                                    split[i] = s;
                                }
                            } else {
                                split[i] = s;
                            }

                        }
                    }

                    if (!webpage.equals("")) {
                        for (int i = split.length - 1; i >= 0; i--) {
                            String s = split[i];
                            if (Patterns.WEB_URL.matcher(s).find()) {
                                String replace = otherLinks[otherLinks.length - 1];
                                if (replace.replace(" ", "").equals("")) {
                                    replace = webpage;
                                }
                                split[i] = replace;
                                changed = true;
                                break;
                            }
                        }
                    }

                    if(changed) {
                        full = "";
                        for (String p : split) {
                            full += p + " ";
                        }

                        full = full.substring(0, full.length() - 1);
                    }

                    return full;
                }

                private String stripTrailingPeriods(String url) {
                    try {
                        if (url.substring(url.length() - 1, url.length()).equals(".")) {
                            return stripTrailingPeriods(url.substring(0, url.length() - 1));
                        } else {
                            return url;
                        }
                    } catch (Exception e) {
                        return url;
                    }
                }
            });


            holder.quoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(context, ComposeActivity.class);
                    intent.setType("text/plain");
                    String text = holder.tweet.getText().toString();

                    text = TweetLinkUtils.removeColorHtml(text, settings);
                    text = restoreLinks(text);

                    switch (settings.quoteStyle) {
                        case AppSettings.QUOTE_STYLE_TWITTER:
                            text = " " + "https://twitter.com/" + name + "/status/" + id;
                            break;
                        case AppSettings.QUOTE_STYLE_TALON:
                            text = "\"@" + name + ": " + text + "\" ";
                            break;
                        case AppSettings.QUOTE_STYLE_RT:
                            text = text = " RT @" + name + ": " + text + " \n- RT'd using #BlackTweeterApp -";
                            break;
                    }
                    intent.putExtra("user", text);
                    intent.putExtra("id", id);

                    context.startActivity(intent);
                }

                public String restoreLinks(String text) {
                    String full = text;

                    String[] split = text.split("\\s");
                    String[] otherLink = new String[otherLinks.length];

                    for (int i = 0; i < otherLinks.length; i++) {
                        otherLink[i] = "" + otherLinks[i];
                    }

                    boolean changed = false;

                    if (otherLink.length > 0) {
                        for (int i = 0; i < split.length; i++) {
                            String s = split[i];

                            //if (Patterns.WEB_URL.matcher(s).find()) { // we know the link is cut off
                            if (s.contains("...")) { // we know the link is cut off
                                String f = s.replace("...", "").replace("http", "");

                                Log.v("talon_links", ":" + s + ":");

                                for (int x = 0; x < otherLink.length; x++) {
                                    if (otherLink[x].toLowerCase().contains(f.toLowerCase())) {
                                        changed = true;
                                        // for some reason it wouldn't match the last "/" on a url and it was stopping it from opening
                                        try {
                                            if (otherLink[x].substring(otherLink[x].length() - 1, otherLink[x].length()).equals("/")) {
                                                otherLink[x] = otherLink[x].substring(0, otherLink[x].length() - 1);
                                            }
                                            f = otherLink[x].replace("http://", "").replace("https://", "").replace("www.", "");
                                            otherLink[x] = "";
                                        } catch (Exception e) {

                                        }
                                        break;
                                    }
                                }

                                if (changed) {
                                    split[i] = f;
                                } else {
                                    split[i] = s;
                                }
                            } else {
                                split[i] = s;
                            }

                        }
                    }

                    if (!webpage.equals("")) {
                        for (int i = 0; i < split.length; i++) {
                            String s = split[i];
                            s = s.replace("...", "");

                            if (Patterns.WEB_URL.matcher(s).find() && (s.startsWith("t.co/") || s.contains("twitter.com/"))) { // we know the link is cut off
                                String replace = otherLinks[otherLinks.length - 1];
                                if (replace.replace(" ", "").equals("")) {
                                    replace = webpage;
                                }
                                split[i] = replace;
                                changed = true;
                            }
                        }
                    }



                    if(changed) {
                        full = "";
                        for (String p : split) {
                            full += p + " ";
                        }

                        full = full.substring(0, full.length() - 1);
                    }

                    return full;
                }
            });
        } catch (Exception e) {
            // theme made before these were implemented
        }
    }

    public void removeKeyboard(VerticalViewHolder holder) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(holder.reply.getWindowToken(), 0);
    }

    public void getFavoriteCount(final VerticalViewHolder holder, final long tweetId) {

        Thread getCount = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);
                    final Status status;
                    if (holder.retweeter.getVisibility() != View.GONE) {
                        status = twitter.showStatus(holder.tweetId).getRetweetedStatus();
                    } else {
                        status = twitter.showStatus(tweetId);
                    }

                    if (status != null && holder.tweetId == tweetId) {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.favCount.setText(" " + status.getFavoriteCount());

                                if (status.isFavorited()) {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    if (!settings.addonTheme) {
                                        holder.favorite.setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        holder.favorite.setColorFilter(settings.accentInt);
                                    }

                                    holder.favorite.setImageDrawable(context.getResources().getDrawable(resource));
                                    holder.isFavorited = true;
                                } else {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notFavoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    holder.favorite.setImageDrawable(context.getResources().getDrawable(resource));
                                    holder.isFavorited = false;

                                    holder.favorite.clearColorFilter();
                                }
                            }
                        });
                    }

                } catch (Exception e) {

                }
            }
        });

        getCount.setPriority(7);
        getCount.start();
    }

    public void getCounts(final VerticalViewHolder holder, final long tweetId) {

        Thread getCount = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);
                    final Status status;

                    status = twitter.showStatus(tweetId);

                    if (status != null) {
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.favCount.setText(" " + status.getFavoriteCount());
                                holder.retweetCount.setText(" " + status.getRetweetCount());

                                if (status.isFavorited()) {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.favoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    if (!settings.addonTheme) {
                                        holder.favorite.setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        holder.favorite.setColorFilter(settings.accentInt);
                                    }

                                    holder.favorite.setImageDrawable(context.getResources().getDrawable(resource));
                                    holder.isFavorited = true;
                                } else {
                                    TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.notFavoritedButton});
                                    int resource = a.getResourceId(0, 0);
                                    a.recycle();

                                    holder.favorite.setImageDrawable(context.getResources().getDrawable(resource));
                                    holder.isFavorited = false;

                                    holder.favorite.clearColorFilter();
                                }

                                if (status.isRetweetedByMe()) {
                                    if (!settings.addonTheme) {
                                        holder.retweet.setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        holder.retweet.setColorFilter(settings.accentInt);
                                    }
                                } else {
                                    holder.retweet.clearColorFilter();
                                }
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getCount.setPriority(7);
        getCount.start();
    }

    public void getRetweetCount(final VerticalViewHolder holder, final long tweetId) {

        Thread getRetweetCount = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter =  Utils.getTwitter(context, settings);
                    twitter4j.Status status = twitter.showStatus(holder.tweetId);
                    final boolean retweetedByMe = status.isRetweetedByMe();
                    final String count = "" + status.getRetweetCount();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tweetId == holder.tweetId) {
                                if (retweetedByMe) {
                                    if (!settings.addonTheme) {
                                        holder.retweet.setColorFilter(context.getResources().getColor(R.color.app_color));
                                    } else {
                                        holder.retweet.setColorFilter(settings.accentInt);
                                    }
                                } else {
                                    holder.retweet.clearColorFilter();
                                }
                                if (count != null) {
                                    holder.retweetCount.setText(" " + count);
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getRetweetCount.setPriority(7);
        getRetweetCount.start();
    }

    class FavoriteStatus extends AsyncTask<String, Void, String> {

        public static final int TYPE_ACC_ONE = 1;
        public static final int TYPE_ACC_TWO = 2;
        public static final int TYPE_BOTH_ACC = 3;

        private VerticalViewHolder holder;
        private long tweetId;
        private int type;

        public FavoriteStatus(VerticalViewHolder holder, long tweetId, int type) {
            this.holder = holder;
            this.tweetId = tweetId;
            this.type = type;
        }

        protected void onPreExecute() {
            if (!holder.isFavorited || type == TYPE_ACC_TWO || type == TYPE_BOTH_ACC) {
                Toast.makeText(context, context.getResources().getString(R.string.favoriting_status), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.removing_favorite), Toast.LENGTH_SHORT).show();
            }
        }

        protected String doInBackground(String... urls) {
            try {
                Twitter twitter = null;
                Twitter secTwitter = null;
                if (type == TYPE_ACC_ONE) {
                    twitter = Utils.getTwitter(context, settings);
                } else if (type == TYPE_ACC_TWO) {
                    secTwitter = Utils.getSecondTwitter(context);
                } else {
                    twitter = Utils.getTwitter(context, settings);
                    secTwitter = Utils.getSecondTwitter(context);
                }

                if (holder.isFavorited && twitter != null) {
                    twitter.destroyFavorite(tweetId);
                } else if (twitter != null) {
                    try {
                        twitter.createFavorite(tweetId);
                    } catch (TwitterException e) {
                        // already been favorited by this account
                    }
                }

                if (secTwitter != null) {
                    secTwitter.createFavorite(tweetId);
                }

                return null;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String count) {
            Toast.makeText(context, context.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
            getFavoriteCount(holder, tweetId);
        }
    }

    class RetweetStatus extends AsyncTask<String, Void, String> {

        public static final int TYPE_ACC_ONE = 1;
        public static final int TYPE_ACC_TWO = 2;
        public static final int TYPE_BOTH_ACC = 3;

        private VerticalViewHolder holder;
        private long tweetId;
        private int type;

        public RetweetStatus(VerticalViewHolder holder, long tweetId, int type) {
            this.holder = holder;
            this.tweetId = tweetId;
            this.type = type;
        }

        protected void onPreExecute() {
            Toast.makeText(context, context.getResources().getString(R.string.retweeting_status), Toast.LENGTH_SHORT).show();
        }

        protected String doInBackground(String... urls) {
            try {
                Twitter twitter;
                Twitter secTwitter = null;
                if (type == TYPE_ACC_ONE) {
                    twitter = Utils.getTwitter(context, settings);
                } else if (type == TYPE_ACC_TWO) {
                    twitter = Utils.getSecondTwitter(context);
                } else {
                    twitter = Utils.getTwitter(context, settings);
                    secTwitter = Utils.getSecondTwitter(context);
                }

                try {
                    twitter.retweetStatus(tweetId);
                } catch (TwitterException e) {
                    // already been retweeted by this account
                }

                if (secTwitter != null) {
                    secTwitter.retweetStatus(tweetId);
                }

                return null;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String count) {
            Toast.makeText(context, context.getResources().getString(R.string.retweet_success), Toast.LENGTH_SHORT).show();
            getRetweetCount(holder, tweetId);
        }
    }

    class ReplyToStatus extends AsyncTask<String, Void, String> {

        private VerticalViewHolder holder;
        private long tweetId;

        public ReplyToStatus(VerticalViewHolder holder, long tweetId) {
            this.holder = holder;
            this.tweetId = tweetId;
        }

        protected String doInBackground(String... urls) {
            try {
                Twitter twitter =  Utils.getTwitter(context, settings);


                twitter4j.StatusUpdate reply = new twitter4j.StatusUpdate(holder.reply.getText().toString());
                reply.setInReplyToStatusId(tweetId);

                twitter.updateStatus(reply);

                return null;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String count) {
            removeExpansionWithAnimation(holder);
            removeKeyboard(holder);
        }
    }

    // used to place images on the timeline
    public static ImageUrlAsyncTask mCurrentTask;

    public void loadImage(Context context, final VerticalViewHolder holder, final String url, BitmapLruCache mCache, final long tweetId) {
        // First check whether there's already a task running, if so cancel it
        /*if (null != mCurrentTask) {
            mCurrentTask.cancel(true);
        }*/

        if (url == null) {
            return;
        }

        BitmapDrawable wrapper = mCache.getFromMemoryCache(url);

        if (null != wrapper && holder.image.getVisibility() != View.GONE) {
            // The cache has it, so just display it
            holder.image.setImageDrawable(wrapper);
            Log.d("ben!", "pic url 1542: " + url);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);

            holder.image.startAnimation(fadeInAnimation);
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            holder.image.setImageDrawable(null);

            mCurrentTask = new ImageUrlAsyncTask(context, holder, mCache, tweetId);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    SDK11.executeOnThreadPool(mCurrentTask, url);
                } else {
                    mCurrentTask.execute(url);
                }
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

        }
    }

    private static class ImageUrlAsyncTask
            extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private BitmapLruCache mCache;
        private Context context;
        private VerticalViewHolder holder;
        private long id;

        ImageUrlAsyncTask(Context context, VerticalViewHolder holder, BitmapLruCache cache, long tweetId) {
            this.context = context;
            mCache = cache;
            this.holder = holder;
            this.id = tweetId;
        }

        @Override
        protected CacheableBitmapDrawable doInBackground(String... params) {
            try {
                if (holder.tweetId != id) {
                    return null;
                }
                String url = params[0];

                if (url.contains("twitpic")) {
                    try {
                        URL address = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) address.openConnection(Proxy.NO_PROXY);
                        connection.setConnectTimeout(1000);
                        connection.setInstanceFollowRedirects(false);
                        connection.setReadTimeout(1000);
                        connection.connect();
                        String expandedURL = connection.getHeaderField("Location");
                        if(expandedURL != null) {
                            url = expandedURL;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url.contains("p.twipple.jp")) {

                }

                // Now we're not on the main thread we can check all caches
                CacheableBitmapDrawable result;

                try {
                    result = mCache.get(url, null);
                } catch (Exception e) {
                    result = null;
                }

                if (null == result) {

                    if (!url.contains(" ")) {
                        // The bitmap isn't cached so download from the web
                        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                        InputStream is = new BufferedInputStream(conn.getInputStream());

                        Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

                        try {
                            is.close();
                        } catch (Exception e) {

                        }
                        try {
                            conn.disconnect();
                        } catch (Exception e) {

                        }

                        // Add to cache
                        try {
                            result = mCache.put(url, b);
                        } catch (Exception e) {
                            result = null;
                        }

                    } else {
                        // there are multiple pictures... uh oh
                        String[] pics = url.split(" ");
                        Bitmap[] bitmaps = new Bitmap[pics.length];

                        // need to download all of them, then combine them
                        for (int i = 0; i < pics.length; i++) {
                            String s = pics[i];

                            // The bitmap isn't cached so download from the web
                            HttpURLConnection conn = (HttpURLConnection) new URL(s).openConnection();
                            InputStream is = new BufferedInputStream(conn.getInputStream());

                            Bitmap b = decodeSampledBitmapFromResourceMemOpt(is, 500, 500);

                            try {
                                is.close();
                            } catch (Exception e) {

                            }
                            try {
                                conn.disconnect();
                            } catch (Exception e) {

                            }

                            // Add to cache
                            try {
                                mCache.put(s, b);

                                // throw it into our bitmap array for later
                                bitmaps[i] = b;
                            } catch (Exception e) {
                                result = null;
                            }
                        }

                        // now that we have all of them, we need to put them together
                        Bitmap combined = ImageUtils.combineBitmaps(context, bitmaps);

                        try {
                            result = mCache.put(url, combined);
                        } catch (Exception e) {

                        }
                    }
                }

                return result;

            } catch (IOException e) {
                Log.e("ImageUrlAsyncTask", e.toString());
            } catch (OutOfMemoryError e) {
                Log.v("ImageUrlAsyncTask", "Out of memory error here");
            }

            return null;
        }

        public Bitmap decodeSampledBitmapFromResourceMemOpt(
                InputStream inputStream, int reqWidth, int reqHeight) {

            byte[] byteArr = new byte[0];
            byte[] buffer = new byte[1024];
            int len;
            int count = 0;

            try {
                while ((len = inputStream.read(buffer)) > -1) {
                    if (len != 0) {
                        if (count + len > byteArr.length) {
                            byte[] newbuf = new byte[(count + len) * 2];
                            System.arraycopy(byteArr, 0, newbuf, 0, count);
                            byteArr = newbuf;
                        }

                        System.arraycopy(buffer, 0, byteArr, count, len);
                        count += len;
                    }
                }

                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(byteArr, 0, count, options);

                options.inSampleSize = calculateInSampleSize(options, reqWidth,
                        reqHeight);
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        public static int calculateInSampleSize(BitmapFactory.Options opt, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = opt.outHeight;
            final int width = opt.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        @Override
        protected void onPostExecute(CacheableBitmapDrawable result) {
            super.onPostExecute(result);

            try {
                if (result != null && holder.tweetId == id) {
                    // holder.image.setImageDrawable(result);
                    Log.d("ben!", "pic url 1773: " + result.getUrl());
                    holder.image.setVisibility(View.VISIBLE);

                    ColorDrawable myTransparent = new ColorDrawable(context.getResources().getColor(R.color.pressed_app_color));
                    if (result.getUrl().contains(" ")){
                        Log.d(TAG, "onPostExecute: multiple images");

                        String picUrlArray[] = result.getUrl().split(" ");
                        String firstPicUrl = picUrlArray[0];

                      //  int myInt = (int) (MainActivity.screenWidth * 0.5);
                       // Log.d("ben!", "image width  " + myInt);
                      //  holder.image.getLayoutParams().width = myInt;

                        Picasso.with(context).load(firstPicUrl).into(holder.image);
                        holder.viewMoreText.setVisibility(View.VISIBLE);



                    }  else {
                        Picasso.with(context).load(result.getUrl()).into(holder.image);

                        Animation fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in_fast);

                        if (holder.tweetId == id) {
                            holder.image.startAnimation(fadeInAnimation);
                           // holder.image.setAlpha((float) .5);
                        }
                    }
                }

            } catch (Exception e) {

            }
        }
    }

}
