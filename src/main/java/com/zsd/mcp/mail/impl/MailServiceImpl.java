package com.zsd.mcp.mail.impl;

import com.zsd.mcp.mail.MailService;
import com.zsd.mcp.mail.api.FindContactInput;
import com.zsd.mcp.mail.api.FindContactOutput;
import com.zsd.mcp.mail.api.SendMailInput;
import com.zsd.mcp.mail.api.SendMailOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangshdiong
 * @date 2025/5/9 10:46
 * @description 邮件发送服务
 **/
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    /**模拟数据*/
    private static final Map<String, String> contacts = new HashMap<>();

    static {
        contacts.put("何龙", "helong@tf.cn");
        contacts.put("刘显波", "liuxianbo@tf.cn");
        contacts.put("张仕东", "zhangshidong@tf.cn");
        contacts.put("杨帆", "yangfan@tf.cn");
        contacts.put("涂太松", "tutaisong@tf.cn");
        contacts.put("谭伟", "tanwei@tf.cn");
        contacts.put("张沛", "zhangpei1@tf.cn");
        contacts.put("段龙", "duanlong@tf.cn");
        contacts.put("赖健", "laijian@tf.cn");
        contacts.put("邱季", "qiuji@tf.cn");
        contacts.put("蔡玉成", "caiyucheng@tf.cn");
    }

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Tool(description = "Create and send a new email message.")
    @Override
    public SendMailOutput sendMail(@ToolParam(description = "发送邮件的请求参数") SendMailInput input) {
        log.info("====收到 MCP.sendMail 调用请求：{}", input);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(input.getTo());
        simpleMailMessage.setCc();
        simpleMailMessage.setSubject(input.getSubject());
        simpleMailMessage.setText(input.getBody());
        try{
            mailSender.send(simpleMailMessage);
        }catch (Exception e){
            log.error("send mail failed:", e);
            throw e;
        }
        return SendMailOutput.builder()
                .resultCode("00")
                .resultMsg("发送成功")
                .build();
    }

    @Tool(description = "根据联系人姓名查找对应的邮件地址")
    @Override
    public FindContactOutput findContact(@ToolParam(description = "查找联系人请求参数") FindContactInput input){
        log.info("===收到 MCP.findContact 调用请求:{}", input);
        for (Map.Entry<String, String> contact : contacts.entrySet()){
            //模糊匹配
            Pattern contactPattern = Pattern.compile(".*" + input.getName() + ".*");
            Matcher matcher = contactPattern.matcher(contact.getKey());
            if(matcher.matches()){
                return FindContactOutput.builder()
                        .name(contact.getKey())
                        .email(contact.getValue())
                        .resultCode("00")
                        .resultMsg("查找成功")
                        .build();
            }
        }
        return FindContactOutput.builder()
                .resultCode("01")
                .resultMsg("未找到该联系人")
                .build();
    }
}
