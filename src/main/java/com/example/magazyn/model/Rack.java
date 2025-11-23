package com.example.magazyn.model;

import lombok.Data;

import java.util.List;

@Data
public class Rack {
    private String id;
    private String name;
    private List<Shelf> shelves;
}
