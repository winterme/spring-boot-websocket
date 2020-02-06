package com.zzq.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.logging.log4j.message.MapMessage.MapFormat.JSON;

@Component
@ServerEndpoint("/websocket/{username}")
public class WebSocket {

    private Logger log = LoggerFactory.getLogger(WebSocket.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在线人数
     */
    public static int onlineNumber = 0;
    /**
     * 以用户的姓名为key，WebSocket为对象保存起来
     */
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();

    public static Map<String, WebSocket> getClients() {
        return clients;
    }

    /**
     * 会话
     */
    private Session session;
    /**
     * 用户名称
     */
    private String username;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private String objectToJsonString(Object obj){
        try{
            return objectMapper.writeValueAsString(obj);
        }catch (Exception e){
            return "{}";
        }
    }

    /**
     * OnOpen 表示有浏览器链接过来的时候被调用
     * OnClose 表示浏览器发出关闭请求的时候被调用
     * OnMessage 表示浏览器发消息的时候被调用
     * OnError 表示有错误发生，比如网络断开了等等
     */

    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        String date = simpleDateFormat.format(new Date());
        onlineNumber++;
        log.info("现在来连接的客户id：" + session.getId() + "用户名：" + username);
        this.username = username;
        this.session = session;
        log.info("有新连接加入！ 当前在线人数" + onlineNumber);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
            //先给所有人发送通知，说我上线了
            Map<String, Object> map1 = new HashMap<>();
            map1.put("messageType", 1);
            map1.put("username", username);
            map1.put("t" , date);
            sendMessageAll(objectToJsonString(map1), username);

            //把自己的信息加入到map当中去
            clients.put(username, this);
            //给自己发一条消息：告诉自己现在都有谁在线
            Map<String, Object> map2 = new HashMap<>();
            map2.put("messageType", 3);
            //移除掉自己
            Set<String> set = clients.keySet();
            map2.put("onlineUsers", set);
            map2.put("t", date);
            sendMessageTo(objectToJsonString(map2), username);
        } catch (IOException e) {
            log.info(username + "上线的时候通知所有人发生了错误");
        }


    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("服务端发生了错误" + error.getMessage());
        //error.printStackTrace();
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        onlineNumber--;
        //webSockets.remove(this);
        clients.remove(username);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String, Object> map1 = new HashMap<>();
            map1.put("messageType", 2);
            map1.put("onlineUsers", clients.keySet());
            map1.put("username", username);
            map1.put("t" , simpleDateFormat.format(new Date()));
            sendMessageAll(objectToJsonString(map1), username);
        } catch (IOException e) {
            log.info(username + "下线的时候通知所有人发生了错误");
        }
        log.info("有连接关闭！ 当前在线人数" + onlineNumber);
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            log.info("来自客户端消息：" + message + "客户端的id是：" + session.getId());
            Map<String , String> map = objectMapper.readValue(message, Map.class);
            String textMessage = map.get("message");
            String fromusername = map.get("username");
            String tousername = map.get("to");
            //如果不是发给所有，那么就发给某一个人
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String, Object> map1 = new HashMap<>();
            map1.put("messageType", 4);
            map1.put("textMessage", textMessage);
            map1.put("fromusername", fromusername);
            map1.put("t" , simpleDateFormat.format(new Date()));
            if (tousername.equals("All")) {
                map1.put("tousername", "所有人");
                sendMessageAll(objectToJsonString(map1), fromusername);
            } else {
                map1.put("tousername", tousername);
                sendMessageTo(objectToJsonString(map1), tousername);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("发生了错误了");
        }
    }

    public void sendMessageTo(String message, String ToUserName) throws IOException {
        for (WebSocket item : clients.values()) {
            if (item.username.equals(ToUserName)) {
                item.session.getAsyncRemote().sendText(message);
                break;
            }
        }
    }

    public void sendMessageAll(String message, String FromUserName) throws IOException {
        for (WebSocket item : clients.values()) {
            item.session.getAsyncRemote().sendText(message);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineNumber;
    }

}
