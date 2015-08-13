package com.xiameng.wifip2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class CheckWifi {
	private static final String TAG = "FindPeers";
	private Context context;
	private WifiManager mWifiManager;

	public CheckWifi(Context context) {
		this.context = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public boolean IsWifiOpen() {
		if (mWifiManager != null) {
			return mWifiManager.isWifiEnabled();
		} else {
			return false;
		}
	}

	public void OpenWifi() {
		mWifiManager.setWifiEnabled(true);
	}
}
