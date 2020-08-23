package com.mir1001.btutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_PICKER_DEVICE_ADDRESS_NOT_AVAILABLE;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_PICKER_DEVICE_SELECTED;
import static com.mir1001.btutil.BTHandlerMessages.MESSAGE_BT_PICKER_NO_BT_AVAILABLE;

public class BTHelper {
    public static void ObtainDeviceByAddress(String deviceAddress, Handler handler) {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt == null) {
            Message msg = handler.obtainMessage(MESSAGE_BT_PICKER_NO_BT_AVAILABLE, "For address " + deviceAddress);
            msg.sendToTarget();
            return;
        }
        for (BluetoothDevice device : bt.getBondedDevices())
            if (device.getAddress().equals(deviceAddress)) {
                Message msg = handler.obtainMessage(MESSAGE_BT_PICKER_DEVICE_SELECTED, device);
                msg.sendToTarget();
                return;
            };
        Message msg = handler.obtainMessage(MESSAGE_BT_PICKER_DEVICE_ADDRESS_NOT_AVAILABLE, deviceAddress);
        msg.sendToTarget();
        return;
    }
}

