# 通过`Spring-AI`快速构建`MCP Server`
![spring ai logo](images/spring_ai_logo.png)
- 科技部·研发中心·`应用架构组`
- 2025-05-21

## 1 MCP简介
`MCP`（`Model Context Protocol`，模型上下文协议）定义了应用程序和大模型之间交换上下文信息的方式。使开发者能够以一致的方式将各种数据源、工具和接口连接到大模型，就像USB让不同设备能够通过相同的标准连接一样。MCP的目标是创建一个通用标准，使AI应用程序的开发和集成变得更加简单和统一。

说人话：`MCP`就是一种大模型调用外部接口（`function/tool call`）的协议标准

引用一张网图，让大家更直观的感受`MCP解构`：
![网图 `MCP解构` `MCP概览` `mcp architecture`](images/mcp-architecture.jpeg)

## 2 MCP的作用

### 2.1 提示词工程
我们来尝试一个简单的场景，如下`图2`，让大模型告诉我们`今天是星期几？`
![`图2` `没有工具调用时大模型的表现`](images/no_tool_call.jpg)
大模型相当于一个脱机的、静态的智者，它并不知道当前时间，也不知道外面世界发生的变化。
在没有工具调用的情况下，大模型不能回答`今天是星期几？`这样的问题。

所以我们需要在提示词中提供我们的线索，比如这样问：`2025年5月20日是星期几？`，如下`图3`
![`图2` `2025年5月20日是星期几？`](images/prompt_current_date.png)

如果我们让大模型在回答我问题的时候，能够知道今天是几月几日，问题不就解决了吗。于是，（`function/tool call`）出现了。
当我们给`Agent`添加了一个获取当前时间的工具后，再问`今天是星期几？`，大模型就能解答了，如下`图4`
![`图4` `为Agent添加时间工具后`](images/with_tool_call.jpg)
这样我们就不用每次都把今天的日期输给大模型了，他自己去查，提升了自动化水平，显著提高用户体验。

`MCP`是将外部服务、数据与大模型结合的一种协议标准，有了`MCP`就能让大模型获取私域知识、完成特定指令的执行更加便捷。
我相信各大模型都会在后续的版本中强化`MCP`以提高调用的准确率

### 2.2 标准化
`OpenAI`、`Google`、`Qwen`、`DeepSeek`等各大模型都支持（`function/tool call`），但是在各LLM平台上，`function call API`
实现差异较大，开发者在切换LLM平台和大模型时需要重新对接，增加了适配成本，难以形成生态。
MCP的出现就是要解决这个痛点，充当大模型的"标准扩展坞"，让大模型能够轻松调用各种接口，并且`MCP SERVER`一次开发可以对接多个支持MCP的大模型和平台。

### 2.3 MCP的构成和调用过程
MCP由三个核心组件构成：`Host`、`Client`、`Server`。如下`图5`
- Host：  负责接收用户的提问并与大模型交互，比如：`Dify`、`Cursor`、`Coze`等；
- Client：当大模型决定需要调用外部工具时，`Host`中内置的`Client`会被激活，负责与恰当的`Server`建立连接并发送请求；
- Server：被`Client`调用的服务端。负责执行实际的指令，比如：查询数据库并返回对应的数据、调用邮箱的接口发送一份邮件、访问服务器文件系统并返回文件列表等。
  ![`图5` `MCP三个核心组件`](images/mcp_host_client_server.png)

MCP调用过程如下`图6-1`、`图6-2`
![`图6-1` `MCP调用过程`](images/mcp_invoke.jpeg)
![`图6-2` `MCP调用过程`](images/mcp-process.jpg)

## 3 构建MCP Sever

实现`MCP Server`的技术框架已经出了很多，`python`、`nodejs`、`java`、`golang`等语言都有对应的`SDK`

### 3.1 `Spring AI`构建`MCP Server`

#### 3.1.1 环境要求
- `jdk` >= `17`
- `spring-boot` >= `3.4.5`
- `spring-ai` >=`1.0.0`

