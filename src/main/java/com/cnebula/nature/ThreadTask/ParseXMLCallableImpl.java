package com.cnebula.nature.ThreadTask;

import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.*;
import com.cnebula.nature.entity.Institution;
import com.cnebula.nature.exception.PersistentException;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.ReturningWork;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

public class ParseXMLCallableImpl implements Callable<Object> {

    private final static Logger log = LoggerFactory.getLogger(ParseXMLCallableImpl.class);

    private static ZipFile zipFile;
    private List<String> fileNameIssue;
    private static Properties properties;

    public ParseXMLCallableImpl() {
    }

    public ParseXMLCallableImpl(ZipFile zipFile, List<String> fileNameIssue, Properties properties) {
        this.zipFile = zipFile;
        this.fileNameIssue = fileNameIssue;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object call() throws Exception {
        Thread.currentThread().setName(this.fileNameIssue.get(0));
        try {
            JSONObject articleSetJson = null;
            List<Element> es = null;
            Iterator<String> issueIt = this.fileNameIssue.iterator();
            String artXMLName = null;
            String artPDFName = null;
            InputStream isPdf = null;
            Integer byteCount = null;
            while (issueIt.hasNext()) {
                String issueFileName = issueIt.next();
                if (issueFileName.endsWith("xml.Meta")) {
                    artXMLName = issueFileName;
                    InputStream ips = null;
                    try {
                        ips = this.zipFile.getInputStream(this.zipFile.getEntry(issueFileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SAXReader sr = new SAXReader();
                    Document doc = null;
                    try {
                        doc = sr.read(ips);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(ips);
                    }
                    Element rootElement = doc.getRootElement();
                    es = rootElement.elements();

                    InputStream ipss = null;
                    ipss = this.zipFile.getInputStream(this.zipFile.getEntry(issueFileName));
                    articleSetJson = XML.toJSONObject(new InputStreamReader(ipss));
                    IOUtils.closeQuietly(ipss);
                } else if (issueFileName.endsWith("pdf")) {
                    artPDFName = issueFileName;
                    isPdf = this.zipFile.getInputStream(this.zipFile.getEntry(issueFileName)); // InputStream of PDF
                    byteCount = IOUtils.toByteArray(this.zipFile.getInputStream(this.zipFile.getEntry(issueFileName))).length;
                }
            }

            if (isPdf == null) log.warn("This article DO NOT BE with PDF: " + artXMLName);
            if (articleSetJson == null) return "This article DO NOT BE with XML: " + artXMLName;
            //if (articleSetJson == null) throw new Exception("Fail to get XML");

            JSONObject front = articleSetJson.getJSONObject("article").getJSONObject("front");
            JSONObject articleMeta = front.getJSONObject("article-meta");

            ArticleXML articleXML = new ArticleXML();
            articleXML.setArticleCategories(articleMeta.getJSONObject("article-categories").toString());
            articleXML.setArticleType(articleSetJson.getJSONObject("article").getString("article-type"));
            articleXML.setCustomMateGroup(articleMeta.getJSONObject("custom-meta-group").toString());
            articleXML.setPermissions(articleMeta.getJSONObject("permissions").toString());

            Article at = new Article();
            if (artPDFName != null) {
                String pips = artPDFName.substring(artPDFName.lastIndexOf("/") + 1, artPDFName.lastIndexOf("."));
                at.setPips(pips);
            }

            JSONArray atIds = new JSONArray();
            if (articleMeta.has("article-id")) {
                if (articleMeta.get("article-id") instanceof JSONObject) {
                    atIds.put(articleMeta.getJSONObject("article-id"));
                } else if (articleMeta.get("article-id") instanceof JSONArray) {
                    atIds = articleMeta.getJSONArray("article-id");
                }

                articleXML.setArticleId(atIds.toString());

                for (int j = 0; j < atIds.length(); j++) {
                    String idType = atIds.getJSONObject(j).getString("pub-id-type");
                    if (idType.contentEquals("doi")) {
                        at.setDoi(atIds.getJSONObject(j).getString("content"));
                    } else
                    if (idType.contentEquals("pii")) {
                        at.setPii(atIds.getJSONObject(j).getString("content"));
                    }
                }
            }
            //if (at.getPii() == null) at.setPii(at.getDoi());


            JSONObject journalMeta = front.getJSONObject("journal-meta");
            JSONObject publisher = journalMeta.getJSONObject("publisher");
            at.setPublisherName(publisher.getString("publisher-name"));
            articleXML.setPublisher(publisher.toString());

            JSONArray journalIds = new JSONArray();
            if (journalMeta.has("journal-id")) {
                if (journalMeta.get("journal-id") instanceof JSONObject) {
                    journalIds.put(journalMeta.getJSONObject("journal-id"));
                } else if (journalMeta.get("journal-id") instanceof JSONArray) {
                    journalIds = journalMeta.getJSONArray("journal-id");
                }
                articleXML.setJournalId(journalIds.toString());
            }

            JSONObject journalTitleGroup = journalMeta.getJSONObject("journal-title-group");
            String journalTitle = journalTitleGroup.getString("journal-title");
            String abbrevTitle = journalTitleGroup.getJSONObject("abbrev-journal-title").getString("content");
            at.setJournalTitle(journalTitle);
            at.setJabt(abbrevTitle);
            articleXML.setJournalTitleGroup(journalTitleGroup.toString());

            JSONArray issns = new JSONArray();
            if (journalMeta.has("issn")) {
                if (journalMeta.get("issn") instanceof JSONObject) {
                    issns.put(journalMeta.getJSONObject("issn"));
                } else if (journalMeta.get("issn") instanceof JSONArray) {
                    issns = journalMeta.getJSONArray("issn");
                }

                articleXML.setIssn(issns.toString());

                for (int i = 0; i < issns.length(); i++) {
                    String pubType = issns.getJSONObject(i).getString("pub-type");
                    if (pubType.contentEquals("ppub")) {
                        at.setIssn9(issns.getJSONObject(i).getString("content"));
                        at.setIssn8(issns.getJSONObject(i).getString("content").replaceAll("-", ""));
                        break;
                    }
                }
            }

            at.setVolume(String.valueOf(articleMeta.get("volume")));
            at.setIssue(String.valueOf(articleMeta.getJSONObject("issue").get("content")));
            articleXML.setIssue(articleMeta.getJSONObject("issue").toString());
            articleXML.setVolume(String.valueOf(articleMeta.get("volume")));

            JSONArray pubDate = new JSONArray();
            if (articleMeta.has("pub-date")) {
                if (articleMeta.get("pub-date") instanceof JSONObject) {
                    pubDate.put(articleMeta.getJSONObject("pub-date"));
                } else if (articleMeta.get("pub-date") instanceof JSONArray) {
                    pubDate = articleMeta.getJSONArray("pub-date");
                }
                articleXML.setPubDate(pubDate.toString());

                for (int i = 0; i < pubDate.length(); i++) {
                    String dateType = pubDate.getJSONObject(i).getString("date-type");
                    if (dateType.contentEquals("ppub") || dateType.contentEquals("epub")) {
                        String day = pubDate.getJSONObject(i).has("day") ? String.valueOf(pubDate.getJSONObject(i).get("day")) : null;
                        String month = String.valueOf(pubDate.getJSONObject(i).get("month"));
                        String year = String.valueOf(pubDate.getJSONObject(i).get("year"));
                        at.setPubDate(year, month, day);
                        at.setPubStatus(dateType);
                        break;
                    }
                }

                if (StringUtils.isEmpty(at.getPubDate())) {
                    at.setPubDate("9999"); // Set the value of cd is 9999 if there is  suggested by Mike Zhu.
                }
            }

            // at.setPubStatus(journal.getJSONObject("PubDate").getString("PubStatus"));

            String articleType = articleSetJson.getJSONObject("article").getString("article-type");
            at.setCateg(articleType.replaceAll("-", " ").toUpperCase());
            /*JSONArray subjGroup = articleMeta.getJSONObject("article-categories").getJSONArray("subj-group");
            for (int i = 0; i < subjGroup.length(); i++) {
                JSONObject subjGroupType = subjGroup.getJSONObject(i);
                if (subjGroupType.getString("subj-group-type").contentEquals("NatureArticleTypeID")) {
                    at.setCateg(subjGroupType.getString("subject").replaceAll("-", " ").toUpperCase());
                    break;
                }
            }*/

            try {
                String language = articleMeta.getJSONObject("title-group").getJSONObject("article-title").getString("xml:lang");
                at.setLanguage(language);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (articleMeta.getJSONObject("title-group").getJSONObject("article-title").has("content")) {
                    if (articleMeta.getJSONObject("title-group").getJSONObject("article-title").get("content") instanceof JSONArray) {
                        JSONArray atls = articleMeta.getJSONObject("title-group").getJSONObject("article-title").getJSONArray("content");
                        StringBuilder atl = new StringBuilder();
                        for (int i = 0; i < atls.length(); i++) {
                            atl.append(atls.get(i).toString());
                        }
                        at.setAtl(atl.toString());
                    } else {
                        String articleTitle = String.valueOf(articleMeta.getJSONObject("title-group").getJSONObject("article-title").get("content"));
                        at.setAtl(articleTitle);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                articleXML.setTitleGroup(articleMeta.getJSONObject("title-group").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //nteger fpage = Integer.valueOf(String.valueOf(articleMeta.get("fpage")));
            //Integer lpage = Integer.valueOf(String.valueOf(articleMeta.get("lpage")));
            if (articleMeta.has("fpage") && articleMeta.has("lpage")) {
                at.setPpct(String.valueOf(articleMeta.getInt("lpage") - articleMeta.getInt("fpage") + 1));
                at.setPpf(String.valueOf(articleMeta.getInt("fpage")));
                at.setPpl(String.valueOf(articleMeta.getInt("lpage")));
                articleXML.setPage(new JSONObject().put("fpage", articleMeta.getInt("fpage")).put("lpage", articleMeta.getInt("lpage")).toString());
            } else if (articleMeta.has("elocation-id")) {
                articleXML.setPage(new JSONObject().put("elocation-id", articleMeta.get("elocation-id")).toString());
            }


            JSONArray abstracts = new JSONArray();
            if (articleMeta.has("abstract")) {
                if (articleMeta.get("abstract") instanceof JSONObject) {
                    abstracts.put(articleMeta.getJSONObject("abstract"));
                } else if (articleMeta.get("abstract") instanceof JSONArray) {
                    abstracts = articleMeta.getJSONArray("abstract");
                }

                articleXML.setAbst(abstracts.toString());

                for (int i = 0; i < abstracts.length(); i++) {
                    if (abstracts.getJSONObject(i).has("title")) {
                        String title = String.valueOf(abstracts.getJSONObject(i).get("title"));
                        if (title.contentEquals("Abstract")) {
                            if (abstracts.getJSONObject(i).has("p")) {
                                if (abstracts.getJSONObject(i).getJSONObject("p").has("content")) {
                                    if (abstracts.getJSONObject(i).getJSONObject("p").get("content") instanceof JSONArray) {
                                        JSONArray absts = abstracts.getJSONObject(i).getJSONObject("p").getJSONArray("content");
                                        StringBuilder abst = new StringBuilder();
                                        for (int j = 0; j < absts.length(); j++) {
                                            abst.append(absts.get(j));
                                        }
                                        at.setAbst(abst.toString());
                                    } else {
                                        String abst = String.valueOf(abstracts.getJSONObject(i).getJSONObject("p").get("content"));
                                        at.setAbst(abst);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(byteCount != null) at.setFileSize(String.valueOf(byteCount));
            at.setAbsFlag(StringUtils.isEmpty(at.getAbst()) ? 0 : 1);
            at.setOriContent(es.get(0).asXML());

            JSONObject contribGroup = new JSONObject();
            if (articleMeta.has("contrib-group")) {
                contribGroup = articleMeta.getJSONObject("contrib-group");
            }

            JSONArray contribs = new JSONArray();
            if (articleMeta.has("contrib-group") && contribGroup.has("contrib")) {
                if (contribGroup.get("contrib") instanceof JSONObject) {
                    contribs.put(contribGroup.getJSONObject("contrib"));
                } else if (contribGroup.get("contrib") instanceof JSONArray) {
                    contribs = contribGroup.getJSONArray("contrib");
                }

                articleXML.setContrib(contribs.toString());
            }

            JSONArray affs = new JSONArray();
            if (articleMeta.has("contrib-group") && contribGroup.has("aff")) {
                if (contribGroup.get("aff") instanceof JSONObject) {
                    affs.put(contribGroup.getJSONObject("aff"));
                } else if (contribGroup.get("aff") instanceof JSONArray) {
                    affs = contribGroup.getJSONArray("aff");
                }

                articleXML.setAff(affs.toString());
            }

            /*if (articleMeta.has("author-notes")) {
                JSONObject authorNotes = articleMeta.getJSONObject("author-notes");
                JSONArray corresps = new JSONArray();
                if (authorNotes.has("corresp")) {
                    if (authorNotes.get("corresp") instanceof JSONObject) {
                        corresps.put(authorNotes.getJSONObject("corresp"));
                    } else if (authorNotes.get("corresp") instanceof JSONArray) {
                        corresps = authorNotes.getJSONArray("corresp");
                    }
                }
            }*/

            //JSONArray authorsAffs = new JSONArray();
            ArrayList<LinkedHashMap<Author, JSONArray>> authorsAffs = new ArrayList<>();
            //JSONArray affiliations = new JSONArray();

            for (int i = 0; i < contribs.length(); i++) {
                LinkedHashMap<Author, JSONArray> authorsAffsKV = new LinkedHashMap<>();
                authorsAffs.add(authorsAffsKV);
                JSONObject contrib = contribs.getJSONObject(i);
                if (contrib.has("name")) {
                    JSONObject author = contrib.getJSONObject("name");
                    Author auth = new Author();
                    //authors.put(auth);
                    auth.setAusort(String.valueOf(i + 1));
                    auth.setAufnms(author.getString("given-names"));
                    auth.setAufnmsindex(author.getString("given-names"));
                    auth.setAusnm(author.getString("surname"));
                    auth.setAusnmindex(author.getString("surname"));
                    String[] gNames = author.getString("given-names").split(" ");
                    String abbName = "";
                    for (int j = 0; j < gNames.length; j++) {
                        if (j == 0) {
                            abbName += gNames[j].substring(0, 1) + ".";
                        } else if (j == 1) {
                            abbName += gNames[j];
                        }
                    }
                    auth.setAbbindex(abbName);

                    //JSONArray affRids = new JSONArray();
                    //JSONArray corRids = new JSONArray();

                    JSONArray xrefs = new JSONArray();
                    if (contrib.has("xref")) {
                        if (contrib.get("xref") instanceof JSONObject) {
                            xrefs.put(contrib.getJSONObject("xref"));
                        } else if (contrib.get("xref") instanceof JSONArray) {
                            xrefs = contrib.getJSONArray("xref");
                        }
                        authorsAffsKV.put(auth, xrefs);
                    }
                }
            }

            for (int i = 0; i < authorsAffs.size(); i++) {
                LinkedHashMap<Author, JSONArray> authorsAffsKV = ( LinkedHashMap<Author, JSONArray>) authorsAffs.get(i);
                /*authorsAffsKV.entrySet().forEach(jsonObjectJSONArrayEntry -> {
                    JSONObject author = jsonObjectJSONArrayEntry.getKey();
                    JSONArray value = jsonObjectJSONArrayEntry.getValue();
                });*/

                Set<Map.Entry<Author, JSONArray>> entries = authorsAffsKV.entrySet();
                for (Map.Entry<Author, JSONArray> e:entries ) {
                    JSONArray xrefs = e.getValue();
                    JSONArray affRs = new JSONArray();
                    e.setValue(affRs);
                    for (int j = 0; j < affs.length(); j++) {
                        JSONObject aff = affs.getJSONObject(j);
                        //Affiliation affiliation = new Affiliation();
                        //affRs.put(affiliation);
                        Institution institution1 = new Institution();
                        int sort = 0;
                        for (int k = 0; k < xrefs.length(); k++) {
                            JSONObject xref = xrefs.getJSONObject(k);
                            if (xref.getString("ref-type").contentEquals("aff")) {
                                String affRid = xref.getString("rid");
                                if (aff.getString("id").contentEquals(affRid)) {
                                    sort += 1;
                                    Affiliation affiliation = new Affiliation();
                                    affiliation.setAffsort(sort);
                                    affRs.put(affiliation);
                                    JSONObject institutionWrap = aff.getJSONObject("institution-wrap");
                                    JSONArray institutions = new JSONArray();
                                    if (institutionWrap.get("institution") instanceof JSONObject) {
                                        institutions.put(institutionWrap.getJSONObject("institution"));
                                    } else if (institutionWrap.get("institution") instanceof JSONArray) {
                                        institutions = institutionWrap.getJSONArray("institution");
                                    }

                                    for (int l = 0; l < institutions.length(); l++) {
                                        JSONObject institution = institutions.getJSONObject(l);
                                        if (institution.getString("content-type").contentEquals("org-name")) {
                                            institution1.setOrgName(institution.getString("content"));
                                        }
                                        if (institution.getString("content-type").contentEquals("org-division")) {
                                            institution1.setOrgDivision(institution.getString("content"));
                                        }
                                    }

                                    JSONObject country = aff.getJSONObject("country");
                                    institution1.setCountry(country.getString("content"));

                                    JSONArray addrs = new JSONArray();
                                    if (aff.get("addr-line") instanceof JSONObject) {
                                        addrs.put(aff.getJSONObject("addr-line"));
                                    } else if (aff.get("addr-line") instanceof JSONArray) {
                                        addrs = aff.getJSONArray("addr-line");
                                    }

                                    for (int l = 0; l < addrs.length(); l++) {
                                        JSONObject addr = addrs.getJSONObject(l);
                                        if (addr.getString("content-type").contentEquals("state")) {
                                            institution1.setState(addr.getString("content"));
                                        }
                                        if (addr.getString("content-type").contentEquals("city")) {
                                            institution1.setCity(addr.getString("content"));
                                        }
                                    }

                                    affiliation.setAff(institution1.toString());
                                }
                            } /*else if (xref.getString("ref-type").contentEquals("corresp")) {
                                String corRid = xref.getString("rid");
                            }*/
                        }
                    }
                }

            }

                // Remove Duplicate
                synchronized (ParseXMLCallableImpl.class) {
                    Session session = HibernateConfiguration.sessionFactory.openSession();
                    Transaction transaction1 = session.beginTransaction();

                    try {
                        Object o = session.doReturningWork(new ReturningWork<Object>() {
                            @Override
                            public Object execute(Connection connection) throws SQLException {
                                CallableStatement cs = connection.prepareCall("{Call pc_remove_duplication(?,?,?,?,?,?,?)}");
                                if (!StringUtils.isEmpty(at.getJabt())) {
                                    cs.setString(1, at.getJabt());
                                } else {
                                    cs.setNull(1, 12);
                                }
                                if (!StringUtils.isEmpty(at.getIssn8())) {
                                    cs.setString(2, at.getIssn8());
                                } else {
                                    cs.setNull(2, 1);
                                }
                                if (!StringUtils.isEmpty(at.getVolume())) {
                                    cs.setString(3, at.getVolume());
                                } else {
                                    cs.setNull(3, 1);
                                }
                                if (!StringUtils.isEmpty(at.getIssue())) {
                                    cs.setString(4, at.getIssue());
                                } else {
                                    cs.setNull(4, 1);
                                }
                                if (!StringUtils.isEmpty(at.getAtl())) {
                                    cs.setString(5, at.getAtl());
                                } else {
                                    cs.setNull(5, 12);
                                }
                                if (!StringUtils.isEmpty(at.getPpf())) {
                                    cs.setString(6, at.getPpf());
                                } else {
                                    cs.setNull(6, 1);
                                }

                                if (!StringUtils.isEmpty(at.getPpl())) {
                                    cs.setString(7, at.getPpl());
                                } else {
                                    cs.setNull(7, 1);
                                }
                                //cs.setNull(6, at.getPpf() != null ? Integer.valueOf(at.getPpf()) : 1);
                                //cs.setNull(7, at.getPpl() != null ? Integer.valueOf(at.getPpl()) : 1);
                                return cs.executeUpdate();
                                //return null;
                            }
                        });
                        System.out.println("=========================Result from Procedure=============================");
                        System.out.println(o);
                        log.info(o.toString());
                        System.out.println("=========================Result from Procedure=============================");
                        //if (!(StringUtils.isEmpty(at.getJabt()) | StringUtils.isEmpty(at.getVolume()) | StringUtils.isEmpty(at.getIssue()) | StringUtils.isEmpty(at.getPips()))) {
                        if (!StringUtils.isEmpty(at.getJabt()) && !StringUtils.isEmpty(at.getVolume()) && !StringUtils.isEmpty(at.getIssue()) && !StringUtils.isEmpty(at.getPips())) {
                            // Delete PDF of specific article
                            String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                            String pdfDirChild = at.getJabt() + File.separator + at.getVolume() + File.separator + at.getIssue() + File.separator + at.getPips() + ".pdf";

                            File file = new File(pdfBaseDir, pdfDirChild);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        transaction1.commit();
                    } catch (Exception e) {
                        transaction1.rollback();
                        e.printStackTrace();
                        throw e;
                    }/* finally {
                        session.close();
                    }*/

                    //Session session1 = HibernateConfiguration.sessionFactory.openSession();
                    Transaction transaction = session.beginTransaction();
                    try {
                        session.save(at);
                        articleXML.setArtid(at.getArtid());
                        session.save(articleXML);
                    if (!(StringUtils.isEmpty(at.getJabt()) | StringUtils.isEmpty(at.getVolume()) | StringUtils.isEmpty(at.getIssue()) | StringUtils.isEmpty(at.getPips()))) {
                        // Store PDF to specific directory of file system
                        String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                        String pdfDirChild = at.getJabt() + File.separator + at.getVolume() + File.separator + at.getIssue() + File.separator + at.getPips() + ".pdf";
                        String pdfDirChildS = at.getJabt() + File.separator + at.getVolume() + File.separator + at.getIssue();
                        File fileS = new File(pdfBaseDir, pdfDirChildS);
                        if (!fileS.exists()) {
                            fileS.mkdirs();
                        }

                        OutputStream bos = null;
                        try {
                            bos = new FileOutputStream(pdfBaseDir + File.separator + pdfDirChild, false);
                            IOUtils.copy(isPdf, bos, 1024);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        } finally {
                            IOUtils.closeQuietly(isPdf);
                            IOUtils.closeQuietly(bos);
                        }
                    }


                    for (int j = 0; j < authorsAffs.size(); j++) {
                        LinkedHashMap<Author, JSONArray> authorsAffsKV = (LinkedHashMap<Author, JSONArray>) authorsAffs.get(j);
                        Set<Map.Entry<Author, JSONArray>> entries = authorsAffsKV.entrySet();
                        for (Map.Entry<Author, JSONArray> e:entries) {
                            Author author = e.getKey();
                            author.setArtid(at.getArtid());
                            session.save(author);

                            JSONArray affRs = e.getValue();
                            for (int k = 0; k < affRs.length(); k++) {
                                Affiliation affiliation = (Affiliation) affRs.get(k);
                                affiliation.setArtid(at.getArtid());
                                session.save(affiliation);
                                AuthAff authAff = new AuthAff();
                                authAff.setAid(author.getAid());
                                authAff.setAffid(affiliation.getAffid());
                                session.save(authAff);
                            }

                        }

                    }
                    transaction.commit();
                    } catch (RuntimeException e) {
                        transaction.rollback();
                        e.printStackTrace();
                        e.addSuppressed(PersistentException.class.newInstance());
                        throw e;
                    } finally {
                        session.close();
                    }
                }
        //    }
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
            //return null;
        }
        return "SUCCESS";
    }
}
