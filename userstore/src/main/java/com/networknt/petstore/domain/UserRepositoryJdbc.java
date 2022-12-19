package com.networknt.petstore.domain;

import com.networknt.petstore.util.IdentityGenerator;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryJdbc implements UserRepository{

    private final static Logger logger = LoggerFactory.getLogger(UserRepositoryJdbc.class);
    private DataSource dataSource ;

    public UserRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Save User to db using jdbc
    @Override
    public User save(User user) {
        user.setId(IdentityGenerator.generate());
        String psInsert = "INSERT INTO USERS (id, userName, firstName, lastName, password, phone, status)" +
                " VALUES (?, ?, ? , ? , ?, ?, ?)";

        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psInsert);
            stmt.setLong(1, user.getId());
            stmt.setString(2, user.getUserName());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getPassword());
            stmt.setString(6, user.getPhone());
            stmt.setLong(7, user.getUserStatus());

            int count = stmt.executeUpdate();
            if (count != 1) {
                logger.error("Failed to insert USER: {}", user.getId());
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return user;
    }

    @Override
    public User update(User user) {
        String psUpdate = "UPDATE USERS SET  userName = ?, firstName = ?, lastName = ?, " +
                "password = ?, phone = ?, status = ?";
        if (exists(user.getId())) {
            try (final Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(psUpdate);
                stmt.setString(1, user.getUserName());
                stmt.setString(2, user.getFirstName());
                stmt.setString(3, user.getLastName());
                stmt.setString(4, user.getPassword());
                stmt.setString(5, user.getPhone());
                stmt.setLong(6, user.getUserStatus());
                int count = stmt.executeUpdate();
                if (count != 1) {
                    logger.error("Failed to update User: {}", user.getId());
                }
            } catch (SQLException e) {
                logger.error("SqlException:", e);
            }
        }

        return user;
    }

    @Override
    public User findOne(Long id) {
        String psSelect = "SELECT id, userName, firstName, lastName, password, phone, status from USERS WHERE id = ?";
        User user = null;
        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() == false) {
                logger.error("incorrect fetch result {}", id);
            }
            while (rs.next()) {
                user = new User();
                user.setId(id);
                user.setUserName(rs.getString("userName"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setPassword(rs.getString("password"));
                user.setPhone(rs.getString("phone"));
                user.setUserStatus(rs.getInt("status"));

            }

        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return user;
    }

    @Override
    public User findByUserName(String userName) {
        String psSelect = "SELECT id, userName, firstName, lastName, password, phone, status from USERS WHERE userName = ?";
        User user = null;
        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            stmt.setString(1, userName);
            ResultSet rs = stmt.executeQuery();
            if (rs == null || rs.getFetchSize() > 1) {
                logger.error("incorrect fetch result {}", userName);
            }
            while (rs.next()) {
                user = new User();
                user.setUserName(userName);
                user.setId(rs.getLong("id"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setPassword(rs.getString("password"));
                user.setPhone(rs.getString("phone"));
                user.setUserStatus(rs.getInt("status"));
            }

        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList();
        String psSelect = "SELECT id, userName, firstName, lastName, password, phone, status from USERS  ";
        User user = null;
        try (final Connection connection = dataSource.getConnection()){
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                user = new User();
                user.setId(rs.getLong("id"));
                user.setUserName(rs.getString("userName"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setPassword(rs.getString("password"));
                user.setPhone(rs.getString("phone"));
                user.setUserStatus(rs.getInt("status"));

                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }
        return users;
    }



    @Override
    public void delete(Long id) {
        String psDelete = "DELETE from USERS where id = ?";
        User user = findOne(id);
        if (user != null) {
            try (final Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(psDelete);
                stmt.setLong(1, id);
                Integer rs = stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("SqlException:", e);
            }
        }
    }

    @Override
    public boolean exists(Long id) {
        User user = findOne(id);
        if (user == null) {
            return false;
        } else {
            return true;
        }
    }
}
