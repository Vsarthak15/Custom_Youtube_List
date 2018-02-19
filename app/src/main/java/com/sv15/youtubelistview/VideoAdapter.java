package com.sv15.youtubelistview;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.prof.youtubeparser.VideoStats;
import com.prof.youtubeparser.models.stats.Statistics;
import com.prof.youtubeparser.models.videos.Video;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private ArrayList<Video> videos;
    private int rowLayout;
    private Context mContext;

    private final String API_KEY = "Your API key";


    public VideoAdapter(ArrayList<Video> list, int rowLayout, Context context) {
        this.videos = list;
        this.rowLayout = rowLayout;
        this.mContext = context;
    }

    public void clearData(){
        if (videos != null)
            videos.clear();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)  {

        final Video currentVideo = videos.get(position);

        String pubDateString = currentVideo.getDate();
        final String videoTitle = currentVideo.getTitle();

        //retrieve video link
        final String videoId = currentVideo.getVideoId();
        final String url="<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+videoId+"\" frameborder=\"0\" allowfullscreen></iframe>";

        viewHolder.title.setText(currentVideo.getTitle());
        viewHolder.pubDate.setText(pubDateString);
        viewHolder.webView.loadData(url,"text/html","utf-8");

        //show statistic of the selected video
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                VideoStats videoStats = new VideoStats();
                String url = videoStats.generateStatsRequest(videoId, API_KEY);
                videoStats.execute(url);
                videoStats.onFinish(new VideoStats.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(Statistics stats) {

                        String body = "Views: " + stats.getViewCount() + "\n" +
                                "Like: " + stats.getLikeCount() + "\n" +
                                "Dislike: " + stats.getDislikeCount() + "\n" +
                                "Number of comment: " + stats.getCommentCount() + "\n";

                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                        dialogBuilder.setTitle("Stats for: \"" + videoTitle + "\"");
                        dialogBuilder.setMessage(body);
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.setNegativeButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        dialogBuilder.show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(mContext, "Unable to get statistic for this video. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos == null ? 0 : videos.size();
    }

    public ArrayList<Video> getList() {
        return videos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView pubDate;
        WebView webView;

        public ViewHolder(View itemView) {

            super(itemView);
            title = itemView.findViewById(R.id.title);
            pubDate = itemView.findViewById(R.id.pubDate);
            webView = itemView.findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebChromeClient(new WebChromeClient(){

            });
        }
    }
}
