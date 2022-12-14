package com.termux.api.apis;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.JsonWriter;
import android.util.Log;
import android.os.Handler;

import com.termux.MainActivity;
import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// TERMUX MERGE

// This is the implementation of the termux-api command "termux-bluetooth". Its purpose is to supply a bluetooth scan and bluetooth connect methods.
public class BluetoothAPI {

    private static final String LOG_TAG = "BluetoothAPI";

    private static boolean scanning = false;
    private static Set<String> deviceList = new HashSet<String>();
    public static boolean unregistered = true;
    public static BluetoothAdapter mBluetoothAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String deviceName = device.getName();
                    // you can get more info from device here
                    // ...

                    if (deviceName != null && !deviceName.equals("null") && !deviceName.trim().equals("")) {
                        deviceList.add(deviceName);
                    }
                }
            }
        }
    };
    private static BluetoothDevice mBluetoothDevice;
    private static ProgressDialog mBluetoothConnectProgressDialog;
    private static BluetoothSocket mBluetoothSocket;

    public static void bluetoothStartScanning() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        MainActivity.activity.getBaseContext().registerReceiver(mReceiver, filter);
        unregistered = false;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
    }

    public static void bluetoothStopScanning() {
        if (!unregistered) {
            mBluetoothAdapter.cancelDiscovery();
            MainActivity.activity.getBaseContext().unregisterReceiver(mReceiver);
            unregistered = true;
        }
    }

    public static void onReceiveBluetoothScanInfo(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {

            @Override
            public void writeJson(final JsonWriter out) throws Exception {
                scanning = true;
                deviceList.clear();
                bluetoothStartScanning();

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    bluetoothStopScanning();
                    scanning = false;
                    try {
                        out.name("devices").beginArray();
                        for (String device : deviceList) {
                            out.value(device);
                        }
                        out.endArray();
                        out.endObject();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 2000);
            }
        });
    }


    public static void onReceiveBluetoothConnect(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.WithStringInput() {

            @Override
            public void writeResult(PrintWriter out) throws Exception {
                try {
                    JsonWriter writer = new JsonWriter(out);
                    writer.setIndent("  ");

                    if (inputString.equals("")) {
                        writer.beginObject().name("message:").value("invalid input").endObject();
                    } else {
                        //Implement Bluetooth connection here to the device with the name inputString
                        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(inputString);
                        mBluetoothConnectProgressDialog = ProgressDialog.show(context, "Connecting...", mBluetoothDevice.getName() + " : " + mBluetoothDevice.getAddress(), true, false);
                        Thread mBluetoothConnectThread = new Thread(context.toString());
                        mBluetoothConnectThread.start();

                        try {
                            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                            mBluetoothAdapter.cancelDiscovery();
                            mBluetoothSocket.connect();
                            mHandler.sendEmptyMessage(0);
                        } catch (IOException eConnectException) {
                            Log.d(LOG_TAG, "CouldNotConnectToSocket", eConnectException);

                            writer.beginObject().name("message:").value("Bluetooth cannot connect to " + inputString).endObject();
                            out.println();

                            closeSocket(mBluetoothSocket);
                            return;
                        }

                        writer.beginObject().name("message:").value("Bluetooth connected to " + inputString).endObject();
                        out.println();
                    }
                } catch (Exception e) {
                    Log.d("Except BluetoothConnect", e.getMessage());

                }
            }
        });
    }

    private static void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(LOG_TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(LOG_TAG, "CouldNotCloseSocket");
        }
    }

    private static final Handler mHandler = new Handler(Looper.myLooper(), (msg) -> {
        mBluetoothConnectProgressDialog.dismiss();
        return true;
    });
}
