package com.suibian.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.suibian.common.BaseResponse;
import com.suibian.common.ErrorCode;
import com.suibian.common.ResultUtils;
import com.suibian.exception.BusinessException;
import com.suibian.model.dto.user.UserLoginReqDTO;
import com.suibian.model.dto.user.UserRegisterReqDTO;
import com.suibian.model.vo.LoginUserVO;
import com.suibian.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     *
     * @param registerReqDTO 用户注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Long> userRegister(@Parameter(description = "用户注册请求参数") @RequestBody UserRegisterReqDTO registerReqDTO) {
        if (registerReqDTO == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        String userAccount = registerReqDTO.getUserAccount();
        String userPassword = registerReqDTO.getUserPassword();
        String checkPassword = registerReqDTO.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAM_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * @param userLoginReqDTO 用户登录请求
     * @return 返回用户VO
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginReqDTO userLoginReqDTO) {
        if (userLoginReqDTO == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        String userAccount = userLoginReqDTO.getUserAccount();
        String userPassword = userLoginReqDTO.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAM_ERROR);
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录的用户信息
     * 不需要传入任何参数
     * 根据satoken框架之间返回的用户信息封装成VO对象返回给前端
     *
     * @return 当前登录的用户信息
     */
    @GetMapping("/get/login")
    @SaCheckLogin
    @Operation(summary = "获取当前登录的用户信息")
    public BaseResponse<LoginUserVO> getLoginUser() {
        LoginUserVO loginUserVO = userService.getLoginUserVO();
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户登出
     * 不需要传入任何参数
     *
     * @return 登出结果
     */
    @DeleteMapping("/logout")
    @Operation(summary = "用户登出")
    public BaseResponse<Boolean> logout() {
        // 调用 satoken 的服务方法，注销用户
        StpUtil.logout();
        return ResultUtils.success(true);
    }
}
