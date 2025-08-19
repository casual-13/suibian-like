package com.suibian;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.suibian.common.BaseResponse;
import com.suibian.common.ErrorCode;
import com.suibian.model.dto.user.UserLoginReqDTO;
import com.suibian.model.dto.user.UserRegisterReqDTO;
import com.suibian.model.entity.User;
import com.suibian.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuibianLikeApplicationTests {

    @Resource
    UserService userService;

    @Resource
    MockMvc mockMvc;

    /**
     * 创建用户
     */
    @Test
    void addUser() throws Exception {
        // 默认创建5000个用户
        int initCount = 5000;
        // 用 Set 集合保存已经创建的用户
        HashSet<String> hashSet = new HashSet<>(initCount);

        // 循环创建
        for (int i = 0; i < initCount; i++) {
            // 随机生成用户账号
            String userAccount = RandomUtil.randomString(8);
            // 如果账号存在就重新生成
            while (hashSet.contains(userAccount)) {
                userAccount = RandomUtil.randomString(8);
            }
            // 将账号保存到 Set 中
            hashSet.add(userAccount);

            // 统一生成账号的密码都为 “123456789”
            String userPassword = "123456789";

            // 构建注册请求表
            UserRegisterReqDTO userRegisterReqDTO = new UserRegisterReqDTO();
            userRegisterReqDTO.setUserAccount(userAccount);
            userRegisterReqDTO.setUserPassword(userPassword);
            userRegisterReqDTO.setCheckPassword(userPassword);

            // 将请求表转化为 Json 字符串
            String requestBody = JSONUtil.toJsonStr(userRegisterReqDTO);

            // 构建 post请求
            MockHttpServletRequestBuilder requestBuilder = post("/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody);

            // 发送请求
            ResultActions resultActions = mockMvc.perform(requestBuilder);

            // 解析响应结果
            MvcResult mvcResult = resultActions.andReturn();
            String resultStr = mvcResult.getResponse().getContentAsString();

            // 将响应结果反序列化
            BaseResponse<Long> commonResult = JSONUtil.toBean(resultStr, new TypeReference<BaseResponse<Long>>() {
            }, true);

            // 断言判断结果为成功，并且获取用户 id 不为空
            assertThat(commonResult.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
            assertThat(commonResult.getData()).isNotNull();
        }
    }

    /**
     * 登录并导出 session 到 csv 文件
     */
    @Test
    void testLoginAndExportSessionToCsv() throws Exception {
        // 获取所有的用户列表
        List<User> users = userService.list();

        // 断言判断用户集合不为空
        assertThat(users).isNotNull();

        // 使用 try-with-resources 创建文件输出流，保证流关闭
        try (PrintWriter pw = new PrintWriter(new FileWriter("token_output.csv"))) {
            // 判断文件是否为空，如果第一个编写，将头文件信息写入
            if (new File("token_output.csv").length() == 0) {
                pw.println("userId,token,timestamp");
            }

            for (User user : users) {
                // 获取用户ID
                Long userId = user.getId();

                // 构建登录请求表
                UserLoginReqDTO userLoginReqDTO = new UserLoginReqDTO();
                userLoginReqDTO.setUserAccount(user.getUserAccount());
                userLoginReqDTO.setUserPassword(user.getUserPassword());
                if ("suibian".equals(user.getUserName())) {
                    userLoginReqDTO.setUserPassword("12345678");
                }

                // 将请求表转化为 Json 字符串
                String requestBody = JSONUtil.toJsonStr(userLoginReqDTO);

                // 构建 post请求
                MockHttpServletRequestBuilder requestBuilder = post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody);

                // 发出请求
                MvcResult mvcResult = mockMvc.perform(requestBuilder)
                        .andExpect(status().isOk())    // 断言判断请求是否成功
                        .andReturn();

                // 解析响应结果
                List<String> headers = mvcResult.getResponse().getHeaders("Set-Cookie");
                // 断言判断 Cookie 集合不能为空
                assertThat(headers).isNotNull();

                // 获得token值
                Optional<String> tokenValue = extractCookieValue(headers);

                String token = tokenValue.orElseThrow(() -> new RuntimeException("No suibian-like-token found in response"));

                // 写入 csv 文件
                pw.printf("%d,%s,%s%n", userId, token, LocalDateTime.now());

                // 输出日志
                System.out.println("✅ 写入 CSV：" + userId + " -> " + token);
            }
        }
    }

    /**
     * 从 Cookie 中提取 token 值
     *
     * @param cookieHeader Cookie 头信息
     * @return token 值
     */
    public static Optional<String> extractCookieValue(List<String> cookieHeader) {
        // Cookie 名称
        String cookieName = "suibian-token";
        return cookieHeader.stream()
                // 过滤出包含 Cookie 名称
                .filter(cookie -> cookie.startsWith(cookieName + "="))
                // 获取 Cookie 值
                .map(cookie -> cookie.split(";")[0])
                .map(cookie -> cookie.split("=")[1])
                .findFirst();
    }

}
