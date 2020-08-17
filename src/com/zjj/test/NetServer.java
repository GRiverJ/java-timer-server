package com.zjj.test;

import com.zjj.test.lib.UrlUtil;

import java.io.*;
import java.net.*;

import java.util.Timer;
import java.util.TimerTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.OutputStream;

/**
 * 一些命令
 * javaw -jar TimerServer.jar
 * netstat -ano | find 12900
 * tskill XXXX
 */
public class NetServer{
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(12900, 100, InetAddress.getByName("localhost.com"));
        System.out.println("Server started  at:  " + serverSocket);
        while (true) {
            System.out.println("Waiting for a  connection...");
            final Socket activeSocket = serverSocket.accept();
            System.out.println("Received a  connection from  " + activeSocket);
            Runnable runnable = () -> handleClientRequest(activeSocket);
            new Thread(runnable).start(); // start a new thread
        }
    }

    public static void handleClientRequest(Socket socket) {
        try{
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream os=socket.getOutputStream();

            String inMsg;
            int delay = 0;
            String url = "";
            while ((inMsg = socketReader.readLine()) != null) {
                if(inMsg.isEmpty()){
                    break;
                }
                if(inMsg.indexOf("favicon.ico") > 0){
                    break;
                }
                if(inMsg.indexOf("HTTP/") > 0){
                    UrlUtil.UrlEntity urlEntity = UrlUtil.parse(inMsg);
                    if(urlEntity.params.get("delay") != null && urlEntity.params.get("url") != null){
                        delay = Integer.parseInt(urlEntity.params.get("delay"));
                        url = urlEntity.params.get("url").replace(" HTTP/1.1", "");
                    }
                }
                System.out.println("Received from  client: " + inMsg);
            }
            if(delay != 0 && !url.equals("")){
                setTimer(delay, url);
            }
            String res = "HTTP/1.1 200 OK\r\n" +"Content-Type: text/html\r\n" +"\r\n" +"success";
            os.write(res.getBytes());
            os.flush();
            os.close();
            socket.close();
            System.out.println("close");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void setTimer(int delay, String url){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("-------执行任务--------");
                    String urlNew = URLDecoder.decode(url, "UTF-8");
                    System.out.println("访问:"+urlNew);
                    String result = UrlUtil.httpRequest(urlNew, "GET", null);
                    System.out.println("返回值:"+result);
                    System.out.println("-------结束任务--------");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, delay * 1000);
    }
}




