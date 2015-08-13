/**
 * 2015byxiameng
 * wifip2p学习
 * 
 * 
 */
package com.xiameng.wifip2p;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiameng.animview.MetaballView;

public class MainActivity extends Activity implements PeerListListener ,ConnectionInfoListener{
	private MetaballView metaballView;
	/*
	 * WifiP2p的基本前提对象
	 */
	private IntentFilter mIntentFilter;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiP2pInfo info;
	/*
	 * WifiP2p设备信息
	 */
	private BroadcastReceiver mReceiver;
	private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>(); // 用来存放发现的节点
	private static WifiP2pDevice device;
	
	/*
	 * UI组件
	 */
	private ListView mDevicesList;
	private WifiPeerListAdapter adapter;
	private Button sendBt;
	private CheckWifi fp;
	private boolean isWifiOpen = false;
	private boolean isConnected=false;
	
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		metaballView = (MetaballView) this.findViewById(R.id.metaball);
		sendBt=(Button) findViewById(R.id.bt_send_peer);
		// 得到一个广播过滤器
		mIntentFilter = new IntentFilter();
		// 指示一个WI-FI P2P 状态的变化过滤器
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// 指示一个在可用的PEER端的List里面的一个变化
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// 指示一个在连接状态的P2P端的状态的变化
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// 指示一个设备的细节发生了变化
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		// 得到一个WifiP2pManager实例
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

