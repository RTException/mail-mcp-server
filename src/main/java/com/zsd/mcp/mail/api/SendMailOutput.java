package com.zsd.mcp.mail.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.jfr.Description;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author zhangshdiong
 * @date 2025/5/9 10:48
 * @description 发送邮件返回结果
 **/
@Data
@Builder
@Description("发送邮件返回结果")
public class SendMailOutput {

    @JsonProperty(required = true)
    @Description("返送邮件返回码，00=成功，其他为失败")
    @ToolParam(description = "接收邮件的地址")
    private String resultCode;

    @JsonProperty(required = true)
    @Description("返送邮件返回消息，")
    @ToolParam(description = "返送邮件返回消息")
    private String resultMsg;
}