#### 3.1.2 构建项目
`Spring AI`支持`STDIO`、`WebMVC`和`WebFlux`三种通讯方式构建`MCP Server`：
- `spring-ai-starter-mcp-server`：支持标准输入输出的基本服务，适合本机嵌入
- `spring-ai-starter-mcp-server-webmvc`：基于`Spring MVC`的`SSE`通讯实现的服务
- `spring-ai-starter-mcp-server-webflux`：基于`Spring WebFlux`的`SSE`通讯实现的服务

我这里使用`WebFlux`（推荐）构建一个通过行内邮箱发送邮件的`MCP Server`，步骤如下：

- 1、`pom.xml`中添加依赖

```xml
<!-- lookup parent from repository -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.5</version>
    <relativePath />
</parent>

<groupId>com.zhangsd</groupId>
<artifactId>mail-mcp-server</artifactId>
<version>1.0-SNAPSHOT</version>

<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- spring ai mcp server依赖 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
        <exclusions>
            <exclusion>
                <artifactId>spring-boot-starter-logging</artifactId>
                <groupId>org.springframework.boot</groupId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 邮件 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <!-- 日志 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
</dependencies>
```
- 2、创建启动入口类

```java
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
}
```

- 3、在`application.yml`中添加必要的配置

```yaml
spring:
  application:
    name: mail-mcp-server
  jackson:
    default-property-inclusion: non_null
  ai:
    mcp:
      server:
        # mcp server的名称，在查询tools的接口中会返回给client
        # 在多个server的情况下，准确的名称有利于大模型选中恰当的server
        name: ${spring.application.name}
        version: 1.0.0
        # 支持SYNC/ASYNC
        # SYNC：同步服务模式，默认值。专为简单`请求-响应`模式而设计
        # ASYNC：异步服务模式。针对非阻塞作进行了优化。适合耗时长的复杂任务
        type: SYNC
        # Enable/disable stdio transport
        # 开启后，将通过标准输入/输出进行通讯，通过进程间通信（IPC）实现，适合本地部署场景
        stdio: false
        # 调用initialize、notifications/initialized、tools/list、tools/call等接口的content-path
        # 默认：/sse/message
        sse-message-endpoint: /mcp/message
        # `Created new SSE connection for sessions`接口的content-path
        # 默认：/sse
        sse-endpoint: /sse
        # 资源更改通知
        resource-change-notification: true
        # 工具更改通知
        tool-change-notification: true
        # 提示词更改通知
        prompt-change-notification: true
  # 邮箱服务配置
  mail:
    default-encoding: UTF-8
    host: @dyn.mail.host@
    port: @dyn.mail.port@
    username: @dyn.mail.username@
    password: @dyn.mail.password@
    protocol: smtp
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          starttls:
            enable: true
            required: true
```

- 4、创建并编写`MCP Server`的业务逻辑（通过姓名查找收件地址、发送邮件）

`@Tool`注解是`Spring AI MCP`框架中用于快速暴露业务能力为`MCP协议工具`的核心注解，该注解将`Service方法`自动映射成`MCP协议工具`，并且通过注解的属性`description`对工具进行描述。

🚩使用`@Tool`注解标注要提供为`MCP协议工具`的方法，并使用`description`属性为工具添加合适的说明

🚩使用`@ToolParam`注解标注工具方法的参数，使用`description`属性为参数添加合适的说明，使用`required`属性标注参数是否必传，默认为`true`

```java
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
```

```java
package com.zsd.mcp.mail;

import com.zsd.mcp.mail.api.FindContactInput;
import com.zsd.mcp.mail.api.FindContactOutput;
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
   * @return 响应
   */
  SendMailOutput sendMail(SendMailInput input);

  /**
   * 通过姓名查找收邮件地址
   * @param input 请求
   * @return 响应
   */
  FindContactOutput findContact(FindContactInput input);

}
```

```java
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
```

- 5、注册工具到`MCP Server`

```java
    @Bean
    public ToolCallbackProvider mailTools(MailService mailService) {
        return MethodToolCallbackProvider.builder().toolObjects(mailService).build();
    }
```

#### 3.1.3 Client调用

##### 3.1.3.1 `Spring AI`构建`MCP Client`

