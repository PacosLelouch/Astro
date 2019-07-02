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

import org.json.JSONObject;

public class tcpServer {

    private static ServerThread serverThread = null;
    private static Map<String, clientStruct> clientMap = new HashMap<>(); // key = ip地址 value = 对应的结构体
    private static ArrayList<String> clientList = new ArrayList<>(); // item = ip地址
    private static Boolean[] ifUsedList = new Boolean[]{false, false, false, false};
    private static int connect_sum = 0;

    private static class clientStruct{
        public Socket clientSocket;
        public String clientName;
        public Integer clientPosition;
        public String clientIP;
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
                                    String string = socket.getRemoteSocketAddress().toString();
                                    clientList.add(string);
                                    clientStruct new_struct = new clientStruct();

                                    new_struct.clientSocket = socket;
                                    new_struct.clientIP = socket.getInetAddress().toString();
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
                                OutputStream os = socket.getOutputStream();

                                // 接下来考虑输入流的读取显示到PC端和返回是否收到
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    System.out.println("收信了收信了");
                                    String str = new String(buffer, 0, len);
                                    System.out.println("收到的数据为:\n" + str);
                                    try{
                                        JSONObject data = new JSONObject(str);
                                        Message msg = new Message();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("clientIP", socket.getRemoteSocketAddress().toString());
                                        Iterator<String> it = data.keys();
                                        while(it.hasNext()){
                                            String key = it.next();
                                            String value = data.getString(key);
                                            bundle.putString(key,value);
                                        }
                                        msg.setData(bundle);
                                        msg.what = Integer.parseInt(bundle.get("type").toString());
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
        clientMap.clear();
        clientList.clear();
    }

    public static boolean sendMessageToAll(String msg){
        try{
            for(clientStruct stru:clientMap.values()){
                OutputStream os = stru.clientSocket.getOutputStream();
                os.write(msg.getBytes("utf-8"));
            }
            return true;
        }
        catch(Exception e){
            System.out.println("群发消息失败");
            e.printStackTrace();
            return false;
        }
    }



}
