package com.potatofriedbread.astro;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class udpBroadcast extends Thread {
    MulticastSocket sender = null;
    DatagramPacket dj = null;
    InetAddress group = null;

    byte[] data = new byte[1024];

    public udpBroadcast(JSONObject room) {
        data = room.toString().getBytes();
    }

    @Override
    public void run() {
        try {
            Log.e("Broadcast", "开始广播");
            sender = new MulticastSocket();
            group = InetAddress.getByName(Value.broadcast_address);
            dj = new DatagramPacket(data, data.length, group, Value.broadcast_port);
            sender.send(dj);
            sender.close();
        } catch(IOException e) {
            Log.e("Broadcast", "炸了");
            e.printStackTrace();
        }
    }
}
