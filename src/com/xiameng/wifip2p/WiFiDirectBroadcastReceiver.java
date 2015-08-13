package com.xiameng.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	private WifiP2pManager mManager;
	private Channel mChannel;
	private MainActivity mActivity;
	
	
	public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
			MainActivity activity) {
		super();
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if WiFi is enabled and notify appropriate activity
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// WiFi P2P is enabled
				Log.d(mActivity.getClass().getName(), "WIFI�ɹ�����");
			} else {
				// WiFi P2P is not enabled
				Log.d(mActivity.getClass().getName(), "WIFI�ر�");
			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			if(mManager!=null){ 
				mManager.requestPeers(mChannel, mActivity);
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			// Respond to new connection or disconnections
			if(mManager==null){
				return;
			}
			NetworkInfo networkInfo=(NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()){
				mManager.requestConnectionInfo(mChannel, (ConnectionInfoListener)mActivity); 
			}else{
				mActivity.resetData(); 
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			// Respond to this device's wifi state changing
		}

	}

}
