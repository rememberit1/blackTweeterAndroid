package com.blacktweeter.android.twitter.activities.tweet_viewer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.utils.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class VideoFragment extends Fragment {

    public Context context;
    public String tweetUrl;
    public String videoUrl;

    public VideoView video;
    public LinearLayout spinner;

    private View download;

    public VideoFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        tweetUrl = getArguments().getString("url");

        View layout = inflater.inflate(R.layout.gif_player, null, false);
        video = (VideoView) layout.findViewById(R.id.gif);
        spinner = (LinearLayout) layout.findViewById(R.id.spinner);

        if (tweetUrl.contains("video.twimg")) {
            MediaController mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(video);
            video.setMediaController(mediaController);
        }

        download = layout.findViewById(R.id.download_button);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo();
            }
        });
        download.setEnabled(false);

        getGif();

        return layout;
    }

    public void getGif() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (tweetUrl.contains("vine.co")) {
                    // have to get the html from the page and parse the video from there.

                    videoUrl = getVineLink();
                } else if (tweetUrl.contains("/photo/1") && tweetUrl.contains("twitter.com/")) {
                    // this is before it was added to the api.
                    // finds the video from the HTML on twitters website.

                    videoUrl = getGifLink();
                } else {
                    videoUrl = tweetUrl.replace(".png", ".mp4").replace(".jpg",".mp4").replace(".jpeg", ".mp4");
                }

                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (videoUrl != null) {
                                    final Uri videoUri = Uri.parse(videoUrl);

                                    video.setVideoURI(videoUri);
                                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            spinner.setVisibility(View.GONE);
                                            video.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
                                        }
                                    });

                                    video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            mp.seekTo(0);
                                            mp.start();
                                        }
                                    });

                                    video.start();

                                    download.setEnabled(true);
                                } else {
                                    Toast.makeText(getActivity(), R.string.error_gif, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                // fragment not attached
                            }
                        }
                    });
                } catch (Exception e) {
                    // activity not attached
                }

            }
        }).start();
    }

    public Document getDoc() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet((tweetUrl.contains("http") ? "" : "https://") + tweetUrl);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            String docHtml = sb.toString();

            is.close();

            return Jsoup.parse(docHtml);
        } catch (Exception e) {
            return null;
        }
    }

    public String getGifLink() {
        try {
            Document doc = getDoc();

            if(doc != null) {
                Elements elements = doc.getElementsByAttributeValue("class", "animated-gif");

                for (Element e : elements) {
                    for (Element x : e.getAllElements()) {
                        if (x.nodeName().contains("source")) {
                            return x.attr("video-src");
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getVineLink() {
        try {
            Document doc = getDoc();

            if(doc != null) {
                Elements elements = doc.getElementsByAttributeValue("property", "twitter:player:stream");

                for (Element e : elements) {
                    return e.attr("content");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }

    private void downloadVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setTicker(context.getResources().getString(R.string.downloading) + "...")
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setContentText("Saving video...")
                                    .setProgress(100, 100, true)
                                    .setOngoing(true);

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(6, mBuilder.build());

                    Uri uri = IOUtils.saveVideo(videoUrl);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "video/*");

                    PendingIntent pending = PendingIntent.getActivity(context, 91, intent, 0);

                    mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setContentIntent(pending)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setContentText("Saved video!");

                    mNotificationManager.notify(6, mBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_stat_icon)
                                    .setTicker(context.getResources().getString(R.string.error) + "...")
                                    .setContentTitle(context.getResources().getString(R.string.app_name))
                                    .setContentText(context.getResources().getString(R.string.error) + "...")
                                    .setProgress(0, 100, true);

                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(6, mBuilder.build());
                }
            }
        }).start();
    }
}
