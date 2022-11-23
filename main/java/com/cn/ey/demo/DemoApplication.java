package com.cn.ey.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.ey.demo.controller.dto.BaseResponse;
import com.cn.ey.demo.controller.dto.UserDto;
import com.fasterxml.jackson.core.type.TypeReference;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
@MapperScan("com.cn.ey.demo.infrastructure.mapper")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);


        /*UserDto dto1 = new UserDto();
        dto1.setName("小红");
        dto1.setExtension(new HashMap<>(){{
            put("age", 20);
            put("sex", "男");
        }});

        UserDto dto2 = new UserDto();
        dto2.setName("小红红");
        dto2.setExtension(new HashMap<>(){{
            put("age", 21);
            put("sex", "女");
        }});
        List<UserDto> dtoList = new ArrayList<>() {{
            add(dto1);
            add(dto2);
        }};
        Page<UserDto> objectPage = Page.<UserDto>of(1, 2, 100L);
        objectPage.setRecords(dtoList);*/

        //ResolvableType resolvableType = ResolvableType.forType(new TypeReference<BaseResponse<Page<UserDto>>>() {}.getType());
        //ResolvableType resolvableType1 = resolvableType.forType(resolvableType.getGeneric(0).getType());
        //System.out.printf("" + resolvableType);
    }
}
