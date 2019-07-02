package com.potatofriedbread.astro;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.os.Handler;

import org.json.JSONObject;


public class tcpClient {

    private Socket socket;
    private String target_ip;
    private String nickname;
    private Handler mHandler;

    public tcpClient(String host_ip, String nickname, Handler handler){
        target_ip = host_ip;
        this.nickname = nickname;
        mHandler = handler;
    }

    public void startConnect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("tcpClient", target_ip + Value.tcp_port);
                    socket = new Socket(target_ip, Value.tcp_port);
                    System.out.println("客户端连接服务器成功");
                    sendMsgToServer(generateHelloMsg()); // 发送hello信息
                    // 接收
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        String str = new String(buffer, 0, len);
                        System.out.println("接收到服务器信息: " + str);
                        try{
                            JSONObject data = new JSONObject(str);
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            Iterator<String> it = data.keys();
                            while(it.hasNext()){
                                String key = it.next();
                                String value = data.getString(key);
                                bundle.putString(key,value);
                            }
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("客户端收信bug");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMsgToServer(final String msg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    System.out.println("发信了发信了");
                    OutputStream os = socket.getOutputStream();
                    os.write(msg.getBytes("utf-8"));
                    os.flush();
                }
                catch(Exception e){
                    System.out.println("客户端发信失败");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String generateHelloMsg(){
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Value.type_hello);
        msg.put("clientName", nickname);
        return msg.toString();
    }

}
