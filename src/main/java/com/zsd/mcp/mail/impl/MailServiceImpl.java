package com.zsd.mcp.mail.impl;

import com.zsd.mcp.mail.MailService;
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

/**
 * @author zhangshdiong
 * @date 2025/5/9 10:46
 * @description 邮件发送服务
 **/
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Tool(description = "Create and send a new email message.")
    @Override
    public SendMailOutput sendMail(@ToolParam(description = "发送邮件的请求参数") SendMailInput input) {
        log.info("====收到 MCP 调用请求：{}", input);
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
}
