package com.cnebula.nature.dto;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "nature.dbo.tb_article")
public class ArticleXML {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(generator = "id", strategy = GenerationType.IDENTITY)
    @GenericGenerator(name = "id", strategy = "increment")
    @Column(name = "id", nullable = false)
    private Integer id; //Primary key

    @Column(name = "artid", nullable = false, columnDefinition="INT")
    private Integer artid; // json type

    @Column(name = "journal_id", nullable = true, columnDefinition="TEXT")
    private String journalId; // json type

    @Column(name = "issn", nullable = true, columnDefinition="TEXT")
    private String issn; // json type

    @Column(name = "publisher", nullable = true, columnDefinition="TEXT")
    private String publisher;

    @Column(name = "journal_title_group", nullable = true, columnDefinition="TEXT")
    private String journalTitleGroup;

    @Column(name = "article_type", nullable = true, columnDefinition="TEXT")
    private String articleType;

    @Column(name = "volume", nullable = true, columnDefinition="TEXT")
    private String volume;

    @Column(name = "issue", nullable = true, columnDefinition="TEXT")
    private String issue;

    @Column(name = "article_id", nullable = true, columnDefinition="TEXT")
    private String articleId;

    @Column(name = "title_group", nullable = true, columnDefinition="TEXT")
    private String titleGroup;

    @Column(name = "contrib", nullable = true, columnDefinition="TEXT")
    private String contrib;

    @Column(name = "aff", nullable = true, columnDefinition="TEXT")
    private String aff;

    @Column(name = "abst", nullable = true, columnDefinition="TEXT")
    private String abst;

    @Column(name = "article_categories", nullable = true, columnDefinition="TEXT")
    private String articleCategories;

    @Column(name = "custom_mate_group", nullable = true, columnDefinition="TEXT")
    private String customMateGroup;

    @Column(name = "permissions", nullable = true, columnDefinition="TEXT")
    private String permissions;

    @Column(name = "page", nullable = true, columnDefinition="TEXT")
    private String page;

    @Column(name = "pub_date", nullable = true, columnDefinition="TEXT")
    private String pubDate;

}
