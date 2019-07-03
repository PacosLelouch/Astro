package com.potatofriedbread.astro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

public class tcpServer {

    private static ServerThread serverThread = null;
    private static Map<String, clientStruct> clientMap = new HashMap<>(); // key = ip地址 value = 对应的结构体

    public static ServerThread getServerThread() {
        return serverThread;
    }

    public static void setServerThread(ServerThread serverThread) {
        tcpServer.serverThread = serverThread;
    }

    public static Map<String, clientStruct> getClientMap() {
        return clientMap;
    }

    public static void setClientMap(Map<String, clientStruct> clientMap) {
        tcpServer.clientMap = clientMap;
    }

    public static ArrayList<String> getClientList() {
        return clientList;
    }

    public static void setClientList(ArrayList<String> clientList) {
        tcpServer.clientList = clientList;
    }

    public static Boolean[] getIfUsedList() {
        return ifUsedList;
    }

    public static void setIfUsedList(Boolean[] ifUsedList) {
        tcpServer.ifUsedList = ifUsedList;
    }

    public static int getConnect_sum() {
        return connect_sum;
    }

    public static void setConnect_sum(int connect_sum) {
        tcpServer.connect_sum = connect_sum;
    }

    private static ArrayList<String> clientList = new ArrayList<>(); // item = ip地址
    private static Boolean[] ifUsedList = new Boolean[]{false, false, false, false};
    private static int connect_sum = 0;

    public static class clientStruct{
        public Socket clientSocket;
        public String clientName;
        public Integer clientPosition;
        public String clientIP;
    }

    public void changeHandler(Handler handler){
        serverThread.mHandler = handler;
    }

    private static class ServerThread implements Runnable {

        private Handler mHandler;
        private ServerSocket server;
        private boolean isExit = false;// 一个boolean类型的判断 默认是退出状态false

        // 构造函数
        public ServerThread(Handler handler) {
            try {
                server = new ServerSocket(Value.tcp_port);
                System.out.println("启动server，端口号：" + Value.tcp_port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mHandler = handler;
        }

        @Override
        public void run() {
            try{
                while(!isExit){
                    System.out.println("等待连接中");
                    final Socket socket = server.accept();
                    for(int i = 0; i < clientList.size(); i++){
                        if(socket.getRemoteSocketAddress().toString() == clientList.get(i)){
                            OutputStream os = socket.getOutputStream();
                            os.write("连接已存在, 正在断开连接".getBytes("utf-8"));
                            socket.close();
                        }
                    }

                    System.out.println("接收到连接, ip地址和端口号为: " + socket.getRemoteSocketAddress().toString());

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    // 在这里考虑到线程总数的计算 也代表着连接手机的数量
                                    connect_sum++;
                                    // 存入到集合和Map中为群发和单独发送做准备
                                    String string = socket.getInetAddress().toString().substring(1);
                                    clientList.add(string);
                                    clientStruct new_struct = new clientStruct();

                                    new_struct.clientSocket = socket;
                                    new_struct.clientIP = socket.getInetAddress().toString().substring(1);
                                    new_struct.clientName = null;

                                    for(int i = 0; i < 4; i++){
                                        if(ifUsedList[i] == false){
                                            new_struct.clientPosition = i;
                                            ifUsedList[i] = true;
                                            break;
                                        }
                                    }
                                    clientMap.put(string, new_struct);
                                }

                                // 定义输入输出流
                                InputStream is = socket.getInputStream();

                                // 接下来考虑输入流的读取显示到PC端和返回是否收到
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    String str = new String(buffer, 0, len);
                                    System.out.println("收到的数据为:\n" + str);
                                    try{
                                        JSONObject data = new JSONObject(str);
                                        Message msg = new Message();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("clientIP", socket.getInetAddress().toString().substring(1));
                                        Iterator<String> it = data.keys();
                                        while(it.hasNext()){
                                            String key = it.next();
                                            String value = data.getString(key);
                                            bundle.putString(key,value);
                                        }
                                        msg.setData(bundle);
                                        Object whatObj = bundle.get("type");
                                        if(whatObj != null) {
                                            msg.what = Integer.parseInt(whatObj.toString());
                                        }
                                        mHandler.sendMessage(msg);
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                System.out.println("关闭连接：" + socket.getRemoteSocketAddress().toString());
                                synchronized (this) {
                                    connect_sum--;
                                    String string = socket.getRemoteSocketAddress().toString();
                                    clientMap.remove(string);
                                    clientList.remove(string);
                                }
                            }
                        }
                    }).start();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        public void stop(){
            isExit = true;
            if(server != null){
                try{
                    server.close(); // 注意这样做之前应该把所有连接客户端也close
                    System.out.println("server端口已关闭");
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    public ServerThread startServer(Handler handler){
        System.out.println("server准备开启");
        if(serverThread != null){
            System.out.println("server已存在,正在重启server");
            shutdown();
        }
        serverThread = new ServerThread(handler);
        new Thread(serverThread).start();
        System.out.println("server开启成功");
        return serverThread;
    }

    public static void shutdown(){
        for(clientStruct stru: clientMap.values()){
            try{
                stru.clientSocket.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        serverThread.stop();
        ifUsedList = new Boolean[]{false, false, false, false};
        clientMap.clear();
        clientList.clear();
    }

    public static void sendMessageToAll(final String msg){
        for(final clientStruct stru:clientMap.values()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OutputStream os = stru.clientSocket.getOutputStream();
                        os.write(msg.getBytes("utf-8"));
                    }
                    catch(Exception e){
                        System.out.println("群发消息失败");
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static Boolean findPosition(String clientIP, String clientName){
        clientMap.get(clientIP).clientName = clientName;
        if(clientMap.get(clientIP).clientPosition != null)
            return true;
        else
            return false;
    }

    public static void broadcastPosition(){
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Value.msg_position);

        ArrayList<String> clientNames = new ArrayList<String>() {{
            add(null);
            add(null);
            add(null);
            add(null);
        }};
        ArrayList<String> clientIPs = new ArrayList<String>() {{
            add(null);
            add(null);
            add(null);
            add(null);
        }};
        for(clientStruct stru: clientMap.values()){
            clientNames.set(stru.clientPosition, stru.clientName);
            clientIPs.set(stru.clientPosition, stru.clientIP);
        }
        msg.put("clientNames", clientNames);
        msg.put("clientIPs", clientIPs);

        sendMessageToAll(msg.toString());
        return;
    }

}
