package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "province")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "_name", length = 50, nullable = false)
    private String name;

    @Column(name = "_code", length = 20, nullable = false)
    private String code;


    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<District> districts;
    
    // Constructors
    public Province() {}

    public Province(Integer id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
