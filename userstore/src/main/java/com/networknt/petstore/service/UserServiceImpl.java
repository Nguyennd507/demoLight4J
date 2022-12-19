package com.networknt.petstore.service;

import com.networknt.petstore.domain.User;
import com.networknt.petstore.domain.UserRepository;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserServiceImpl implements UserService{
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static UserRepository userRepository = SingletonServiceFactory.getBean(UserRepository.class);

    public UserServiceImpl () {

    }

    @Override
    public List<User> getListUser() {
        logger.info("UserService get all users");
        return userRepository.findAll();
    }

    @Override
    public void addUser(User user) {
        logger.info("UserService add new User");
         userRepository.save(user);

    }

    @Override
    public void updateUser(User user) {
        logger.info("UserService update User");
        userRepository.update(user);
    }

    @Override
    public User getUserById(Long id) {
        logger.info("User Service get by Id " + id);
        return userRepository.findOne(id);
    }

    @Override
    public User getUserByUserName(String userName) {
        logger.info("User Service get by User Name " + userName);
        return userRepository.findByUserName(userName);
    }

    @Override
    public void deleteUserById(Long id) {
        logger.info("User Service delete user by Id " + id);
        userRepository.delete(id);
    }
}
