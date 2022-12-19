package com.networknt.petstore.service;

import com.networknt.petstore.domain.Pet;

import java.util.List;

public interface PetService {
    List<Pet> getPets();
    void deletePetById(Long id);
    Pet getPetById(Long id);
    void addPet(Pet pet);
    void updatePet(Long id, Pet pet);
}
