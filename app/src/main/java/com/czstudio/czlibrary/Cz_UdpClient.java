package com.czstudio.czlibrary;

import android.app.Activity;
import android.content.Context;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Cz_UdpClient {
    public static final int RX_DATA_LENGTH = 1024;
    Context context;
    Cz_UdpClient instance;
    int listenPort;
    DatagramSocket udpSocket = null;
    Activity activity;

    boolean isRunning;

    Thread listenThread;
    Thread sendThread;

    ArrayList<UdpSendData> sendBytesList;

    byte rxData[] = new byte[RX_DATA_LENGTH];

    public Cz_UdpClient(Context Context, int listenPort) {
        instance=this;
        this.context = context;
        this.listenPort = listenPort;
        sendBytesList = new ArrayList<UdpSendData>();
        if(initUdpSocket()) {
            isRunning=true;
            startListenThread();
            startSendThread();
        }
    }

    public boolean initUdpSocket() {
        try {
            udpSocket = new DatagramSocket(listenPort);
            udpSocket.setSoTimeout(5000);
            return true;
        } catch (Exception e) {
            sendError("Cz_UdpClient/initUdpSocket/初始化UdpSocket失败:" + e, -1);
            udpSocket=null;
            return false;
        }
    }

    public void startListenThread() {
        if(udpSocket==null){
            return;
        }

        listenThread = new Thread() {
            public void run() {
                DatagramPacket packet = new DatagramPacket(rxData, rxData.length);
                while (isRunning) {
                    if(udpSocket!=null){
                        try {
                            udpSocket.receive(packet);
                            sendMessageBytes(packet.getData(),packet.getAddress().getHostName(),packet.getPort());
                        }catch(Exception ex){
                            sendError("Cz_UdpClient/startListenThread/接收数据失败:" + ex, -1);
                        }
                    }
                }
            }
        };
        listenThread.start();
    }

    public void stopListenThread() {
        isRunning = false;
    }

    public void startSendThread(){
        if(udpSocket==null){
            return;
        }
        sendThread = new Thread() {
            public void run(){
                while(isRunning){
                    if(udpSocket!=null){
                        if(sendBytesList.size()>0){
                            UdpSendData sendData = sendBytesList.get(0);
                            try {
                                udpSocket.send(new DatagramPacket(
                                        sendData.sendBytes, sendData.sendBytes.length, InetAddress.getByName(sendData.ip), sendData.port
                                ));
                            }catch(Exception e){
                                sendError("Cz_UdpClient/startSendThread/发送数据失败:" + e, -1);
                            }
                        }
                    }
                }
            }
        };
        sendThread.start();
    }

    public void sendUdpBytes(byte[] sendBytes,String ip,int port){
        sendBytesList.add(new UdpSendData(sendBytes, ip, port));
    }

    void sendMessageBytes(final byte[] msgBytes,final String ip, final int port) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(udpClientListener!=null){
                            udpClientListener.onGetMessageBytes(instance,msgBytes,ip, port);
                        }
                    }
                }
        );
    }

    void sendMessageStr(final String msgStr,final String ip, final int port) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(udpClientListener!=null){
                            udpClientListener.onGetMessageString(instance,msgStr,ip, port);
                        }
                    }
                }
        );
    }

    void sendError(final String errInfo, final int errCode) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if(udpClientListener!=null){
                            udpClientListener.onError(instance,errInfo,errCode);
                        }
                    }
                }
        );
    }

    public interface UdpClientListener {
        public void onError(Cz_UdpClient udpClient,String errInfo, int errCode);
        public void onGetMessageBytes(Cz_UdpClient udpClient,byte[] msgBytes,String ip, int port);
        public void onGetMessageString(Cz_UdpClient udpClient,String msgStr,String ip, int port);
    }

    UdpClientListener udpClientListener = null;

    public void setUdpClientListener(UdpClientListener listener) {
        udpClientListener = listener;
    }
}

class UdpSendData{
    byte[] sendBytes;
    String ip;
    int port;
    public UdpSendData(byte[] sendBytes,String ip,int port){
        this.sendBytes = sendBytes;
        this.ip = ip;
        this.port = port;
    }
}
