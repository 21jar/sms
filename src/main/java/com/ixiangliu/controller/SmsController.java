package com.ixiangliu.controller;

import com.ixiangliu.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.smslib.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        SmsSendJob smsSendJob = SmsSendJob.getInstance();	// 运行实例
        if (comPort != null){
            boolean status = true;
            smsSendJob.initial(baudRate, comPort);				// 设置波特率和串口字符
            if (smsSendJob.readyToSendMsg()){				// 准备 - ok
                // smsSendJob.printSmsInof();					// 打印sms信息
                List<String> phoneList = new ArrayList<String>();
                phoneList.add(phone);
                status = smsSendJob.sendMessage(phoneList, content);
                if (status) {
                    log.info("发送成功");
                } else {
                    log.info("发送失败");
                }
                try {
                    Thread.sleep(60 * 1000);  						// 一分钟后,关闭短信服务
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 接收短信在SmsService中已经注册,InboundNotification中process会处理
                // 收短信后,默认会删除收到的短信,也可以通过setRec_msg_remove(boolean)修改
                status = true;
            } else {
                status = false;
                log.error("初始化短信模块失败");
            }
            smsSendJob.stopService();
            return status ? Result.ok("发送成功") : Result.ok("发送失败");
        }else{
            log.error("没有找到合适的串口号");
            return Result.ok("发送失败");
        }
    }

    public static void main(String[] args) throws Exception {

        SmsSendJob smsSendJob = SmsSendJob.getInstance();	// 运行实例
        String comName ="COM2"; 		// 获取合适短信模块的 串口字符

        if (comName != null){
            smsSendJob.initial(9600, comName);				// 设置波特率和串口字符
            if (smsSendJob.readyToSendMsg()){				// 准备 - ok
                // smsSendJob.printSmsInof();					// 打印sms信息
                List<String> phoneList = new ArrayList<String>();
                phoneList.add("18703694138");
                String message = "是job"; // 给10086发一条查询余额的短信
                smsSendJob.sendMessage(phoneList, message);
                //Thread.sleep(60 * 1000);  						// 一分钟后,关闭短信服务
                // 接收短信在SmsService中已经注册,InboundNotification中process会处理
                // 收短信后,默认会删除收到的短信,也可以通过setRec_msg_remove(boolean)修改
            }
            smsSendJob.stopService();
        }else{
            System.out.println("没有找到合适的串口号");
        }
    }

}