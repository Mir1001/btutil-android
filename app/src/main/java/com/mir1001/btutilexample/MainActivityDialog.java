package com.mir1001.btutilexample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mir1001.btutil.BTHandlerMessages;
import com.mir1001.btutil.BTHelper;
import com.mir1001.btutil.classic.BTConnectThreadAsClient;
import com.mir1001.btutil.dialog.BTDevicePickerDialog;

import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_SEND_DATA;


public class MainActivityDialog extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1211;
    private static final String TAG = MainActivityDialog.class.getName();
    private TextView tvInfo;
    private BluetoothDevice selectedDevice;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String currentReadData = "";
    private SharedPreferences sharedPreferences;
    private String LAST_SELECTED_GC = "LAST_SELECTED_GC";
    BTConnectThreadAsClient btConnectThreadAsClient;
    Button btnSendData;
    Handler btHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvInfo = findViewById(R.id.tvInfo);
        btnSendData = findViewById(R.id.btnSendData);
        btnSendData.setEnabled(false);
        sharedPreferences =
                getPreferences(Context.MODE_PRIVATE);
        initBTHandler();
    }

    private void initBTHandler() {
        btHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Log.i(TAG, msg.what + " " + msg.obj);
                setInfoText(msg.obj + "");
                if (BTHandlerMessages.isError(msg.what)) {
                    btnSendData.setEnabled(false);
                }
                switch (msg.what) {
                    case BTHandlerMessages.MESSAGE_BT_PICKER_DEVICE_SELECTED:
                        selectedDevice = (BluetoothDevice) msg.obj;
                        connect();
                        Log.i(TAG, "Selected device: " + selectedDevice.getAddress());
                        break;
                    case BTHandlerMessages.MESSAGE_BT_CONNECTED:
                        Log.i(TAG, "Connected: " + selectedDevice.getAddress());
                        setInfoText("Connected");
                        btnSendData.setEnabled(true);
                        break;
                    case BTHandlerMessages.MESSAGE_BT_DATA_READ:
                        Log.i(TAG, "Data read: " + selectedDevice.getAddress());
                        currentReadData += msg.obj;
                        //parse data commands
                        break;
                    case MESSAGE_BT_SEND_DATA:
                        Log.i(TAG, "send data: " + msg.obj);
                        btConnectThreadAsClient.write(msg.obj.toString());
                        break;
                    default:
                        Log.i(TAG, "Message " + msg.what+" obj:"+msg.obj!=null?msg.obj.toString():"null");
                }

            }
        };
    }

    private void setInfoText(String s) {
        tvInfo.setText(s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else
            onBTEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK)
                onBTEnabled();
            else {
                setInfoText("BT not enabled!");
            }
        }
    }

    void pickBT() {
        BTDevicePickerDialog dialog = new BTDevicePickerDialog(btHandler);
        dialog.show(getSupportFragmentManager(), null);
    }

    void onBTEnabled() {
        String savedDeviceAddress = sharedPreferences.getString(LAST_SELECTED_GC, "");
        if (!savedDeviceAddress.equals("")) BTHelper.ObtainDeviceByAddress(savedDeviceAddress,btHandler);
        else pickBT();
    };


    public void btnSendData(View view) {
        Message msg = btHandler.obtainMessage(MESSAGE_BT_SEND_DATA,"this is data to be send. \n");
        msg.sendToTarget(); //this is communication in the sam activity check handler to see send command
    }

    public void btnOpenPicker(View view) {
        pickBT();
    }

    private void connect() {
        disconnect(); //if any device was connected
        btConnectThreadAsClient = new BTConnectThreadAsClient(selectedDevice, "00001101-0000-1000-8000-00805F9B34FB", bluetoothAdapter, btHandler);
        btConnectThreadAsClient.start();

    }

    public void btnConnect(View view) {
        connect();
    }

    private void disconnect() {
        if (btConnectThreadAsClient != null) btConnectThreadAsClient.cancel();
        btConnectThreadAsClient = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

}