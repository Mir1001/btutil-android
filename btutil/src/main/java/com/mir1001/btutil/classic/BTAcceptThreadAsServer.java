package com.mir1001.btutil.classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_DATA_TRANSFER_IO_ERROR;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_SOCKET;


//https://developer.android.com/guide/topics/connectivity/bluetooth#ConnectionTechniques
public class BTAcceptThreadAsServer extends Thread {
    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = BTAcceptThreadAsServer.class.getName();
    private final BluetoothServerSocket mmServerSocket;
    Handler handler;

   // BluetoothAdapter bluetoothAdapter;
   public BTAcceptThreadAsServer(BluetoothAdapter bluetoothAdapter, String NAME, Handler handler) {
       this(bluetoothAdapter,NAME,BLUETOOTH_SPP, handler);
   }
    public BTAcceptThreadAsServer(BluetoothAdapter bluetoothAdapter, String NAME, UUID MY_UUID, Handler handler) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        this.handler = handler;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Socket's listen() method failed");
            msg.sendToTarget();
            Log.e(TAG, "BT Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Message msg = handler.obtainMessage(
                        MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Socket's accept() method failed");
                msg.sendToTarget();
                Log.e(TAG, "BT Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Message msg = handler.obtainMessage(
                            MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Socket's close() method failed");
                    msg.sendToTarget();
                    Log.e(TAG, "BT Socket's close() method failed", e);
                }
                break;
            }
        }
    }

    private void manageMyConnectedSocket(BluetoothSocket socket) {
        Message msg = handler.obtainMessage(
                MESSAGE_BT_SOCKET,socket);
        msg.sendToTarget();
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Could not close the connect socket");
            msg.sendToTarget();
        }
    }
}