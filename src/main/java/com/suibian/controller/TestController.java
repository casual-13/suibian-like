package com.suibian.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "测试文档")
public class TestController {

    @GetMapping("/test")
    public String test() {
        log.info("test");
        return "test";
    }
}
