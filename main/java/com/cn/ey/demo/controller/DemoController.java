package com.cn.ey.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.ey.demo.controller.dto.BaseResponse;
import com.cn.ey.demo.controller.dto.BaseResponseTest;
import com.cn.ey.demo.controller.dto.UserDto;
import com.cn.ey.demo.domain.user.entity.UserBO;
import com.cn.ey.demo.domain.user.service.UserDomainService;
import com.cn.ey.demo.domain.user.valueobject.UserQueryVO;
import com.cn.ey.demo.controller.dto.UserQuery;
import com.cn.ey.demo.support.converter.JsonPackHttpMessageConverters;
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

    @PostMapping("/search-a")
    public UserDto searchA(@RequestBody UserQuery userQuery) {
        List<UserDto> userDtoList = (List<UserDto>) search(userQuery);

        return userDtoList.get(0);
    }

    @PostMapping("/search")
    public List<? extends UserDto> search(@RequestBody UserQuery userQuery) {
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

            UserDto c = new UserDto();
            c.setId(101L);
            c.setName("xxx");
            c.setExtension(dto.getExtension());
            // dto.setChildren(c);
            // dto.setChildren(Collections.singletonList(c));
            // dto.setChildren(Collections.singletonList(Collections.singletonList(Collections.singletonList(c))));
            /*dto.setChildren(new HashMap<>() {{
                put("ccc", Collections.singletonList(Collections.singletonList(c)));
            }});*/

            Page<List<UserDto>> page = new Page<>(1L, 10L, false);
            Page<List<UserDto>> objectPage = Page.<List<UserDto>>of(page.getCurrent(), page.getSize(), 100L);
            objectPage.setRecords(Collections.singletonList(Collections.singletonList(c)));
            //dto.setChildren(objectPage);

            // dto.setChildren(BaseResponse.success("zzz", (Collections.singletonList(objectPage))));
            dto.setChildren(BaseResponse.success("zzz", Collections.singletonList(Collections.singletonList(objectPage))));

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public <T extends UserDto> UserDto add(@RequestBody T userDto) {
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
    public List<UserDto> batch(@RequestBody List<? extends UserDto> userDtoList) {
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

    @PostMapping("/response-search")
    public BaseResponse<Page<UserDto>, String> responseSearch(@RequestBody UserQuery userQuery) {
        log.info("查询参数：{}", userQuery);

        // 分页对象
        Page<UserDto> page = new Page<>(1L, 10L, false);

        // 执行查询
        UserQueryVO vo = new UserQueryVO();
        BeanUtils.copyProperties(userQuery, vo);
        List<UserBO> userBOList = service.search(vo);
        if (CollectionUtils.isEmpty(userBOList)) {
            return null;
        }
        log.info("查询结果：[{}]", userBOList);

        List<UserDto> dtoList = userBOList.stream().map(bo -> {
            UserDto dto = new UserDto();
            BeanUtils.copyProperties(bo, dto);
            return dto;
        }).collect(Collectors.toList());

        Page<UserDto> objectPage = Page.<UserDto>of(page.getCurrent(), page.getSize(), 100L);
        objectPage.setRecords(dtoList);

        return BaseResponse.success(objectPage, "操作成功");
    }

    @PostMapping("/response-search-test")
    public BaseResponseTest<String, IPage<? extends UserDto>> responseSearchTest(@RequestBody UserQuery userQuery) {
        log.info("查询参数：{}", userQuery);

        // 分页对象
        Page<UserDto> page = new Page<>(1L, 10L, false);

        // 执行查询
        UserQueryVO vo = new UserQueryVO();
        BeanUtils.copyProperties(userQuery, vo);
        List<UserBO> userBOList = service.search(vo);
        if (CollectionUtils.isEmpty(userBOList)) {
            return null;
        }
        log.info("查询结果：[{}]", userBOList);

        List<UserDto> dtoList = userBOList.stream().map(bo -> {
            UserDto dto = new UserDto();
            BeanUtils.copyProperties(bo, dto);
            return dto;
        }).collect(Collectors.toList());

        Page<UserDto> objectPage = Page.<UserDto>of(page.getCurrent(), page.getSize(), 100L);
        objectPage.setRecords(dtoList);

        return BaseResponseTest.success(objectPage, "操作成功");
    }

    @PostMapping("/response-save")
    public BaseResponse<UserDto, String> responseSave(@RequestBody UserDto userDto) {
        log.info("接收参数：{}", userDto);

        // 添加
        UserBO bo = new UserBO();
        BeanUtils.copyProperties(userDto, bo);
        UserBO userBO = service.save(bo);

        log.info("操作结果：{}", userBO);

        UserDto dto = new UserDto();
        BeanUtils.copyProperties(userBO, dto);
        return BaseResponse.success(userDto);
    }

    @PostMapping("/page-query")
    public BaseResponse<Page<? extends UserDto>, String> pageQuery(@RequestBody BaseResponse<Page<? extends UserDto>, String> query) {
        log.info("query:" + query);

        return query;
    }

    @PostMapping("/batch-multi")
    public List<List<List<? extends UserDto>>> batchMulti(@RequestBody List<List<List<? extends UserDto>>> userDtoList) {
        log.info("接收参数：[{}]", userDtoList);
        return userDtoList;
    }

    @PostMapping("/mix/{id}")
    public UserDto mix(@PathVariable("id") String id, @RequestBody UserDto userDto) {
        log.info("接收参数：{}, [{}]", id, userDto);

        UserDto dto = new UserDto();
        dto.setExtension(userDto.getExtension());
        return userDto;
    }

    @PostMapping("/with-binder")
    public UserDto withBinder(@RequestParam("name") String name, @RequestParam(value = "extension", required = false) UserDto userDto) {
        log.info("接收参数：{}, [{}]", name, userDto);

        return userDto;
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
                userDto = JsonPackHttpMessageConverters.deserialize(UserDto.class, text.getBytes(StandardCharsets.UTF_8));
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




    // TODO 键值对
    @PostMapping("/form")
    public UserDto form(UserDto userDto) {
        log.info("接收参数：{}", userDto);

        return userDto;
    }


    // 功能测试
    @PostMapping("/map")
    public Map<String, Object> map(@RequestParam Map<String, Object> objectMap) {
        log.info("接收参数：{}", objectMap);

        return objectMap;
    }
    @PostMapping("/string")
    public String string(@RequestParam String string) {
        log.info("接收参数：{}", string);

        return string;
    }
}