`Spring AI`构建`MCP Client`[官方示例](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- 1、`pom.xml`中添加依赖

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.4.5</version>
  <relativePath /> <!-- lookup parent from repository -->
</parent>

<groupId>com.zhangsd</groupId>
<artifactId>mail-mcp-client</artifactId>
<version>1.0-SNAPSHOT</version>

<properties>
  <maven.compiler.source>21</maven.compiler.source>
  <maven.compiler.target>21</maven.compiler.target>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
    <exclusions>
      <exclusion>
        <artifactId>spring-boot-starter-logging</artifactId>
        <groupId>org.springframework.boot</groupId>
      </exclusion>
    </exclusions>
  </dependency>
  <dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
  </dependency>
  <!-- 日志依赖 -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
  </dependency>
</dependencies>
```
- 2、创建启动入口

```java
package com.zhangsd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class McpClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }
}
```

- 3、在`application.yml`中添加配置

```yaml
spring:
  main:
    web-application-type: none
  application:
    name: mail-mcp-client
  jackson:
    default-property-inclusion: non_null
  ai:
    model:
      chat: openai
    openai:
      base-url: @dyn.openai.base-url@
      api-key: @dyn.openai.api-key@
      chat:
        completions-path: /chat/completions
        options:
          # chat默认使用的模型名
          # Qwen/Qwen3-235B-A22B, deepseek-ai/DeepSeek-R1, deepseek-ai/DeepSeek-V3
          model: 'deepseek-ai/DeepSeek-V3'
    mcp:
      client:
        # 是否开启MCP Client，默认 true
        enabled: true
        # MCP客户端实例名
        name: mail-assistant
        # 版本号
        version: 1.0.0
        # 是否在创建时初始化客户端，默认 true
        initialized: true
        # 请求的超时时间，默认20s
        request-timeout: 20s
        # 客户端类型（SYNC/ASYNC）
        type: SYNC
        sse:
          connections:
            mail-mcp-server:
              url: http://127.0.0.1:9088
```

- 4、创建`ChatClient`并添加可用工具

```java
 package com.zhangsd.mcp.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author zhangshdiong
 * @date 2025/5/28 17:28
 * @description 邮件MCP客户端
 **/
@Component
public class MailClient {

  private final ChatClient chatClient;

  /**
   * 构造方法
   * @param chatClientBuilder spring ai根据yml中的spring.ai.model配置自动装配的对话客户端builder
   * @param toolCallbackProvider spring ai根据yml中的spring.ai.mcp.client.sse.connections配置自动装配的mcp工具provider
   */
  public MailClient(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider){
    //创建会话记忆窗口，最大10轮
    ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
    //创建与大模型的交互客户端
    this.chatClient = chatClientBuilder.defaultSystem("""
                    你是一个聪明的邮件助手，根据用户指令发送电子邮件，步骤如下：
                    1、先获取工具列表，找到需要的工具和对应的参数，所有的工具调用都要基于事实，不要猜测，不要假设；
                    2、根据用户的要求生成合理的邮件内容，根据邮件内容生成合适的邮件主题；邮件内容需要包含称呼、问候语、正文、祝福语；发件人是四川天府银行科技部的张仕东；
                    3、如果用户没有明确给出收件地址，而是姓名，请调用工具查找联系人的邮件地址；如果未查找到联系人，请提示用户提供准确的姓名或直接给出邮件地址；
                    4、在发送前请向用户确认即将发送的邮件标题、内容、收件地址等信息，得到用户肯定确认后调用对应的工具发送邮件；
                    5、在调用工具时，如果缺失参数请向用户询问，直到收集完全部的调用参数后调用工具完成发送，并将发送结果反馈给用户。""")
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()) //添加会话记忆
            .defaultToolCallbacks(toolCallbackProvider) //添加工具列表
            .build();
  }

  /**
   * 发送邮件
   * @param command 用户指令
   * @return 运行结果
   */
  public Flux<String> sendMail(String command){
    return chatClient.prompt(command)
            .stream()
            .content();
  }
}
```

- 5、创建一个用户的交互接口，这里使用命令行交互

```java
    /**
     * 创建一个命令行用户交互
     * @param mailClient 邮件智能体
     * @return 命令行输出
     */
    @Bean
    CommandLineRunner cli(MailClient mailClient){
        return args -> {
            var scanner = new Scanner(System.in);
            System.out.println("Agent: 我是一个非常聪明的邮件助手，能够帮您编辑并发送邮件。");
            while (true){
                System.out.print("User: ");
                //采集用户输入
                String userCommand = scanner.nextLine();
                if(StringUtils.isBlank(userCommand)){
                    System.out.println("System: 请输入您的指令");
                    continue;
                }
                if(StringUtils.containsAny(userCommand, "exit", "退出")){
                    break;
                }
                //向智能体发送用户指令
                Flux<String> sendResponse = mailClient.sendMail(userCommand);
                System.out.print("Agent: ");
                //大模型返回的流式输出
                sendResponse
                        .doOnNext(System.out::print)
                        .blockLast();
                System.out.println();
            }
        };
    }
