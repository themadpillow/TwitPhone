package com.the_mad_pillow.twitphone;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import io.skyway.Peer.Browser.MediaConstraints;
import io.skyway.Peer.Browser.MediaStream;
import io.skyway.Peer.Browser.Navigator;
import io.skyway.Peer.CallOption;
import io.skyway.Peer.MediaConnection;
import io.skyway.Peer.OnCallback;
import io.skyway.Peer.Peer;
import io.skyway.Peer.PeerOption;

public class MyPeer {
    //debug
    private final String TAG = getClass().getSimpleName();

    private MainActivity activity;
    private Peer peer;
    private MediaConnection connection;

    MyPeer(MainActivity activity, PeerOption options) {
        peer = new Peer(activity, options);
        this.activity = activity;
    }

    MyPeer(MainActivity activity, String peerId, PeerOption options) {
        peer = new Peer(activity, peerId, options);
        this.activity = activity;
    }

    //受信設定
    public void init() {
        peer.on(Peer.PeerEventEnum.CALL, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "CALL Event is Received");
                if (o instanceof MediaConnection) {
                    MediaConnection connection = (MediaConnection) o;

                    if (MyPeer.this.connection != null) {
                        Log.d(TAG, "connection is already created");
                        connection.close();
                        return;
                    }

                    MediaStream stream = getMediaStream();
                    connection.answer(stream);
                    setConnectionCallback(connection);
                    MyPeer.this.connection = connection;
                    Log.d(TAG, "CALL Event is Received and Set");
                }
            }
        });
    }

    public Peer getPeer() {
        return peer;
    }

    public void on(Peer.PeerEventEnum peerEventEnum, OnCallback callback) {
        peer.on(peerEventEnum, callback);
    }

    /**
     * オンラインのpeerIDListのリロード
     */
    public void refreshPeerList() {
        peer.listAllPeers(new OnCallback() {
            @Override
            public void onCallback(Object object) {
                if (object instanceof JSONArray) {
                    JSONArray array = (JSONArray) object;
                    activity.getIdList().clear();
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            String id = array.getString(i);
                            //自分のIDはListに表示しない
                            if (!id.equals(activity.getMyTwitter().getScreenName())) {
                                activity.getIdList().add(id);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.getAdapter().notifyDataSetChanged();
                            activity.getSwipeRefreshLayout().setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public void call(String peerId) {
        Log.d(TAG, "Calling to id:" + peerId);
        if (peer == null) {
            Log.i(TAG, "Call but peer is null");
            return;
        }

        if (peer.isDestroyed() || peer.isDisconnected()) {
            Log.i(TAG, "Call but peer is not active");
            return;
        }

        if (connection != null) {
            Log.d(TAG, "Call but connection is already created");
            return;
        }

        MediaStream stream = getMediaStream();

        if (stream == null) {
            Log.d(TAG, "Call but media stream is null");
            return;
        }
        CallOption option = new CallOption();
        option.metadata = BuildConfig.SKYWAY_HOST;
        MediaConnection mediaConnection = peer.call(peerId, stream, option);

        if (mediaConnection == null) {
            Log.d(TAG, "Call but MediaConnection is null");
            return;
        }

        setConnectionCallback(mediaConnection);
        connection = mediaConnection;
        Log.d(TAG, "connection started!");
    }

    private MediaStream getMediaStream() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.videoFlag = false;
        constraints.audioFlag = true;
        return Navigator.getUserMedia(constraints);
    }

    private void closeConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
            Log.d(TAG, "Connection is Closed");
        }
    }

    private void setConnectionCallback(MediaConnection connection) {
        connection.on(MediaConnection.MediaEventEnum.CLOSE, new OnCallback() {
            @Override
            public void onCallback(Object o) {
                Log.d(TAG, "Close Event is Received");
                closeConnection();
            }
        });
    }

    public boolean isDestroyed() {
        return peer.isDestroyed();
    }

    public boolean isDisconnected() {
        return peer.isDisconnected();
    }
}
