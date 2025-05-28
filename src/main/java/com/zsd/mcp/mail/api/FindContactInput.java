package com.zsd.mcp.mail.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.jfr.Description;
import lombok.Builder;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author zhangshdiong
 * @date 2025/5/28 20:05
 * @description 查找联系人请求参数
 **/
@Data
@Builder
@Description("查找联系人请求参数")
public class FindContactInput {

    @JsonProperty(required = true)
    @Description("联系人姓名")
    @ToolParam(description = "联系人姓名")
    String name;
}
