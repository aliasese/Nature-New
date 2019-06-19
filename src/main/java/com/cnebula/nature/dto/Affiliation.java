package com.cnebula.nature.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.aff")
public class Affiliation {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(generator = "affid", strategy = GenerationType.AUTO)
    @GenericGenerator(name = "affid", strategy = "native")
    @Column(name = "affid", nullable = false)
    private Integer affid;

    @Column(name = "artid", nullable = false)
    //@OneToMany(targetEntity = Article.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Integer artid;

    @Column(name = "aff", nullable = false)
    private String aff;

    @Column(name = "affsort", nullable = true)
    private Integer affsort;

}
