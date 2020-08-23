package com.mir1001.btutil.classic;
//https://developer.android.com/guide/topics/connectivity/bluetooth#ConnectAsAClient

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.mir1001.btutil.BTHandlerMessages.*;

public class BTConnectThreadAsClient extends Thread {
    private static final String TAG = BTConnectThreadAsClient.class.getName();
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final UUID MY_UUID;
    BluetoothAdapter bluetoothAdapter;
    BTConnectedThread thread;
    Handler handler;

    public void write(String data) {
        if (thread!=null) {
            thread.write(data);
        } else {
            Message msg = handler.obtainMessage(MESSAGE_BT_IO_CONNECTION_ERROR,"BTConnectedThread is null");
            msg.sendToTarget();
        }
    }

    public BTConnectThreadAsClient(BluetoothDevice device, String MY_UUIDS, BluetoothAdapter bluetoothAdapter, Handler handler) {
        this.MY_UUID = UUID.fromString(MY_UUIDS);
        this.handler = handler;
        this.bluetoothAdapter = bluetoothAdapter;
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_DATA_TRANSFER_IO_ERROR, "BT Socket's create() method failed");
            msg.sendToTarget();

            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_IO_CONNECTION_ERROR, "BT Socket's create() method failed");
            msg.sendToTarget();
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                msg = handler.obtainMessage(
                        MESSAGE_BT_DATA_TRANSFER_IO_ERROR, "BT Could not close the client socket");
                msg.sendToTarget();
                Log.e(TAG, "BT Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageMyConnectedSocket(mmSocket);
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
        thread = new BTConnectedThread(mmSocket,handler);
        Message msg = handler.obtainMessage(
                MESSAGE_BT_SOCKET, mmSocket);
        msg.sendToTarget(); //send object
        thread.start(); //AUTO start
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            if (thread!=null) thread.cancel();
            mmSocket.close();
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_DATA_TRANSFER_IO_ERROR, "BT Could not close the client socket");
            msg.sendToTarget();
            Log.e(TAG, "BT Could not close the client socket", e);
        }
    }
}