package com.summer.community.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.server.PathParam;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@ServerEndpoint("/msgServer/{userId}")
@Component
@Scope("prototype")
public class WebSocketServer {

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static final ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Session> MatchPool = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, String> MatchRelationship = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收userId
     */
    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        /**
         * 连接被打开：向socket-map中添加session

         */
        System.out.println(userId + " - 连接建立成功...");
        webSocketMap.put(userId, session);
        //MatchPool.put(userId, session);

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            this.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("连接异常...");
        error.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("连接关闭");
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        if (message.equals("心跳")){
            this.session.getBasicRemote().sendText(message);
        } else
        if (message.equals("准备连接")) {
            System.out.println(this.userId + message);
            if (!MatchPool.containsKey(this.userId) && !MatchRelationship.containsKey(this.userId))
                MatchPool.put(this.userId, this.session);
        } else
        if (message.equals("退出连接")) {
            System.out.println(this.userId + message);
            if (MatchPool.containsKey(this.userId))
                MatchPool.remove(this.userId);
            else {
                String target = MatchRelationship.get(this.userId);
                MatchRelationship.remove(this.userId);
                MatchRelationship.remove(target);
            }
        } else if (MatchRelationship.containsKey(this.userId)) {
            //已连接
            String key = MatchRelationship.get(this.userId);
            Session sessionValue = webSocketMap.get(key);
            if (sessionValue.isOpen()) {
                System.out.println("发消息给对方: " + key + " ,message: " + message);
                sessionValue.getBasicRemote().sendText(message);
            } else {
                System.err.println(key + ": not open");
                sessionValue.close();
                webSocketMap.remove(key);
            }
        } else {//未连接
            Enumeration<String> keys = MatchPool.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (key.equals(this.userId)) {
                    System.err.println("my id " + key);
                    continue;
                }
                if (webSocketMap.get(key) == null) {
                    webSocketMap.remove(key);
                    System.err.println(key + " : null");
                    continue;
                }
                Session sessionValue = webSocketMap.get(key);
                if (sessionValue.isOpen()) {
                    System.out.println("群发消息给: " + key + " ,message: " + message);
                    MatchPool.remove(this.userId);
                    MatchPool.remove(key);
                    MatchRelationship.put(this.userId, key);
                    MatchRelationship.put(key, this.userId);
                    System.out.println(this.userId + "  " + key + "已连接");
                    sessionValue.getBasicRemote().sendText(message);
                    break;
                } else {
                    System.err.println(key + ": not open");
                    sessionValue.close();
                    webSocketMap.remove(key);
                }
            }
        }
    }

    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        System.out.println("发送消息到:" + userId + "，内容:" + message);
        if (!StringUtils.isEmpty(userId) && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).getBasicRemote().sendText(message);
            //webSocketServer.sendMessage(message);
        } else {
            System.out.println("用户" + userId + ",不在线！");
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}