```
- 6、运行效果

![`spring-ai-mcp-client`运行效果](images/spring-ai-mcp-client.jpg)

PS：
目前，原生的`Spring AI`暂不支持将非`DeepSeek`模型的推理过程流式输出，issue上已经有人提了适配Qwen3，个人推测在后续版本中应该会支持推理类模型而不止特定的推理模型。
同时也不支持将工具调用过程在返回中输出，官网上说将在后续版本中增加这个支持。

##### 3.1.3.2 `Dify MCP SSE 插件`
除了使用`Spring AI`构建一个`MCP Client`外，现在很多智能应用平台都支持MCP调用，如：`Dify`、`Cursor`、`COZE`等，我这里使用`Dify`
- 1、下载`MCP SSE`插件

在`Dify`的插件市场搜索`MCP SSE`关键字，选中对应的插件并点击安装，如下`图7`
![`图7` `MCP SSE插件`](images/mcp_sse_plugin.jpg)


- 2、配置`MCP Server`地址

`MCP SSE`插件安装好后，在插件列表中将其选中，点击`授权`，填写`MCP服务配置`，如下`图8`
![`图8` `MCP SSE插件配置`](images/mcp_sse_config.jpg)

```json
{
  "server_name": {
    "url": "http://${server_ip}:${server_port}/mcp",
    "headers": {},
    "timeout": 60,
    "sse_read_timeout": 300
  }
}
```

- 3、创建一个Agent，配置一个大模型，填写合适的提示词，并添加`MCP SSE`工具，如下`图9`

![`图9` `配置agent`](images/agent_config.jpg)

提示词示例：
```text
你是一个聪明能干的邮件助手，根据用户指令发送电子邮件，步骤如下：
1、先获取工具列表，找到需要的工具和对应的参数；
2、根据用户的要求生成合理的邮件内容，根据邮件内容生成合适的邮件主题；邮件内容需要包含称呼、问候语、正文、祝福语；
3、如果用户没有明确给出收件地址，而是姓名，请使用姓名拼音的小写加上@xxx.com后缀作为收件地址；如：张三的收件地址为zhangsan@tf.cn；
4、在发送前请向用户确认即将发送的邮件标题、内容、收件地址等信息，得到用户肯定确认后调用对应的工具发送邮件；
5、在调用工具时，如果缺失参数请向用户询问，直到收集完全部的调用参数后调用工具完成发送，并将发送结果反馈给用户。
```

- 4、使用效果
输入`给张**发送一封邮件，告诉他5月31日至6月2日端午节放假，放假期间注意出行安全，按时返岗`，
`Agent`会根据提示词组装邮件参数，并调用对应的`MCP工具`完成邮件发送，如下`图10-1`和`图10-2`，收到的邮件如`图11`
![`图10-1` `Agent调用mail-mcp-server`](images/mcp_send_mail_1.jpg)
![`图10-2` `Agent调用mail-mcp-server`](images/mcp_send_mail_2.jpg)
![`图11` `mail-mcp-server邮件发送结果`](images/received_mail.jpg)

## 4 快速启动

完整项目源码
- [mail-mcp-server](https://github.com/RTException/mail-mcp-server.git)
- [mail-mcp-client](https://github.com/RTException/mail-mcp-client.git)

- [快速启动包](https://github.com/RTException/mail-mcp-server/releases/download/1.0.0/mail-mcp.zip)

将压缩包下载到本地后执行以下命令可快速启动

```shell
unzip mail-mcp.zip
cd mail-mcp

