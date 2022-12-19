package com.networknt.petstore.service;

import com.networknt.petstore.domain.Pet;
import com.networknt.petstore.domain.PetRepository;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PetServiceImpl implements PetService {
    private static final Logger logger = LoggerFactory.getLogger(PetServiceImpl.class);
    private static PetRepository petRepository = SingletonServiceFactory.getBean(PetRepository.class);;

    public PetServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public List<Pet> getPets() {
        logger.info("PetService get all pets");
          return petRepository.findAll();
    }

    @Override
    public void deletePetById(Long id) {
        logger.info("PetService delete by Id ++" + id);
        petRepository.delete(id);
    }

    @Override
    public Pet getPetById(Long id) {
        logger.info("PetService get by Id ++" + id);
      return  petRepository.findOne(id);
    }

    @Override
    public void addPet(Pet pet) {
        logger.info("PetService add new Pet ");
        petRepository.save(pet);

    }
    @Override
    public void updatePet(Long id,Pet pet) {
        logger.info("PetService update Pet ");
        petRepository.update(id, pet);

    }


}
