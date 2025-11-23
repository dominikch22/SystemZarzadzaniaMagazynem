package com.example.magazyn.service;

import com.example.magazyn.dto.CreateLocationRequest;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Location;
import com.example.magazyn.entity.User;
import com.example.magazyn.model.LocationType;
import com.example.magazyn.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Location createLocation(CreateLocationRequest request, User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }

        Location newLocation = new Location();
        newLocation.setName(request.getName());
        newLocation.setLocationType(request.getLocationType());
        newLocation.setCompany(company);

        if (request.getParentId() != null) {
            Location parentLocation = locationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Lokalizacja nadrzędna o ID " + request.getParentId() + " nie została znaleziona."));

            if (!parentLocation.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Brak uprawnień do dodawania pod-lokalizacji w tej strukturze.");
            }

            validateHierarchy(request.getLocationType(), parentLocation.getLocationType());

            newLocation.setParentLocation(parentLocation);

        } else {
            if (request.getLocationType() != LocationType.WAREHOUSE) {
                throw new IllegalArgumentException("Tylko lokalizacja typu WAREHOUSE (magazyn) może być utworzona bez lokalizacji nadrzędnej.");
            }
        }

        return locationRepository.save(newLocation);
    }

    public List<Location> getAllLocations(User user) {
        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie jest przypisany do żadnej firmy.");
        }
        return locationRepository.findAllByCompany(company);
    }

    private void validateHierarchy(LocationType childType, LocationType parentType) {
        switch (childType) {
            case RACK:
                if (parentType != LocationType.WAREHOUSE) {
                    throw new IllegalArgumentException("Regał (RACK) może być utworzony tylko wewnątrz magazynu (WAREHOUSE).");
                }
                break;
            case SHELF:
                if (parentType != LocationType.RACK) {
                    throw new IllegalArgumentException("Półka (SHELF) może być utworzona tylko wewnątrz regału (RACK).");
                }
                break;
            case WAREHOUSE:
                throw new IllegalArgumentException("Magazyn (WAREHOUSE) nie może być pod-lokalizacją.");
        }
    }
}