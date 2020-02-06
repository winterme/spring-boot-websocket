package com.zzq.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebSocketController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/websocketredi/{name}")
    public String webSocket(@PathVariable String name, Model model) {
        try {
            log.info("跳转到websocket的页面上");
            model.addAttribute("username", name);
            return "websocket";
        } catch (Exception e) {
            log.info("跳转到websocket的页面上发生异常，异常信息是：" + e.getMessage());
            return "error";
        }
    }

    @RequestMapping("/verfUSerName")
    @ResponseBody
    public Object verfUSerName(String username){
        return WebSocket.getClients().get(username)==null;
    }


}
