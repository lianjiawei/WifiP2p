/**
 * 2015byxiameng
 * wifip2pѧϰ
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
	 * WifiP2p�Ļ���ǰ�����
	 */
	private IntentFilter mIntentFilter;
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiP2pInfo info;
	/*
	 * WifiP2p�豸��Ϣ
	 */
	private BroadcastReceiver mReceiver;
	private List<WifiP2pDevice> mPeers = new ArrayList<WifiP2pDevice>(); // ������ŷ��ֵĽڵ�
	private static WifiP2pDevice device;
	
	/*
	 * UI���
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
		// �õ�һ���㲥������
		mIntentFilter = new IntentFilter();
		// ָʾһ��WI-FI P2P ״̬�ı仯������
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// ָʾһ���ڿ��õ�PEER�˵�List�����һ���仯
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// ָʾһ��������״̬��P2P�˵�״̬�ı仯
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// ָʾһ���豸��ϸ�ڷ����˱仯
		mIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		// �õ�һ��WifiP2pManagerʵ��
		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

		// ʵ��WifiP2pManager��initialize�����õ�WifiP2pManager.Channel����APP��Wi-Fi
		// P2P��ܵ�����
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
									"���P2P���̳ɹ�");
							metaballView.setVisibility(View.VISIBLE); 
						}

						@Override
						public void onFailure(int reason) {
							Log.d(MainActivity.this.getClass().getName(),
									"���P2P����ʧ��");
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
	 * ���ӻ��߶Ͽ����ӵĴ�����
	 */
	private void connectToPeer() {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		Log.d("FIND_DEVICE", "�豸�����ǣ�"+device.deviceName+"---"+"�豸��ַ��:"+device.deviceAddress);
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(MainActivity.this.getClass().getName(), "�ɹ����ӵ�"
						+ device.deviceName);
				isConnected=true;
				Toast.makeText(MainActivity.this, "�ɹ����ӵ�" + device.deviceName,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(int reason) {
				Log.d(MainActivity.this.getClass().getName(), "����ʧ��");
				Toast.makeText(MainActivity.this, "����ʧ��", Toast.LENGTH_SHORT)
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
				// ���Ե���Ϣ���
				mPeers.clear();
				setAdapter();
				isConnected=false;
				sendButtonVisi();
			}
		});
	}

	/**
	 * 
	 * ��ť������
	 */
	public void mDiscoverPeers(View view) {
		if (mManager != null) {
			metaballView.setVisibility(View.VISIBLE); 
			mManager.discoverPeers(mChannel,
					new WifiP2pManager.ActionListener() {

						@Override
						public void onSuccess() {
							Log.d(MainActivity.this.getClass().getName(),
									"���P2P���̳ɹ�");
						}

						@Override
						public void onFailure(int reason) {
							Log.d(MainActivity.this.getClass().getName(),
									"���P2P����ʧ��");
						}
					});
		}
	}
	public void mSendPeer(View view){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		//TODO ��������
		startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
	}
	public void mDisConnectPeer(View view) {
		cancelConnect(device);
	}

	/**
	 * ϵͳ�ĺ���
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// ʵ����һ���㲥������
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	/**
	 * ����
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
		//��������
		serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
				info.groupOwnerAddress.getHostAddress());
		//���ö˿�
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
	 * �����������ĺ���
	 */
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {// PeerListListener�Ĵ�����
		// TODO Auto-generated method stub
		mPeers.clear();
		mPeers.addAll(peers.getDeviceList());
		if(mPeers.size()!=0){
			Log.d(this.getClass().getName(),
					"���豸��");
		}else{
			Log.d(this.getClass().getName(),
					"���豸��");
		}
		setAdapter();
	}

	/**
	 * ListView ��Adapter
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
	/**���������peers*/
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
			Toast.makeText(MainActivity.this, "��Ϊ�����������ɹ�", Toast.LENGTH_SHORT).show(); 
		}else if(info.groupFormed){
			isConnected=true;
			sendButtonVisi();
			metaballView.setVisibility(View.GONE);
			Toast.makeText(MainActivity.this, "��Ϊ�ͻ��˿����ɹ�", Toast.LENGTH_SHORT).show(); 
		}
	} 
}
