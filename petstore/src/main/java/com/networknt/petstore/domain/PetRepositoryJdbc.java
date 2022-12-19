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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetRepositoryJdbc implements PetRepository {

    private static Logger logger = LoggerFactory.getLogger(PetRepositoryJdbc.class);

    private  DataSource dataSource ;

    public PetRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Pet save(Pet pet) {
        pet.setId(IdentityGenerator.generate());
        String psInsert = "INSERT INTO pet (id, name, status) VALUES (?, ?, ?)";

        try (final Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(psInsert)) {
            stmt.setLong(1, pet.getId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getStatus());

            int count = stmt.executeUpdate();
            if (count != 1) {
                logger.error("Failed to insert Pet: {}", pet.getId());
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return pet;
    }

    @Override
    public Pet update(Long id, Pet pet) {

        String psUpdate = "UPDATE pet SET name = ?, status = ? WHERE  id = ?";
        if (exists(id)) {
            try (final Connection connection = dataSource.getConnection()) {
                PreparedStatement stmt = connection.prepareStatement(psUpdate);
                stmt.setString(1, pet.getName());
                stmt.setString(2, pet.getStatus());
                int count = stmt.executeUpdate();
                if (count != 1) {
                    logger.error("Failed to update pet: {}", pet.getId());
                }
            } catch (SQLException e) {
                logger.error("SqlException:", e);
            }
        }

        return pet;

    }

    @Override
    public Pet findOne(Long id) {
        String psSelect = "SELECT id, name, status from pet WHERE id = ?";
        Pet pet = null;
        try (final Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs == null || rs.getFetchSize() > 1) {
                logger.error("incorrect fetch result {}", id);
            }
            while (rs.next()) {
                pet = new Pet();
                pet.setId(id);
                pet.setName(rs.getString("name"));
                pet.setStatus(rs.getString("status"));
            }

        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

        return pet;
    }

    @Override
    public boolean exists(Long id) {
        Pet pet = findOne(id);
        if (pet == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<Pet> findAll() {
        List<Pet> pets = new ArrayList();
        String psSelect = "SELECT id,  name, status from pet ";
        Pet pet = null;
        try (final Connection connection = dataSource.getConnection()){
            PreparedStatement stmt = connection.prepareStatement(psSelect);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pet = new Pet();
                pet.setId(rs.getLong("id"));
                pet.setName(rs.getString("name"));
                pet.setStatus(rs.getString("status"));
               pets.add(pet);
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }
        return pets;
    }

    @Override
    public void delete(Long id) {
        String psDelete = "DELETE from pet where id = ? ";
        Pet pet = findOne(id);
        if (pet != null) {
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
