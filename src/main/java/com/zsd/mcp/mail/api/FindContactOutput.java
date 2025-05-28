package com.zsd.mcp.mail.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.jfr.Description;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author zhangshdiong
 * @date 2025/5/28 20:03
 * @description 查找联系人返回结果
 **/
@Data
@Builder
@Description("查找联系人返回结果")
public class FindContactOutput {

    @JsonProperty(required = true)
    @Description("查找联系人返回码，00=成功，其他为失败")
    @ToolParam(description = "查找联系人返回码")
    private String resultCode;

    @JsonProperty(required = true)
    @Description("查找联系人返回消息")
    @ToolParam(description = "查找联系人返回消息")
    private String resultMsg;

    @JsonProperty(required = false)
    @Description("联系人姓名")
    @ToolParam(description = "联系人姓名")
    private String name;

    @JsonProperty(required = false)
    @Description("联系人邮箱地址")
    @ToolParam(description = "联系人邮箱地址")
    private String email;
}
