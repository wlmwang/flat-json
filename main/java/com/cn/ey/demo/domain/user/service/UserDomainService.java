package com.cn.ey.demo.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cn.ey.demo.domain.user.entity.UserBO;
import com.cn.ey.demo.domain.user.valueobject.UserQueryVO;
import com.cn.ey.demo.infrastructure.user.provider.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * todo 直接依赖了infrastructure中的mybatis-plus
 */
@Service
public class UserDomainService {
    @Autowired
    private UserService service;

    public List<UserBO> search(@RequestBody UserQueryVO userQuery) {
        LambdaQueryWrapper<UserBO> queryWrapper = Wrappers.lambdaQuery(UserBO.class);
        // queryWrapper.select(User::getAge);
        queryWrapper.eq(!Objects.isNull(userQuery.getId()), UserBO::getId, userQuery.getId());
        queryWrapper.like(!Objects.isNull(userQuery.getName()), UserBO::getName, userQuery.getName());

        // 字段：extension->>'$.age' = {0}
        // 数组：JSON_CONTAINS(extension, JSON_OBJECT('family', {0}))

        queryWrapper.apply(StringUtils.isNotBlank(userQuery.getFamily()), "JSON_CONTAINS(extension, JSON_OBJECT('family', {0}))", userQuery.getFamily());
        // queryWrapper.apply(StringUtils.isNotBlank(userDto.getFamily()), "extension ->> '$.family[0]' LIKE CONCAT('%',{0},'%')", userDto.getFamily());

        if (!Objects.isNull(userQuery.getAge())) {
            queryWrapper.and(wrapper -> {
                wrapper.apply(!Objects.isNull(userQuery.getAge()), "extension->>'$.age' is null").or();
                wrapper.apply(!Objects.isNull(userQuery.getAge()), "extension->>'$.age' = {0}", userQuery.getAge());
            });
        }
        queryWrapper.last("ORDER BY extension->>'$.age' DESC");

        return service.list(queryWrapper);
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
}
