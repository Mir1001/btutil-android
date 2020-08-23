package com.mir1001.btutil.classic;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_CONNECTED;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_DATA_READ;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_DATA_TRANSFER_IO_ERROR;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_IO_STREAM_DISCONNECTED;


//https://developer.android.com/guide/topics/connectivity/bluetooth#ManageAConnection
public class BTConnectedThread extends Thread {
    private static final String TAG = BTConnectedThread.class.getName();
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    Handler handler;

    public BTConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        this.handler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        Log.d(TAG, "Start new connection");
        Message msg = handler.obtainMessage(
                MESSAGE_BT_CONNECTED,"BT Start new connection");
        msg.sendToTarget();
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
             msg = handler.obtainMessage(
                     MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Error occurred when creating input stream");
            msg.sendToTarget();
            Log.e(TAG, "BT Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            msg = handler.obtainMessage(
                    MESSAGE_BT_DATA_TRANSFER_IO_ERROR,"BT Error occurred when creating output stream");
            msg.sendToTarget();
            Log.e(TAG, "BT Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream. Limited to string
                numBytes = mmInStream.read(mmBuffer);
                byte[] data = Arrays.copyOf(mmBuffer, numBytes);
                if(handler != null) {
                    Message msg = handler.obtainMessage(
                            MESSAGE_BT_DATA_READ,new String(data));
                    msg.sendToTarget();

                }
            } catch (IOException e) {
                Message msg = handler.obtainMessage(
                        MESSAGE_BT_IO_STREAM_DISCONNECTED,"Input stream was disconnected");
                msg.sendToTarget();
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(String write) {
        try {
            byte[] bytes = write.getBytes();
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_IO_STREAM_DISCONNECTED,"Error occurred when sending data");
            msg.sendToTarget();
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Message msg = handler.obtainMessage(
                    MESSAGE_BT_IO_STREAM_DISCONNECTED,"Could not close the connect socket");
            msg.sendToTarget();
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}