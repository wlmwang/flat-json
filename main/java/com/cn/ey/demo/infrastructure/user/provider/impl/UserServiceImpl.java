package com.cn.ey.demo.infrastructure.user.provider.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.ey.demo.domain.user.entity.UserBO;
import com.cn.ey.demo.infrastructure.user.provider.UserService;
import com.cn.ey.demo.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserBO> implements UserService {
}
