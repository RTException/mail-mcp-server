package com.zsd.mcp.mail;

import com.zsd.mcp.mail.api.SendMailInput;
import com.zsd.mcp.mail.api.SendMailOutput;

/**
 * @author zhangshdiong
 * @date 2025/5/9 10:46
 * @description 电子邮件服务接口定义
 **/
public interface MailService {

    /**
     * 发送电子邮件
     * @param input 请求
     * @return 相应
     */
    SendMailOutput sendMail(SendMailInput input);

}
