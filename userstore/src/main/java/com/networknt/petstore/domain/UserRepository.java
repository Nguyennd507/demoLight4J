package com.networknt.petstore.domain;

import java.util.List;

public interface UserRepository {

    <S extends User> S save(S entity);
    <S extends User> S update(S entity);
    User findOne(Long id);
    User findByUserName(String userName);
    List<User> findAll();
    void delete(Long id);
    boolean exists(Long id);
}
