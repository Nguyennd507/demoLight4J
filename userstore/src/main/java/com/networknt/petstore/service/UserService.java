package com.networknt.petstore.service;

import com.networknt.petstore.domain.User;

import java.util.List;

public interface UserService {
    List<User> getListUser();

    void addUser(User user);

    void updateUser(User user);

    User getUserById(Long id);

    User getUserByUserName(String userName);

    void deleteUserById(Long id);

}
