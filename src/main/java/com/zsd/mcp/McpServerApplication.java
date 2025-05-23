package com.zsd.mcp;

import com.zsd.mcp.mail.MailService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author zhangshdiong
 * @date 2025/5/8 11:11
 * @description 启动入口
 **/
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    /**
     * 注册工具到MCP Server
     * @param mailService 邮件服务工具
     * @return @link{ToolCallbackProvider}
     */
    @Bean
    public ToolCallbackProvider mailTools(MailService mailService) {
        return MethodToolCallbackProvider.builder().toolObjects(mailService).build();
    }
}
