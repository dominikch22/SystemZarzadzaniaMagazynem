package com.example.magazyn.entity;


import com.example.magazyn.model.LocationType;
import com.example.magazyn.model.Rack;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false)
    private LocationType locationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Location parentLocation;

    @OneToMany(mappedBy = "parentLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> subLocations = new ArrayList<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StockItem> stockItems;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company company;



    public void addSubLocation(Location subLocation) {
        if (subLocations == null) {
            subLocations = new ArrayList<>();
        }
        subLocations.add(subLocation);
        subLocation.setParentLocation(this);
    }

    public void removeSubLocation(Location subLocation) {
        if (subLocations != null) {
            subLocations.remove(subLocation);
        }
        subLocation.setParentLocation(null);
    }
}