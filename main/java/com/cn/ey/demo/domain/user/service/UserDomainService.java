package com.cn.ey.demo.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cn.ey.demo.domain.user.entity.UserBO;
import com.cn.ey.demo.domain.user.valueobject.UserQueryVO;
import com.cn.ey.demo.infrastructure.user.provider.UserService;
import com.cn.ey.demo.support.converter.JsonPackHttpMessageConverters;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * todo 直接依赖了infrastructure中的mybatis-plus
 */
@Service
public class UserDomainService {
    @Autowired
    private UserService service;

    public List<UserBO> search(@RequestBody UserQueryVO userQuery) {
        return service.list(buildQuery(userQuery, userQuery.getSearchRuleMap(), userQuery.getSortRuleList(), UserBO.class));
    }

    public UserBO save(UserBO userBO) {
        if (!service.save(userBO)) {
            return null;
        }
        LambdaQueryWrapper<UserBO> queryWrapper = Wrappers.lambdaQuery(UserBO.class).eq(UserBO::getId, userBO.getId());
        return service.getOne(queryWrapper);
    }

    public List<UserBO> saveBatch(List<UserBO> userBOList) {
        if (!service.saveBatch(userBOList)) {
            return null;
        }

        List<Long> idList = userBOList.stream().map(UserBO::getId).collect(Collectors.toList());
        return service.list(Wrappers.<UserBO>lambdaQuery().in(CollectionUtils.isNotEmpty(idList), UserBO::getId, idList));
    }

    private <T> QueryWrapper<T> buildQuery(Object query, Map<String, String> searchRuleMap, List<Map<String, String>> sortRuleList, Class<T> clazz) {
        QueryWrapper<T> queryWrapper = Wrappers.query();

        if (CollectionUtils.isEmpty(searchRuleMap)) {
            return Wrappers.emptyWrapper();
        }
        List<Field> nonePackField = JsonPackHttpMessageConverters.getNoneJsonPackEntityField(clazz);
        Field packField = JsonPackHttpMessageConverters.getJsonPackEntityField(clazz);
        MetaObject queryMetaObject = SystemMetaObject.forObject(query);

        // condition
        searchRuleMap.forEach((field, rule) -> {
            Object value = queryMetaObject.getValue(field);
            if (Objects.isNull(value)) {
                return;
            }

            boolean unpack_ = false;
            for (Field f : nonePackField) {
                if (f.getName().equals(field)) {
                    unpack_ = true;
                    break;
                }
            }
            if (!unpack_) {
                // field = packField.getName() + "->>'$." + field + "'";
                field = packField.getName() + "->>'" + field + "'"; // TODO postgresql
            }

            // 1. TODO 在postgresql中，检索json里非text的数据字段时，需要精确指定其类型：("extension"->>'age')::int
            // 2. TODO 在postgresql中，为了使用GIN索引，请使用一下sql语法
            // select id,name,extension from public.user WHERE "extension"::jsonb @> '{"addr": "浦东"}';
            // 3. TODO 在postgresql中，对于数组搜索，语法等同与常规字段匹配
            // select id,name,extension from public.user WHERE "extension"::jsonb @> '{"family": ["dad"]}';
            // 4. TODO 在postgresql中，对于字段提取。可以使用形如 "extension"->>'addr' 的语法，也可以使用如下函数形式。Mysql中使用JSON_EXTRACT提取
            // select id,name,extension from public.user WHERE jsonb_extract_path_text("extension", 'addr') = '浦东';
            // select id,name,extension from public.user WHERE jsonb_extract_path("extension", 'age') = 20;
            switch (rule) {
                case "=":
                    if (!unpack_) {
                        queryWrapper.apply(field + " = {0}", value);
                    } else {
                        queryWrapper.eq(field, value);
                    }
                    break;
                case ">":
                    if (!unpack_) {
                        queryWrapper.apply(field + " > {0}", value);
                    } else {
                        queryWrapper.gt(field, value);
                    }
                    break;
                case "<":
                    if (!unpack_) {
                        queryWrapper.apply(field + " < {0}", value);
                    } else {
                        queryWrapper.lt(field, value);
                    }
                    break;
                case "%like%":
                    if (!unpack_) {
                        queryWrapper.apply(field + " LIKE CONCAT('%',{0},'%')", value);
                    } else {
                        queryWrapper.like(field, value);
                    }
                    break;

                case "in":
                case "between":
                case "contain":
                    // queryWrapper.apply(StringUtils.isNotBlank(userDto.getFamily()), "extension ->> '$.family[0]' LIKE CONCAT('%',{0},'%')", userDto.getFamily());

            }
        });

        // order
        sortRuleList.forEach(rule -> {
            assert rule.size() == 1;
            rule.forEach((n, r) -> {
                String field = n;

                boolean unpack_ = false;
                for (Field f : nonePackField) {
                    if (f.getName().equals(n)) {
                        unpack_ = true;
                        break;
                    }
                }
                if (!unpack_) {
                    // field = packField.getName() + "->>'$." + field + "'";
                    field = packField.getName() + "->>'" + field + "'";   // TODO postgresql
                }

                switch (r.toUpperCase()) {
                    case "DESC":
                        queryWrapper.orderByDesc(field);
                        break;
                    case "ASC":
                        queryWrapper.orderByAsc(field);
                }
            });
        });

        return queryWrapper;
    }
}
