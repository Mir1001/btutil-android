package com.mir1001.btutil;

public class BTHandlerMessages {
    public static final int MESSAGE_BT_DATA_READ = 20001;
    public static final int MESSAGE_BT_IO_STREAM_DISCONNECTED = 20002;
    public static final int MESSAGE_BT_IO_CONNECTION_ERROR = 20003;
    public static final int MESSAGE_BT_SOCKET = 20004; //transports BluetoothSocket object if needed
    public static final int MESSAGE_BT_CONNECTED = 20005;
    public static final int MESSAGE_BT_SEND_DATA = 20006; //transports data to connected device
    public static final int MESSAGE_BT_DATA_TRANSFER_IO_ERROR = 20089;
    public static final int MESSAGE_BT_PICKER_NOT_SELECTED = 21000;
    public static final int MESSAGE_BT_PICKER_DEVICE_SELECTED = 21001;
    public static final int MESSAGE_BT_PICKER_DEVICE_ERROR = 21002;
    public static final int MESSAGE_BT_PICKER_NO_BT_AVAILABLE = 21003;
    public static final int MESSAGE_BT_PICKER_DEVICE_ADDRESS_NOT_AVAILABLE = 21004;

    public static boolean isError(int message) {
        switch (message) {
            case MESSAGE_BT_IO_STREAM_DISCONNECTED:
            case MESSAGE_BT_IO_CONNECTION_ERROR:
            case MESSAGE_BT_DATA_TRANSFER_IO_ERROR:
            case MESSAGE_BT_PICKER_NOT_SELECTED:
            case MESSAGE_BT_PICKER_DEVICE_ERROR:
            case MESSAGE_BT_PICKER_NO_BT_AVAILABLE:
                return true;
        }
        return false;
    }
}
