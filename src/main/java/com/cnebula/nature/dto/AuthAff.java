package com.cnebula.nature.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.author_aff")
public class AuthAff {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(generator = "au_affid", strategy = GenerationType.AUTO)
    @GenericGenerator(name = "au_affid", strategy = "native")
    @Column(name = "au_affid", nullable = false)
    private Integer auAffid;

    @Column(name = "aid", nullable = false)
    private Integer aid;

    @Column(name = "affid", nullable = false)
    private Integer affid;
}
