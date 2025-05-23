package com.zsd.mcp.mail.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.jfr.Description;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author zhangshdiong
 * @date 2025/5/9 10:48
 * @description 发送邮件请求实体
 **/
@Data
@Builder
@Description("发送邮件请求参数")
public class SendMailInput {

    @JsonProperty(required = true)
    @Description("接收邮件的地址")
    @ToolParam(description = "接收邮件的地址")
    String to;

    @JsonProperty(required = false)
    @Description("抄送邮件地址")
    @ToolParam(description = "抄送邮件地址", required = false)
    String cc;

    @JsonProperty(required = true)
    @Description("邮件主题")
    @ToolParam(description = "邮件主题")
    String subject;

    @JsonProperty(required = true)
    @Description("邮件内容")
    @ToolParam(description = "邮件内容")
    String body;
}
