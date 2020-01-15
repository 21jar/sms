package com.ixiangliu.controller;

import com.ixiangliu.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.smslib.Message;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.SerialModemGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    private static Service srv = null;

    @Value("${sms.comPort}")
    private String comPort;

    @Value("${sms.baudRate}")
    private int baudRate;

    @Value("${sms.manufacturer}")
    private String manufacturer;

    /**
     * 列表
     */
    @RequestMapping("/send")
    public Result send(String phone, String content){
        boolean sendStatus = sendSms(phone, content);
        return sendStatus?Result.ok("发送成功") : Result.error("发送失败");
    }

    public boolean creatService() {
        List<String> coms = getAllComPorts();
        if (!coms.contains(comPort)) {
            log.error("扫描没有此端口");
            return false;
        }
        srv = new Service();
        SerialModemGateway gateway = new SerialModemGateway("SMS", comPort, baudRate, manufacturer, "");
        gateway.setInbound(true);
        gateway.setOutbound(true);
        try {
            srv.S.SERIAL_POLLING = true;
            srv.addGateway(gateway);
            srv.startService();
            log.info("Modem connected.");
        } catch (Exception ex) {
            log.error("exception", ex);
            return false;
        }
        return true;
    }

    /**
     * @describe: 列举全部串口名称
     * @date:2009-11-22
     */
    public static List<String> getAllComPorts(){
        List<String> comList = new ArrayList<String>();
        Enumeration en = gnu.io.CommPortIdentifier.getPortIdentifiers();
        gnu.io.CommPortIdentifier portIdRs = null;

        while (en.hasMoreElements()) {
            portIdRs = (gnu.io.CommPortIdentifier) en.nextElement();
            if (portIdRs.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                comList.add(portIdRs.getName());
            }
        }
        return comList;
    }

    public boolean sendSms(String mobile, String content) {
        if (srv == null) {
            if (!creatService()) {
                log.error("连接失败");
                close();
                srv = null;
                return false;
            }
        }
        OutboundMessage msg = new OutboundMessage(mobile, content);
        msg.setEncoding(Message.MessageEncodings.ENCUCS2);
        try {
            srv.sendMessage(msg);
            System.out.println(msg);
        } catch (Exception ex) {
            log.error("exception", ex);
            try {
                System.out.println("发送失败   重新发送   ...");
                srv.sendMessage(msg);
                System.out.println(msg);
            } catch (Exception ex2) {
                log.error("exception", ex2);
                return false;
            }
        }
        // 关闭后再次发送失败
        close();
        return true;
    }

    public static void close() {
        try {
            log.info("关闭成功");
            srv.stopService();
        } catch (Exception ex) {
            log.error("关闭失败");
            log.error("exception", ex);
        }
    }

    public static void main(String args[]) throws UnsupportedEncodingException {
        String content = "你好";
//        content = new String(content.getBytes("gbk"), "utf-8");
//        sendSms("111", content);
    }

}