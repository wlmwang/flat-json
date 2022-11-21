package com.cn.ey.demo.controller;

import com.cn.ey.demo.controller.dto.UserDto;
import com.cn.ey.demo.domain.user.entity.UserBO;
import com.cn.ey.demo.domain.user.service.UserDomainService;
import com.cn.ey.demo.domain.user.valueobject.UserQueryVO;
import com.cn.ey.demo.controller.dto.UserQuery;
import com.cn.ey.demo.support.converter.JsonPackHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private UserDomainService service;

    @PostMapping("/search")
    public List<UserDto> search(@RequestBody UserQuery userQuery) {

        log.info("查询参数：{}", userQuery);

        UserQueryVO vo = new UserQueryVO();
        BeanUtils.copyProperties(userQuery, vo);
        List<UserBO> userBOList = service.search(vo);
        if (CollectionUtils.isEmpty(userBOList)) {
            return null;
        }

        log.info("查询结果：[{}]", userBOList);

        return userBOList.stream().map(bo -> {
            UserDto dto = new UserDto();
            BeanUtils.copyProperties(bo, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public UserDto add(@RequestBody List<UserDto> userDto) {
        log.info("接收参数：{}", userDto);

        // 添加
        UserBO bo = new UserBO();
        BeanUtils.copyProperties(userDto, bo);
        UserBO userBO = service.save(bo);

        log.info("操作结果：{}", userBO);

        UserDto dto = new UserDto();
        BeanUtils.copyProperties(userBO, dto);
        return dto;
    }

    @PostMapping("/batch")
    public List<UserDto> batch(@RequestBody List<UserDto> userDtoList) {
        log.info("接收参数：[{}]", userDtoList);

        List<UserBO> userBOList = userDtoList.stream().map(dto -> {
            UserBO bo = new UserBO();
            BeanUtils.copyProperties(dto, bo);
            return bo;
        }).collect(Collectors.toList());

        List<UserBO> boList = service.saveBatch(userBOList);
        if (CollectionUtils.isEmpty(boList)) {
            return null;
        }

        log.info("操作结果：[{}]", boList);

        return boList.stream().map(bo -> {
            UserDto dto = new UserDto();
            BeanUtils.copyProperties(bo, dto);
            return dto;
        }).toList();
    }

    @PostMapping("/mix/{id}")
    public UserDto mix(@PathVariable("id") String id, @RequestBody UserDto userDto) {
        log.info("接收参数：{}, [{}]", id, userDto);

        return userDto;
    }

    @PostMapping("/all")
    public Map<String, Object> all(@RequestParam Map<String, Object> objectMap) {
        log.info("接收参数：{}", objectMap);

        return objectMap;
    }

    @PostMapping("/with-binder")
    public UserDto withBinder(@RequestParam("name") String name, @RequestParam(value = "extension", required = false) UserDto userDto) {
        log.info("接收参数：{}, [{}]", name, userDto);

        return userDto;
    }

    @PostMapping("/str")
    public String str(@RequestBody String str) {
        log.info("接收参数：{}", str);

        return str;
    }

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UserDto.class, new UserConverter());
    }
    public static class UserConverter extends PropertiesEditor {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            UserDto userDto;
            try {
                assert text != null;
                userDto = JsonPackHttpMessageConverter.deserialize(UserDto.class, DemoController.class, text.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setValue(userDto);
        }
        @Override
        public String getAsText() {
            return getValue().toString();
        }
    }


    // todo 键值对
    @PostMapping("/form")
    public UserDto form(UserDto userDto) {
        log.info("接收参数：{}", userDto);

        return userDto;
    }
}
