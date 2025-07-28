package com.suibian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.ErrorCode;
import com.suibian.exception.BusinessException;
import com.suibian.mapper.UserMapper;
import com.suibian.model.entity.User;
import com.suibian.model.enums.UserStatusEnum;
import com.suibian.model.vo.LoginUserVO;
import com.suibian.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author suibian
 * @ description 针对表【user(用户表)】的数据库操作Service实现
 * @ createDate 2025-07-23 10:37:01
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 盐值，用于混淆密码
    private final static String SALT = "suibian";

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 注册成功，返回用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 参数校验是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        // 校验用户账号长度
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        // 校验密码两次输入是否正确
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
        // 校验密码长度是否符合要求
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PASSWORD_LENGTH_NOT_ENOUGH);
        }
        // 加同步锁防止用户账号并发操作
        // inter() 方法是String自带的方法，
        // 如果 userAccount 的值在常量池中则返回常量池的值
        // 否则将该值放入常量池再返回，
        // 确保线程安全，因为 String 地址不同的情况下，两个线程拿到的 userAccount 值不同
        // 这里使用用户账号作为锁，而不是定义一个属性锁lock，
        // 锁的粒度更小，锁的只是一个用户，不影响并发
        synchronized (userAccount.intern()) {
            // 用户账号重复性检验
            userAccountRepeatCheck(userAccount);
            // 密码加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 添加用户
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.ADD_FAIL);
            }
            return user.getId();
        }
    }

    /**
     * 账号重复性检查
     *
     * @param userAccount 用户账号
     */
    public void userAccountRepeatCheck(String userAccount) {
        LambdaQueryWrapper<User> userAccountEq = new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount);
        Long count = this.baseMapper.selectCount(userAccountEq);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_NAME_REPEAT);
        }
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 登录成功，返回用户信息
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        // 账号和密码是否符合规范
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAM_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PASSWORD_LENGTH_NOT_ENOUGH);
        }
        String encryptPassord = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserAccount, userAccount));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        if (!user.getUserPassword().equals(encryptPassord)) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (UserStatusEnum.isDisable(user.getUserStatus())) {
            throw new BusinessException(ErrorCode.USER_NOT_NORMAL);
        }
        StpUtil.login(user.getId());
        return getUserLoginVO(user);
    }

    /**
     * 将用户对象转换为登录用户VO对象
     *
     * @param user 用户对象
     * @return 返回登录用户VO对象
     */
    public LoginUserVO getUserLoginVO(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    /**
     * 获取当前登录用户
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser() {
        if (StpUtil.isLogin()) {
            return this.getById(StpUtil.getLoginIdAsLong());
        }
        return null;
    }

    @Override
    public LoginUserVO getLoginUserVO() {
        User loginUser = getLoginUser();
        return BeanUtil.copyProperties(loginUser, LoginUserVO.class);
    }
}




