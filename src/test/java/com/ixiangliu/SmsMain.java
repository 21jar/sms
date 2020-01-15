package com.ixiangliu;

import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

import java.io.UnsupportedEncodingException;

public class SmsMain {

    private static Service srv = null;

    public static boolean creatService() {
        srv = new Service();
        SerialModemGateway gateway = new SerialModemGateway("SMS", "COM2", 9600, "", "");
        gateway.setInbound(true);
        gateway.setOutbound(true);
        try {
            srv.S.SERIAL_POLLING = true;
            srv.addGateway(gateway);
            srv.startService();
            System.out.println("Modem connected.");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String args[]) throws UnsupportedEncodingException {
        String content = "你好";
//        content = new String(content.getBytes("gbk"), "utf-8");
        sendSms("18703694138", content);
    }

    public static boolean sendSms(String mobile, String content) {
        if (srv == null) {
            if (!creatService()) {
                close();
                srv = null;
                return false;
            }
        }
        OutboundMessage msg = new OutboundMessage(mobile, content);
        msg.setEncoding(MessageEncodings.ENCUCS2);
        try {
            srv.sendMessage(msg);
            System.out.println(msg);
        } catch (Exception ex) {
            try {
                System.out.println("发送失败   重新发送   ...");
                srv.sendMessage(msg);
                System.out.println(msg);
            } catch (Exception ex2) {
                return false;
            }
        }
        close();
        return true;
    }

    public static void close() {
        try {
            System.out.println("Modem disconnected.");
            srv.stopService();
        } catch (Exception ex) {

        }
    }
}
