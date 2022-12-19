package com.networknt.petstore.domain;

import com.networknt.petstore.util.IdentityGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderRepositoryJdbc implements OrderRepository {

    private static Logger logger = LoggerFactory.getLogger(OrderRepositoryJdbc.class);

    private DataSource dataSource ;

    public OrderRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //Save Order to db using jdbc
    @Override
    public Order save(Order order) {
        order.setId(IdentityGenerator.generate());
        String psInsert = "INSERT INTO orders (id, petId, quantity, shipdate, status) VALUES (?, ?, ? , ? , ?)";

        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psInsert);
            stmt.setLong(1, order.getId());
            stmt.setLong(2, order.getPetId());
            stmt.setLong(3, order.getQuantity());
            stmt.setDate(4, order.getShipDate());
            stmt.setString(5, order.getStatus());
            int count = stmt.executeUpdate();
            if (count != 1) {
                logger.error("Failed to insert Pet: {}", order.getId());
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return order;
    }

    @Override
    public Order update(Order order) {

        String psUpdate = "UPDATE ORDERS SET petId = ?, quantity = ?, shipdate = ? , status = ? WHERE  id = ?";
        if (exists(order.getId())) {
            try (final Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(psUpdate);
                stmt.setLong(1, order.getPetId());
                stmt.setLong(2, order.getQuantity());
                stmt.setDate(3, order.getShipDate());
                stmt.setString(4, order.getStatus());
                int count = stmt.executeUpdate();
                if (count != 1) {
                    logger.error("Failed to update Status: {}", order.getId());
                }
            } catch (SQLException e) {
                logger.error("SqlException:", e);
            }
        }

        return order;
    }

    @Override
    public Order findOne(Long id) {

        String psSelect = "SELECT id, petId, quantity, shipDate, status from ORDERS WHERE id = ?";
        Order order = null;
        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs == null || rs.getFetchSize() > 1) {
                logger.error("incorrect fetch result {}", id);
            }
            while (rs.next()) {
                order = new Order();
                order.setId(id);
                order.setPetId(rs.getLong("petId"));
                order.setQuantity(rs.getInt("quantity"));
                order.setShipDate(rs.getDate("shipDate"));
                order.setStatus(rs.getString("status"));

            }

        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return order;
    }

    @Override
    public boolean exists(Long id) {
        Order order = findOne(id);
        if (order == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList();
        String psSelect = "SELECT id,  petId, quantity, shipDate, status from orders ";
        Order order = null;
        try (final Connection connection = dataSource.getConnection()){
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                order = new Order();
                order.setId(rs.getLong("id"));
                order.setPetId(rs.getLong("petId"));
                order.setQuantity(rs.getInt("quantity"));
                order.setShipDate(rs.getDate("shipDate"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }
        return orders;
    }

    @Override
    public void delete(Long id) {
        String psDelete = "DELETE from orders where id = ?";
        Order order = findOne(id);
        if (order != null) {
            try (final Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(psDelete);
                ResultSet rs = stmt.executeQuery();
            } catch (SQLException e) {
                logger.error("SqlException:", e);
            }
        }
    }

    @Override
    public void deleteAll() {
            //to do
    }
}
