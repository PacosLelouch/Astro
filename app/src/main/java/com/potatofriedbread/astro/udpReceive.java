package com.potatofriedbread.astro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class udpReceive extends Thread {

    Socket socket = null;
    MulticastSocket ms = null;
    DatagramPacket dp;
    Handler mHandler = new Handler();

    public udpReceive(Handler handler){
        this.mHandler = handler;
    }

    @Override
    public void run(){
        Log.e("Receive", "开始接收");
        Message msg;
        String information;
        byte[] data = new byte[1024];

        try{
            InetAddress groupAddress = InetAddress.getByName(Value.broadcast_address);
            ms = new MulticastSocket(Value.broadcast_port);
            ms.joinGroup(groupAddress);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        while(true){
            try {
                dp = new DatagramPacket(data, data.length);
                if (ms != null)
                    ms.receive(dp);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(dp.getAddress() != null){
                final String quest_ip = dp.getAddress().toString();
                String host_ip = NetUtils.getLocalHostIp();
                System.out.println("host_ip:  --------------------  " + host_ip);
                System.out.println("quest_ip: --------------------  " + quest_ip.substring(1));
                /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/
                if( (!host_ip.equals(""))  && host_ip.equals(quest_ip.substring(1)) ) {
                    Log.e("Receive","收到自己的udp包");
                    continue;
                }

                final String dataString = new String(data, 0, dp.getLength());

                msg = new Message();
                msg.what = Value.msg_udp_update;
                Bundle bundle = new Bundle();
                try{
                    JSONObject dataJson = new JSONObject(dataString);
                    bundle.putString("hostIP",dataJson.getString("hostIP"));
                    bundle.putString("roomName",dataJson.getString("roomName"));
                    bundle.putString("roomCurNum", dataJson.getString("roomCurNum"));
                    bundle.putString("roomCapacity", dataJson.getString("roomCapacity"));
                    bundle.putString("roomState", dataJson.getString("roomState"));
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                Log.e("Receive","收到来自: \n" + quest_ip.substring(1) + "\n" +"的udp广播\n"
                        + "请求内容: " + dataString + "\n\n");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }

    }



}
