package com.cnebula.nature.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.author")
public class Author {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(generator = "aid")
    @GenericGenerator(name = "aid", strategy = "increment")
    @Column(name = "aid", nullable = false)
    private Integer aid;

    @Column(name = "artid", nullable = false)
    //@OneToMany(targetEntity = Article.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Integer artid;

    @Column(name = "aufnms", nullable = true)
    private String aufnms;

    @Column(name = "aufnmsindex", nullable = true)
    private String aufnmsindex;

    @Column(name = "ausnm", nullable = true)
    private String ausnm;

    @Column(name = "ausnmindex", nullable = true)
    private String ausnmindex;

    @Column(name = "abbindex", nullable = true)
    private String abbindex;

    @Column(name = "ausort", nullable = true)
    private String ausort;

}