		// 实现WifiP2pManager的initialize方法得到WifiP2pManager.Channel用来APP与Wi-Fi
		// P2P框架的连接
		mChannel = mManager.initialize(this, getMainLooper(), null);

		
		mDevicesList = (ListView) findViewById(R.id.devices_lv);
		fp = new CheckWifi(MainActivity.this);
		isWifiOpen = fp.IsWifiOpen();
		if (!isWifiOpen) {
			fp.OpenWifi();
		}
		setAdapter();
		if (mManager != null && isWifiOpen) {
			mManager.discoverPeers(mChannel,
					new WifiP2pManager.ActionListener() {

						@Override
						public void onSuccess() {
							Log.d(MainActivity.this.getClass().getName(),
									"检测P2P进程成功");
							metaballView.setVisibility(View.VISIBLE); 
						}

						@Override
						public void onFailure(int reason) {
							Log.d(MainActivity.this.getClass().getName(),
									"检测P2P进程失败");
						}
					});
		}
		mDevicesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				device = mPeers.get(position);
				connectToPeer();
			}
		});
	}

	/**
	 * 连接或者断开连接的处理方法
	 */
	private void connectToPeer() {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		Log.d("FIND_DEVICE", "设备名称是："+device.deviceName+"---"+"设备地址是:"+device.deviceAddress);
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(MainActivity.this.getClass().getName(), "成功连接到"
						+ device.deviceName);
				isConnected=true;
				Toast.makeText(MainActivity.this, "成功连接到" + device.deviceName,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Log.d(MainActivity.this.getClass().getName(), "连接失败");
				Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT)
						.show();
				isConnected=false;
				sendButtonVisi();
			}
		});
	}

	private void cancelConnect(WifiP2pDevice device) {
		metaballView.setVisibility(View.VISIBLE); 
		if (mManager != null) {
			if (device == null || device.status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (device.status == WifiP2pDevice.AVAILABLE
					|| device.status == WifiP2pDevice.INVITED) {
				mManager.cancelConnect(mChannel, new ActionListener() {
					@Override
					public void onSuccess() {
						Toast.makeText(MainActivity.this,
								"Aborting connection", Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(
								MainActivity.this,
								"Connect abort request failed. Reason Code: "
										+ reasonCode, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}
	}

	private void disconnect() {

		mManager.removeGroup(mChannel, new ActionListener() {
			@Override
			public void onFailure(int reasonCode) {
			}

			@Override
			public void onSuccess() {
				// 将对等信息情况
				mPeers.clear();
				setAdapter();
				isConnected=false;
				sendButtonVisi();
			}
		});
	}

	/**
	 * 
	 * 按钮处理方法
	 */
	public void mDiscoverPeers(View view) {
		if (mManager != null) {
			metaballView.setVisibility(View.VISIBLE); 
			mManager.discoverPeers(mChannel,
					new WifiP2pManager.ActionListener() {

						@Override
						public void onSuccess() {
							Log.d(MainActivity.this.getClass().getName(),
									"检测P2P进程成功");
						}

						@Override
						public void onFailure(int reason) {
							Log.d(MainActivity.this.getClass().getName(),
									"检测P2P进程失败");
						}
					});
		}
	}
	public void mSendPeer(View view){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		//TODO 开启传输
		startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
	}
	public void mDisConnectPeer(View view) {
		cancelConnect(device);
	}

	/**
	 * 系统的函数
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// 实例化一个广播接收器
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	/**
	 * 传输
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// User has picked an image. Transfer it to group owner i.e peer using
		// FileTransferService.
		Uri uri = data.getData();
		Intent serviceIntent = new Intent(MainActivity.this,
				FileTransferService.class);
		serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
		serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
				uri.toString());
		//设置主机
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
				info.groupOwnerAddress.getHostAddress());
		//设置端口
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
				8988);
		MainActivity.this.startService(serviceIntent);
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fill) {
            metaballView.setPaintMode(1);
            return true;
        } else if (id == R.id.action_strock) {
            metaballView.setPaintMode(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
	/**
	 * 各个监听器的函数
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {// PeerListListener的处理函数
		// TODO Auto-generated method stub
		mPeers.clear();
		mPeers.addAll(peers.getDeviceList());
		if(mPeers.size()!=0){
			Log.d(this.getClass().getName(),
					"有设备：");
		}else{
			Log.d(this.getClass().getName(),
					"无设备：");
		}
		setAdapter();
	}

	/**
	 * ListView 的Adapter
	 */

	private class WifiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
		private List<WifiP2pDevice> items;

		public WifiPeerListAdapter(Context context, int textViewResourceId,
				List<WifiP2pDevice> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		@Override
		public WifiP2pDevice getItem(int position) {
			// TODO Auto-generated method stub
			return super.getItem(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) MainActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item_main, null);
			}
			WifiP2pDevice device_item = items.get(position);
			if (device_item != null) {
				TextView tvdename = (TextView) convertView
						.findViewById(R.id.item_tv);
				if (tvdename != null)
					tvdename.setText(device_item.deviceName);
			}
			return convertView;
		}
	}

	private void setAdapter() {
		if (adapter == null) {
			adapter = new WifiPeerListAdapter(MainActivity.this,
					R.layout.item_main, mPeers);
			mDevicesList.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}
	
	private void sendButtonVisi(){
		if(isConnected){
			sendBt.setVisibility(View.VISIBLE);
		}else{
			sendBt.setVisibility(View.GONE); 
		}
	}
	/**清空自身与peers*/
	public void resetData(){
		device = null;
		mPeers.clear();
		setAdapter();
		isConnected=false;
		sendButtonVisi();
		metaballView.setVisibility(View.VISIBLE); 
	}
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		// TODO Auto-generated method stub
		this.info=info;
		if(info.groupFormed && info.isGroupOwner){
			new FileServerAsyncTask(MainActivity.this).execute();
			metaballView.setVisibility(View.GONE);
			Toast.makeText(MainActivity.this, "作为服务器开启成功", Toast.LENGTH_SHORT).show(); 
		}else if(info.groupFormed){
			isConnected=true;
			sendButtonVisi();
			metaballView.setVisibility(View.GONE);
			Toast.makeText(MainActivity.this, "作为客户端开启成功", Toast.LENGTH_SHORT).show(); 
		}
	} 
}
