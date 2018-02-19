package com.sv15.youtubelistview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.prof.youtubeparser.Parser;
import com.prof.youtubeparser.models.videos.Main;
import com.prof.youtubeparser.models.videos.Video;

import java.util.ArrayList;

public class MainActivity extends YouTubeBaseActivity {
    private ConnectivityManager connectivityManager;
    private RecyclerView mRecyclerView;
    private VideoAdapter vAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar progressBar;
    private int totalElement;
    private String nextToken;
    private final String CHANNEL_ID = "CHANNEL ID";
    //TODO: delete
    private final String API_KEY = "Your API Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout = findViewById(R.id.container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.canChildScrollUp();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                vAdapter.clearData();
                vAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(true);
                loadVideo();
            }
        });

        if (!isNetworkAvailable()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_message)
                    .setTitle(R.string.alert_title)
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_positive,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(MainActivity.this, "Finished", Toast.LENGTH_SHORT).show();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();

        } else if (isNetworkAvailable()) {
            loadVideo();
        }
    }
    public boolean isNetworkAvailable() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadVideo() {
        final ProgressDialog progressDialog= new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        Parser parser = new Parser();
        String url = parser.generateRequest(CHANNEL_ID, 30, Parser.ORDER_DATE, API_KEY);

        parser.execute(url);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Video> list, String nextPageToken) {
                //list is an ArrayList with all video's item
                //set the adapter to recycler view
                progressDialog.dismiss();
                vAdapter = new VideoAdapter(list, R.layout.card_video, MainActivity.this);
                mRecyclerView.setAdapter(vAdapter);
                totalElement = vAdapter.getItemCount();
                nextToken = nextPageToken;
                vAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError() {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Error while loading data. Please retry", Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {

        super.onResume();
        if (vAdapter != null)
            vAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (vAdapter != null)
            vAdapter.clearData();
    }
}
