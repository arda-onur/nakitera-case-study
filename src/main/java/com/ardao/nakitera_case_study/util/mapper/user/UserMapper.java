package com.ardao.nakitera_case_study.util.mapper.user;

import com.ardao.nakitera_case_study.entity.User;
import com.ardao.nakitera_case_study.request.customer.UserRequest;

public class UserMapper {
    public static User toEntity(UserRequest userRequest){
        User mappedUser = new User();
        mappedUser.setUsername(userRequest.username());
        mappedUser.setPassword(userRequest.password());
        return mappedUser;
    }

    private UserMapper(){

    }
}
