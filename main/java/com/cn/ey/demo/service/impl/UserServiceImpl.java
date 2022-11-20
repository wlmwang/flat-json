package com.cn.ey.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cn.ey.demo.entity.User;
import com.cn.ey.demo.mapper.UserMapper;
import com.cn.ey.demo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