# =====启动 mcp server=====
# 将配置文件中的邮箱服务配置修改为自己的邮件配置
vi start_mail-mcp-server.sh

sh start_mail-mcp-server.sh
# =========================

# =====启动 mcp client=====
# 将配置文件中的大模型配置修改为自己的
vi start_mail-mcp-client.sh

sh start_mail-mcp-client.sh
# =========================

```

## 5 常见问题

- 1、本机容器启动的`Dify`连不上本机启动的`MCP Server`

  配置`MCP Server`时，一定要使用本机的IP地址，在`Dify`中`localhost`、`127.0.0.1`指向的是容器。


- 2、`MCP Client`从`tool/list`查询回的接口参数缺少描述，导致大模型不能正确组装调用参数

  使用`@ToolParam`注解标注工具方法的每一个参数，并使用`description`属性为参数添加合适的说明，使用`required`属性标注参数是否必传，默认为`true`。
  如果参数是一个VO，请使用为VO的每一个成员变量添加`@ToolParam`注解。

  下面是`mail-mcp-server`的`tool/list`返回的服务端全部工具列表示例。
  准确的工具`name`和`description`有利于提高大模型选对工具的准确率，准确的参数名和描述则有利于提高大模型组装工具参数的准确率。
```json
{"mcp_sse_list_tools": "MCP Server tools list: \n[{'name': 'sendMail', 'description': 'Create and send a new email message.', 'parameters': {'type': 'object', 'properties': {'input': {'type': 'object', 'properties': {'body': {'type': 'string', 'description': '邮件内容'}, 'subject': {'type': 'string', 'description': '邮件主题'}, 'to': {'type': 'string', 'description': '接收邮件的地址'}}, 'required': ['body', 'subject', 'to'], 'description': '发送邮件的请求参数'}}, 'required': ['input'], 'additionalProperties': False}}]"}
```
```json
[
  {
    "name": "sendMail", 
    "description": "Create and send a new email message.", 
    "parameters": {
      "type": "object", 
      "properties": {
        "input": {
          "type": "object", 
          "properties": {
            "body": {
              "type": "string", 
              "description": "邮件内容"
            }, 
            "subject": {
              "type": "string", 
              "description": "邮件主题"
            }, 
            "to": {
              "type": "string", 
              "description": "接收邮件的地址"
            }
          }, 
          "required": ["body", "subject", "to"], 
          "description": "发送邮件的请求参数"
        }
      }, 
      "required": ["input"], 
      "additionalProperties": false
    }
  }
]
```

- 3、`tool/list`接口返回的工具参数多了一层`para0`
  
  其实也不影响使用，大模型会按照格式自行组装参数。

  请将`spring-boot`版本升级到`3.4.5`，`spring-ai`版本使用最新的正式版`1.0.0`。
  或者，通过[快速组织依赖的网站](https://start.spring.io/)重新创建一个干净的工程


- 4、调用`MCP工具`时，总是不能一次就调用成功，需要`ReAct`几次才能成功

  工具调用准确率，大参数模型优于小参数模型，思考模型优于不思考模型。在工具数量达到一定数量时，调用成功率也会下降。
  恰当的提示词能够提高调用成功率。

## 6 推荐网站

- 1、[快速组织依赖的网站](https://start.spring.io/)
- 2、[各种编程语言的`MCP SDK`](https://modelcontextprotocol.io/introduction) （`spring-ai-mcp`依赖了该项目的[`java sdk`](https://github.com/modelcontextprotocol/java-sdk)）
- 3、[`Spring AI`官方文档](https://docs.spring.io/spring-ai/reference/index.html)
- 4、[`Spring AI MCP`示例项目源码](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather)
- 5、[`MCP`工具集合](https://mcp.so/)
- 6、[`Dify`平台`MCP SSE`插件配置介绍](https://marketplace.dify.ai/plugins/junjiem/mcp_sse?language=zh-Hans)