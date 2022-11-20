package com.cn.ey.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cn.ey.demo.converter.JsonPackHttpMessageConverter;
import com.cn.ey.demo.dto.UserQuery;
import com.cn.ey.demo.entity.User;
import com.cn.ey.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private UserService userService;

    @PostMapping("/search")
    public List<User> search(@RequestBody UserQuery userQuery) {
        System.out.printf("查询参数：" + userQuery);

        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class);
        // queryWrapper.select(User::getAge);
        queryWrapper.eq(!Objects.isNull(userQuery.getId()), User::getId, userQuery.getId());
        queryWrapper.like(!Objects.isNull(userQuery.getName()), User::getName, userQuery.getName());

        // 字段：extension->>'$.age' = {0}
        // 数组：JSON_CONTAINS(extension, JSON_OBJECT('family', {0}))
        queryWrapper.apply(!Objects.isNull(userQuery.getAge()), "extension->>'$.age' is null");
        queryWrapper = queryWrapper.or(!Objects.isNull(userQuery.getAge()) || StringUtils.isNotBlank(userQuery.getFamily()), qw -> {
            qw.apply(!Objects.isNull(userQuery.getAge()), "extension->>'$.age' = {0}", userQuery.getAge());
            qw.apply(StringUtils.isNotBlank(userQuery.getFamily()), "JSON_CONTAINS(extension, JSON_OBJECT('family', {0}))", userQuery.getFamily());
            // queryWrapper.apply(StringUtils.isNotBlank(userDto.getFamily()), "extension ->> '$.family[0]' LIKE CONCAT('%',{0},'%')", userDto.getFamily());
        });
        queryWrapper.last("ORDER BY extension->>'$.age' DESC");

        List<User> list = userService.list(queryWrapper);

        System.out.printf("搜索数据" + list);

        return list;
    }

    @PostMapping("/add")
    public User add(@RequestBody User user) {
        System.out.printf("参数：" + user);

        // 添加
        userService.save(user);

        // 查询
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class).eq(User::getId, user.getId());
        User one = userService.getOne(queryWrapper);

        return one;
    }

    @PostMapping("/batch")
    public List<User> batch(@RequestBody List<User> users) {
        System.out.printf("list 接收参数" + users);

        userService.saveBatch(users);

        List<Long> idList = users.stream().map(User::getId).collect(Collectors.toList());
        List<User> userList = userService.list(Wrappers.<User>lambdaQuery().in(CollectionUtils.isNotEmpty(idList), User::getId, idList));
        return userList;
    }

    @PostMapping("/mix/{id}")
    public User mix(@PathVariable("id") String id, @RequestBody User user) {
        System.out.printf("id=" + id);
        return user;
    }

    @PostMapping("/all")
    public Map<String, Object> all(@RequestParam Map<String, Object> user) {
        return user;
    }

    @PostMapping("/form")
    public User form(User user) {
        return user;
    }

    @PostMapping("/with-binder")
    public User withBinder(@RequestParam("user") User user, @RequestParam("name") String name) {
        System.out.printf("name=" + name);
        return user;
    }

    @PostMapping("/str")
    public String str(@RequestBody String str) {
        return str;
    }

    @PostMapping("/noop")
    public String noop() {
        return "noop";
    }


    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(User.class, new UserConverter());
    }
    public static class UserConverter extends PropertiesEditor {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            User user;
            try {
                assert text != null;
                user = JsonPackHttpMessageConverter.converter(User.class, DemoController.class, text.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setValue(user);
        }
        @Override
        public String getAsText() {
            return getValue().toString();
        }
    }
}
