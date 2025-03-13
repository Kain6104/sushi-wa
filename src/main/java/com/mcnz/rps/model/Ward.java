package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ward")
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "_name", length = 50, nullable = false)
    private String name;

    @Column(name = "_prefix", length = 20, nullable = false)
    private String prefix;

    @ManyToOne
    @JoinColumn(name = "_province_id", nullable = false)
    private Province province;

    @ManyToOne
    @JoinColumn(name = "_district_id", nullable = false)
    private District district;

    // Constructors
    public Ward() {}

    public Ward(Integer id, String name, String prefix, Province province, District district) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.province = province;
        this.district = district;
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }
}
