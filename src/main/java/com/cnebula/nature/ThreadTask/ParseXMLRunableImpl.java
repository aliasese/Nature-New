package com.cnebula.nature.ThreadTask;

import com.cnebula.nature.configuration.DefaultConfiguration;
import com.cnebula.nature.configuration.HibernateConfiguration;
import com.cnebula.nature.dto.Affiliation;
import com.cnebula.nature.dto.Article;
import com.cnebula.nature.dto.AuthAff;
import com.cnebula.nature.dto.Author;
import com.cnebula.nature.entity.Institution;
import com.cnebula.nature.util.ExtractZipUtil;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.*;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ParseXMLRunableImpl implements Runnable {

    private ZipFile zipFile;
    private List<String> fileNameIssue;
    private static Properties properties;

    public ParseXMLRunableImpl() {
    }

    public ParseXMLRunableImpl(ZipFile zipFile, List<String> fileNameIssue, Properties properties) {
        this.zipFile = zipFile;
        this.fileNameIssue = fileNameIssue;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            JSONObject articleSetJson = null;
            List<Element> es = null;
            Iterator<String> issueIt = fileNameIssue.iterator();
            String artXMLName = null;
            String artPDFName = null;
            InputStream isPdf = null;
            int byteCount = 0;
            while (issueIt.hasNext()) {
                String issueFileName = issueIt.next();
                if (issueFileName.endsWith("xml.Meta")) {
                    artXMLName = issueFileName;
                    InputStream ips = null;
                    try {
                        ips = zipFile.getInputStream(zipFile.getEntry(issueFileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    SAXReader sr = new SAXReader();
                    Document doc = null;
                    try {
                        doc = sr.read(ips);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                    Element rootElement = doc.getRootElement();
                    es = rootElement.elements();

                    InputStream ipss = null;
                    try {
                        ipss = zipFile.getInputStream(zipFile.getEntry(issueFileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    articleSetJson = XML.toJSONObject(new InputStreamReader(ipss));
                    //fileNameIssue.remove(issueFileName);
                    //break;
                } else if (issueFileName.endsWith("pdf")) {
                    artPDFName = issueFileName;
                    isPdf = zipFile.getInputStream(zipFile.getEntry(artPDFName)); // InputStream of PDF
                    byteCount = isPdf.available();
                }
            }
            if (articleSetJson == null || isPdf == null) return;
            //if (articleSetJson == null) throw new Exception("Fail to get XML");

            JSONObject front = articleSetJson.getJSONObject("article").getJSONObject("front");
            JSONObject articleMeta = front.getJSONObject("article-meta");

            Article at = new Article();
            String pips = artPDFName.substring(artPDFName.lastIndexOf("/") + 1, artPDFName.lastIndexOf("."));
            at.setPips(pips);
            JSONArray atIds = articleMeta.getJSONArray("article-id");
            for (int j = 0; j < atIds.length(); j++) {
                String idType = atIds.getJSONObject(j).getString("pub-id-type");
                if (idType.contentEquals("doi")) {
                    at.setDoi(atIds.getJSONObject(j).getString("content"));
                } else
                if (idType.contentEquals("pii")) {
                    at.setPii(atIds.getJSONObject(j).getString("content"));
                }
            }
            if (at.getPii() == null) at.setPii(at.getDoi());


            JSONObject journalMeta = front.getJSONObject("journal-meta");
            JSONObject publisher = journalMeta.getJSONObject("publisher");
            at.setPublisherName(publisher.getString("publisher-name"));

            String journalTitle = journalMeta.getJSONObject("journal-title-group").getString("journal-title");
            at.setJournalTitle(journalTitle);

            JSONArray issns = new JSONArray();
            if (journalMeta.get("issn") instanceof JSONObject) {
                issns.put(journalMeta.getJSONObject("issn"));
            } else if (journalMeta.get("issn") instanceof JSONArray) {
                issns = journalMeta.getJSONArray("issn");
            }

            for (int i = 0; i < issns.length(); i++) {
                String pubType = issns.getJSONObject(i).getString("pub-type");
                if (pubType.contentEquals("epub")) {
                    at.setIssn9(issns.getJSONObject(i).getString("content"));
                    at.setIssn8(issns.getJSONObject(i).getString("content").replaceAll("-", ""));
                    break;
                }
            }

            at.setVolume(String.valueOf(articleMeta.get("volume")));
            at.setIssue(String.valueOf(articleMeta.getJSONObject("issue").get("content")));

            JSONArray pubDate = articleMeta.getJSONArray("pub-date");

            for (int i = 0; i < pubDate.length(); i++) {
                String dateType = pubDate.getJSONObject(i).getString("date-type");
                if (dateType.contentEquals("epub")) {
                    String day = String.valueOf(pubDate.getJSONObject(i).get("day"));
                    String month = String.valueOf(pubDate.getJSONObject(i).get("month"));
                    String year = String.valueOf(pubDate.getJSONObject(i).get("year"));
                    at.setPubDate(year, month, day);
                    break;
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

            String language = articleMeta.getJSONObject("title-group").getJSONObject("article-title").getString("xml:lang");
            String articleTitle = articleMeta.getJSONObject("title-group").getJSONObject("article-title").getString("content");
            at.setLanguage(language);

            //nteger fpage = Integer.valueOf(String.valueOf(articleMeta.get("fpage")));
            //Integer lpage = Integer.valueOf(String.valueOf(articleMeta.get("lpage")));
            at.setPpct(articleMeta.getInt("lpage") - articleMeta.getInt("fpage") + 1);
            at.setPpf(String.valueOf(articleMeta.getInt("fpage")));
            at.setPpl(String.valueOf(articleMeta.getInt("lpage")));

            at.setAtl(articleTitle);

            JSONArray abstracts = new JSONArray();
            if (articleMeta.get("abstract") instanceof JSONObject) {
                abstracts.put(articleMeta.getJSONObject("abstract"));
            } else if (articleMeta.get("abstract") instanceof JSONArray) {
                abstracts = articleMeta.getJSONArray("abstract");
            }

            for (int i = 0; i < abstracts.length(); i++) {
                String title = abstracts.getJSONObject(i).getString("title");
                if (title.contentEquals("Abstract")) {
                    String abst = abstracts.getJSONObject(i).getJSONObject("p").getString("content");
                    at.setAbst(abst);
                    break;
                }
            }

            at.setFileSize(String.valueOf(byteCount));
            at.setAbsFlag(StringUtils.isEmpty(at.getAbst()) ? 0 : 1);
            at.setOriContent(es.get(0).asXML());

            JSONObject contribGroup = articleMeta.getJSONObject("contrib-group");
            JSONArray contribs = contribGroup.getJSONArray("contrib");

            JSONArray authors = new JSONArray();
            JSONArray affiliations = new JSONArray();

            for (int i = 0; i < contribs.length(); i++) {
                JSONObject contrib = contribs.getJSONObject(i);
                JSONObject author = contrib.getJSONObject("name");
                Author auth = new Author();
                authors.put(auth);
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

                JSONArray affRids = new JSONArray();
                JSONArray corRids = new JSONArray();

                JSONArray xrefs = new JSONArray();
                if (contrib.get("xref") instanceof JSONObject) {
                    xrefs.put(contrib.getJSONObject("xref"));
                } else if (contrib.get("xref") instanceof JSONArray) {
                    xrefs = contrib.getJSONArray("xref");
                }

                for (int j = 0; j < xrefs.length(); j++) {
                    JSONObject xref = xrefs.getJSONObject(j);
                    if (xref.getString("ref-type").contentEquals("aff")) {
                        String affRid = xref.getString("rid");
                        affRids.put(affRid);
                    } else if (xref.getString("ref-type").contentEquals("corresp")) {
                        String corRid = xref.getString("rid");
                        corRids.put(corRid);
                    }
                }

                JSONObject authorNotes = articleMeta.getJSONObject("author-notes");
                JSONArray corresps = new JSONArray();
                if (authorNotes.get("corresp") instanceof JSONObject) {
                    corresps.put(authorNotes.getJSONObject("corresp"));
                } else if (authorNotes.get("corresp") instanceof JSONArray) {
                    corresps = authorNotes.getJSONArray("corresp");
                }

                JSONArray affs = new JSONArray();
                if (contribGroup.get("aff") instanceof JSONObject) {
                    affs.put(contribGroup.getJSONObject("aff"));
                } else if (contribGroup.get("aff") instanceof JSONArray) {
                    affs = contribGroup.getJSONArray("aff");
                }

                for (int j = 0; j < affs.length(); j++) {
                    Affiliation affiliation = new Affiliation();
                    affiliations.put(affiliation);
                    affiliation.setAffsort(j + 1);
                    Institution institution1 = new Institution();
                    JSONObject aff = affs.getJSONObject(j);
                    String id = aff.getString("id");
                    for (int k = 0; k < affRids.length(); k++) {
                        String affRid = affRids.getString(k);
                        if (id.contentEquals(affRid)) {
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
                            break;
                        }
                    }

                    for (int k = 0; k < corRids.length(); k++) {
                        String corRid = corRids.getString(k);
                        if (!id.substring(id.length() - 1).contentEquals(corRid.substring(corRid.length() - 1))) continue;
                        for (int l = 0; l < corresps.length(); l++) {
                            JSONObject corresp = corresps.getJSONObject(l);
                            if (corresp.getString("id").contentEquals(corRid)) {
                                institution1.setEmail(corresp.getString("email"));
                                break;
                            }
                        }
                    }
                    affiliation.setAff(institution1.toString());
                }

                /*for (int k = 0; k < corRids.length(); k++) {
                    String corRid = corRids.getString(k);
                    for (int l = 0; l < corresps.length(); l++) {
                        JSONObject corresp = corresps.getJSONObject(l);
                        if (corresp.getString("id").contentEquals(corRid)) {
                            institution1.setEmail(corresp.getString("email"));
                        }
                    }
                }
                affiliation.setAff(institution1.toString());*/
            }

                // Remove Duplicate
                synchronized (ParseXMLRunableImpl.class) {
                    Session session = HibernateConfiguration.sessionFactory.openSession();
                    /*CriteriaBuilder cb = session.getCriteriaBuilder();
                    CriteriaQuery<Article> query = cb.createQuery(Article.class);

                    query.where((Predicate) Restrictions.eq("artid", 1));*/
                    //query.where();
                    DetachedCriteria dc = DetachedCriteria.forClass(Article.class);
                    //dc.add(Restrictions.eq("jtl", at.getJournalTitle()));
                    dc.add(Restrictions.eq("jabt", at.getJabt()));
                    dc.add(Restrictions.eq("issn8", at.getIssn8()));
                    dc.add(Restrictions.eq("volume", at.getVolume()));
                    dc.add(Restrictions.eq("issue", at.getIssue()));
                    dc.add(Restrictions.eq("atl", at.getAtl()));
                    dc.add(Restrictions.eq("ppf", at.getPpf()));
                    dc.add(Restrictions.eq("ppl", at.getPpl()));

                    Criteria cr = dc.getExecutableCriteria(session);
                    List<Article> list = cr.list();
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@" + list.size() + dc.toString());
                    for (int j = 0; j < list.size(); j++) {
                        //Transaction transaction = session.beginTransaction();
                        Article att = list.get(j);

                        @SuppressWarnings("unchecked")
                        String hqlSelAff = "FROM Affiliation WHERE artid = :artid";
                        List<Affiliation> affs = session.createQuery(hqlSelAff)
                                .setParameter("artid", att.getArtid())
                                .getResultList();

                        @SuppressWarnings("unchecked")
                        String hqlSelAu = "FROM Author WHERE artid = :artid";
                        List<Author> auths = session.createQuery(hqlSelAu)
                                .setParameter("artid", att.getArtid())
                                .getResultList();

                        synchronized (ParseXMLRunableImpl.class) {
                            Transaction transaction = session.beginTransaction();
                            try {
                                for (int k = 0; k < auths.size(); k++) {
                                    Author author = auths.get(k);
                                    //Session session1 = this.sessionFactory.openSession();
                                    //Transaction transaction = session1.beginTransaction();
                                    for (int l = 0; l < affs.size(); l++) {
                                        Affiliation aff = affs.get(l);
                                        synchronized (ParseXMLRunableImpl.class) {
                                            Session session2 = HibernateConfiguration.sessionFactory.getCurrentSession();
                                            Transaction transaction2 = session2.beginTransaction();
                                            String hqlDeleteBatch = "DELETE FROM AuthAff WHERE aid = :aid AND affid = :affid";
                                            try {
                                                session2.createQuery(hqlDeleteBatch)
                                                        .setParameter("aid", author.getAid())
                                                        .setParameter("affid", aff.getAffid())
                                                        .executeUpdate();
                                                transaction2.commit();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                transaction2.rollback();
                                            } finally {
                                                session2.close();
                                            }
                                        }
                                        /*String hqlSelAuthAff = "FROM AuthAff WHERE aid = :aid AND affid = :affid";
                                        List<AuthAff> authAffs = session2.createQuery(hqlSelAuthAff)
                                                .setParameter("aid", author.getAid())
                                                .setParameter("affid", aff.getAffid())
                                                .getResultList();
                                        transaction2.commit();
                                        //session2.close();
                                        for (int m = 0; m < authAffs.size(); m++) {
                                            AuthAff authAff = authAffs.get(m);
                                            session.delete(authAff);
                                        }*/
                                        session.delete(aff);
                                    }
                                    session.delete(author);
                                    //transaction.commit();
                                }
                                session.delete(att);

                                // Delete PDF of specific article
                            /*String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                            String pdfDirChild = att.getJabt() + File.separator + att.getVolume() + File.separator + att.getIssue() + File.separator + att.getPips() + ".pdf";

                            File file = new File(pdfBaseDir, pdfDirChild);
                            if (file.exists()) {
                                file.delete();
                            }*/
                                transaction.commit();
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                                transaction.rollback();
                            } finally {
                            }
                        }
                    }
                    session.close();



                    Session session1 = HibernateConfiguration.sessionFactory.openSession();
                    Transaction transaction = session1.beginTransaction();
                    try {
                        session1.save(at);

                        // Store PDF to specific directory of file system
                    /*String pdfBaseDir = properties.getProperty(DefaultConfiguration.NAME_PDFBASEDIR);
                    String pdfDirChild = at.getJabt() + File.separator + at.getVolume() + File.separator + at.getIssue();
                    File file = new File(pdfBaseDir, pdfDirChild);
                    if (!file.exists()) {
                        file.mkdirs();
                    }

                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(pdfBaseDir + File.separator + pdfDirChild + File.separator + at.getPips().concat(".pdf"), false));
                        byte[] bt = new byte[1024];
                        int length = 0;
                        while ((length = isPdf.read(bt)) != -1) {
                            bos.write(bt);
                            bos.flush();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bos.close();
                            isPdf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/

                        for (int j = 0; j < authors.length(); j++) {
                            Author author = (Author) authors.get(j);
                            author.setArtid(at.getArtid());
                            session1.save(author);
                            for (int k = 0; k < affiliations.length(); k++) {
                                Affiliation affiliation = (Affiliation) affiliations.get(k);
                                affiliation.setArtid(at.getArtid());
                                session1.save(affiliation);
                                AuthAff authAff = new AuthAff();
                                authAff.setAid(author.getAid());
                                authAff.setAffid(affiliation.getAffid());
                                session1.save(authAff);
                            }
                        }
                        transaction.commit();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        transaction.rollback();
                    } finally {
                        session1.close();
                    }
                }
        //    }
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
    }
}
