package com.mir1001.btutil.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import com.mir1001.btutil.R;
import com.mir1001.btutil.BTHandlerMessages;

//Original from https://github.com/kbabioch/BluetoothDevicePicker
public class BTDevicePickerDialog extends DialogFragment {
    private static final String TAG = BTDevicePickerDialog.class.getName();
    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 0;
    private static final String BUNDLE_FOUND_DEVICES = "FOUND_DEVICES";

    private BluetoothAdapter bluetoothAdapter;
    private BTBroadcastReceiver receiver;
    private BTDeviceListAdapter adapterDevices;
    private Handler handler;
    boolean selectedOne = false;

    private ListView listDevices;

    public BTDevicePickerDialog(Handler handler) {
        super();
        this.handler = handler;
        selectedOne = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    /*    try {
            this.activity = (BTDevicePickedMessages) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + " must implement " + BTDevicePickedMessages.class.getName());
        }
*/
        Log.d(TAG, "Registering Bluetooth broadcast receiver");
        receiver = new BTBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(receiver, filter);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Message msg = handler.obtainMessage(BTHandlerMessages.MESSAGE_BT_PICKER_NO_BT_AVAILABLE, "No bluetooth adapter available");
            msg.sendToTarget();
            throw new RuntimeException("No bluetooth adapter available");
        }

        adapterDevices = new BTDeviceListAdapter(getActivity(), R.layout.list_item_bluetooth);
        if (!bluetoothAdapter.isEnabled()) {

            Log.d(TAG, "Bluetooth is disabled, asking user to enable it");
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLUETOOTH_REQUEST_CODE);

        } else {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();

        }

        if (savedInstanceState == null) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                adapterDevices.add(device);
            }
        } else {
            BluetoothDevice[] devices = (BluetoothDevice[]) savedInstanceState.getParcelableArray(BUNDLE_FOUND_DEVICES);
            adapterDevices.addAll(devices);
        }
        adapterDevices.notifyDataSetChanged();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "User enabled Bluetooth");
                bluetoothAdapter.startDiscovery();
            } else {
                Message msg = handler.obtainMessage(BTHandlerMessages.MESSAGE_BT_PICKER_DEVICE_ERROR, "Bluetooth must be enabled for this dialog");
                msg.sendToTarget();
                throw new RuntimeException("Bluetooth must be enabled for this dialog");
            }
        }
    }

    /*
    https://stackoverflow.com/questions/30978238/bluetooth-pairing-h
    ow-to-show-the-simple-cancel-pair-dialog
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);


        listDevices = (ListView) view.findViewById(R.id.listDevices);
        listDevices.setAdapter(adapterDevices);
        listDevices.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device = adapterDevices.getItem(position);

                Log.d(TAG, "Device selected: " + device.getName() + " (" + device.getAddress() + ")");
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    device.createBond();
                    Log.d(TAG, "Not bounded wait to be paired");
                    return;
                }
                Message msg = handler.obtainMessage(BTHandlerMessages.MESSAGE_BT_PICKER_DEVICE_SELECTED, device);
                msg.sendToTarget(); //send msg
                selectedOne = true;
                dismiss();
            }

        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.bt_title);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bluetoothAdapter != null) {
            Log.d(TAG, "Explicitly canceling Bluetooth discovery");
            bluetoothAdapter.cancelDiscovery();
        }
        if (!selectedOne) {
            Message msg = handler.obtainMessage(BTHandlerMessages.MESSAGE_BT_PICKER_NOT_SELECTED, "Exit BT picker without selecting device");
            handler.dispatchMessage(msg);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "Unregistering Bluetooth broadcast receiver");
        getActivity().unregisterReceiver(receiver);

    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        BluetoothDevice[] devices = new BluetoothDevice[adapterDevices.getCount()];
        for (int i = 0; i < adapterDevices.getCount(); i++) {
            devices[i] = adapterDevices.getItem(i);
        }
        bundle.putParcelableArray(BUNDLE_FOUND_DEVICES, devices);
    }

    private boolean deviceAlreadyDiscovered(BluetoothDevice device) {
        for (int i = 0; i < adapterDevices.getCount(); i++) {
            if (adapterDevices.getItem(i).equals(device)) {
                return true;
            }
        }
        return false;
    }


    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!deviceAlreadyDiscovered(device)) {
                    Log.d(TAG, "New device found: " + device.getName() + " (" + device.getAddress() + ")");
                    adapterDevices.add(device);
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "ACTION_BOND_STATE_CHANGED Bluetooth discovery started");
                adapterDevices.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery started");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth discovery finished");
                bluetoothAdapter.startDiscovery();
            }
        }
    }
}
