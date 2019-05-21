package com.cnebula.nature.dto;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "nature.dbo.content")
public class Article {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(generator = "artid")
    @GenericGenerator(name = "artid", strategy = "increment")
    @Column(name = "artid", nullable = false)
    private Integer artid; //Primary key


    @Column(name = "pips", nullable = true)
    private String pips; //全文文件名称,即pdf扩展后缀名.之前的名称

    @Column(name = "pii", nullable = true)
    private String pii; // 电子资源唯一标示

    @Column(name = "doi", nullable = true)
    private String doi; // 电子资源唯一标示

    @Column(name = "pnm", nullable = true)
    private String publisherName; //出版社名称

    @Column(name = "jtl", nullable = false)
    private String journalTitle; //期刊名称

    @Column(name = "jtl_sort", nullable = false)
    private String jtlSort; //期刊排序名称

    @Column(name = "jtl_index", nullable = false)
    private String jtlIndex; //期刊排序名称

    @Column(name = "jabt", nullable = false)
    private String jabt; //期刊排序名称

    @Column(name = "issn9", nullable = true)
    private String issn9; // issn

    @Column(name = "issn8", nullable = true)
    private String issn8; // issn8 去掉中间横杠

    @Column(name = "vid", nullable = false)
    private String volume; //卷号

    @Column(name = "iid", nullable = true)
    private String issue; //期号

    @Column(name = "cd", nullable = true)
    private String pubDate = ""; //出版日期

    @Column(name = "cd_pub", nullable = true)
    private String cdPub = "";

    @Column(name = "pubstatus", nullable = true)
    private String pubStatus;

    @Column(name = "categ", nullable = true)
    private String categ; //文章所属类目

    @Column(name = "lang", nullable = true)
    private String language;

    @Column(name = "ppct", nullable = true)
    private Integer ppct; //页码长度

    @Column(name = "ppf", nullable = true)
    private String ppf; //起始页码

    @Column(name = "ppl", nullable = true)
    private String ppl; //终止页码

    @Column(name = "atl", nullable = false)
    private String atl; //文章名称

    @Column(name = "atl_sort", nullable = false)
    private String atlSort; //文章排序名称

    @Column(name = "atl_index", nullable = false)
    private String atlIndex; //文章索引名称

    @Column(name = "abst", nullable = true)
    private String abst; //文章摘要

    @Column(name = "filesize", nullable = true)
    private String fileSize; //文章PDF文件大小

    @Column(name = "paperflag", nullable = true)
    private Integer paperFlag = 0; //未知, 数据库字段全0，所以此处默认 0

    @Column(name = "absflag", nullable = true)
    private Integer absFlag; //文章是否有摘要,0:无 1:有

    @Column(name = "oricontent", nullable = true)
    private String oriContent; //文章原始XML


    public Article() {

    }

    public Integer getArtid() {
        return artid;
    }

    public void setArtid(Integer artid) {
        this.artid = artid;
    }

    public String getPips() {
        return pips;
    }

    public void setPips(String pips) {
        this.pips = pips;
    }

    public String getPii() {
        return pii;
    }

    public void setPii(String pii) {
        this.pii = pii;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public void setJtlSort(String jtlSort) {
        this.jtlSort = jtlSort;
    }

    public void setJtlIndex(String jtlIndex) {
        this.jtlIndex = jtlIndex;
    }

    public String getJabt() {
        return jabt;
    }

    public void setJabt(String jabt) {
        this.jabt = jabt;
    }

    public String getIssn9() {
        return issn9;
    }

    public void setIssn9(String issn9) {
        this.issn9 = issn9;
    }

    public String getIssn8() {
        return issn8;
    }

    public void setIssn8(String issn8) {
        this.issn8 = issn8;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public void setCdPub(String cdPub) {
        this.cdPub = cdPub;
    }

    public String getPubStatus() {
        return pubStatus;
    }

    public void setPubStatus(String pubStatus) {
        this.pubStatus = pubStatus;
    }

    public String getCateg() {
        return categ;
    }

    public void setCateg(String categ) {
        this.categ = categ;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPpct() {
        return ppct;
    }

    public void setPpct(Integer ppct) {
        this.ppct = ppct;
    }

    public String getPpf() {
        return ppf;
    }

    public void setPpf(String ppf) {
        this.ppf = ppf;
    }

    public String getPpl() {
        return ppl;
    }

    public void setPpl(String ppl) {
        this.ppl = ppl;
    }

    public void setAtlSort(String atlSort) {
        this.atlSort = atlSort;
    }

    public void setAtlIndex(String atlIndex) {
        this.atlIndex = atlIndex;
    }

    public String getAbst() {
        return abst;
    }

    public void setAbst(String abst) {
        this.abst = abst;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getPaperFlag() {
        return paperFlag;
    }

    public void setPaperFlag(Integer paperFlag) {
        this.paperFlag = paperFlag;
    }

    public Integer getAbsFlag() {
        return absFlag;
    }

    public void setAbsFlag(Integer absFlag) {
        this.absFlag = absFlag;
    }

    public String getOriContent() {
        return oriContent;
    }

    public void setOriContent(String oriContent) {
        this.oriContent = oriContent;
    }

    public String getAtl() {
        return atl;
    }

    public void setAtl(String atl) {
        this.atl = atl;
        this.atlSort = atl;
        this.atlIndex = atl;
    }

    public String getAtlSort() {
        return atlSort;
    }

    public String getAtlIndex() {
        return atlIndex;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
        this.jtlSort = journalTitle;
        this.jtlIndex = journalTitle;
    }

    public String getJtlSort() {
        return jtlSort;
    }

    public String getJtlIndex() {
        return jtlIndex;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
        this.cdPub = pubDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getCdPub() {
        return cdPub;
    }

    public void setPubDate(String year, String month, String day) {
        if (!StringUtils.isEmpty(month)) {
            if (month.length() == 1) month = "0" + month;
        }
        if (!StringUtils.isEmpty(day)) {
            if (day.length() == 1) day = "0" + day;
        }
        if (year != null) this.pubDate += year;
        if (month != null) this.pubDate += month;
        if (day != null) this.pubDate += day;
        this.cdPub = this.pubDate;
    }

   /* public static void main(String [] args) throws ParseException {
        Article article = new Article();
        article.setPubDate("2019", "12", "28");
        System.out.println(article.pubDate);

        *//*SimpleDateFormat fo = new SimpleDateFormat("yyyyMMdd");
        String format = fo.format(fo.parse("00000000"));*//*

    }*/
}
/*
class PubDate {

    private String pubStatus;
    private String Year;
}*/
