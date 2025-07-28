package com.suibian.service;

import com.suibian.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibian.model.vo.LoginUserVO;

/**
* @author suibian
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-23 10:37:01
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 注册成功，返回用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 登录成功，返回用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录的用户信息
     * @return 当前登录的用户信息
     */
    User getLoginUser();

    /**
     * 获取当前登录的用户信息
     * @return 当前登录的用户信息
     */
    LoginUserVO getLoginUserVO();

}
