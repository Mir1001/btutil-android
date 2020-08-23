package com.mir1001.btutil.dialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mir1001.btutil.R;

import java.util.Locale;

//Original from https://github.com/kbabioch/BluetoothDevicePicker

public class BTDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {
    private int resource;
    private LayoutInflater inflater;
    public BTDeviceListAdapter(Context context, int resource)
    {
        super(context, resource);
        this.resource = resource;
        inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.textDeviceName = (TextView) convertView.findViewById(R.id.textDeviceName);
            holder.textDeviceStatus = (TextView) convertView.findViewById(R.id.textDeviceStatus);
            holder.textDeviceAddress = (TextView) convertView.findViewById(R.id.textDeviceAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        BluetoothDevice device = getItem(position);
        holder.textDeviceName.setText(device.getName());
        holder.textDeviceStatus.setText(getNameOfState(device.getBondState()));
        holder.textDeviceAddress.setText(device.getAddress());

        return convertView;

    }

    private String getNameOfState(int bondState)
    {

        Locale l = Locale.getDefault();
        switch (bondState) {

            case BluetoothDevice.BOND_NONE:
                return getContext().getString(R.string.bt_device_status_none).toUpperCase(l);

            case BluetoothDevice.BOND_BONDING:
                return getContext().getString(R.string.bt_device_status_pairing).toUpperCase(l);

            case BluetoothDevice.BOND_BONDED:
                return getContext().getString(R.string.bt_device_status_paired).toUpperCase(l);

        }

        return null;

    }

    static class ViewHolder {

        TextView textDeviceName;
        TextView textDeviceStatus;
        TextView textDeviceAddress;

    }

}

