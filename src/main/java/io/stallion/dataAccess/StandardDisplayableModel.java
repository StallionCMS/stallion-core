/*
 * Stallion: A Modern Content Management System
 *
 * Copyright (C) 2015 - 2016 Patrick Fitzsimmons.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
 * License for more details. You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 *
 *
 *
 */

package io.stallion.dataAccess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.stallion.Context;
import io.stallion.dataAccess.db.Converter;
import io.stallion.dataAccess.db.converters.JsonListConverter;
import io.stallion.settings.SecondaryDomain;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;

import javax.persistence.Column;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.stallion.Context.settings;
import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.list;

/**
 * A base class for model objects that can be rendered as web pages.
 */
public class StandardDisplayableModel extends ModelBase implements Displayable {
    // Unfortunately these fields all have to be null, so when the object is passed in from
    // a JSON web request, we can identify which fields are set and which are not
    private String title;
    private String slug;
    private String content;
    private String originalContent;
    private String template;
    private ZonedDateTime publishDate;
    private Boolean draft = false;
    private String author = "";
    private String metaDescription = "";
    private String overrideDomain = null;
    private String relCanonical = "";
    private String metaKeywords = "";
    private String titleTag = "";
    private String image = "";
    private String ogType = "article";
    private String previewKey = "";
    private List<String> oldUrls = list();
    private String contentType = "";

    /**
     * Used in the page &lt;title&gt; tag.
     *
     * @return
     */
    @Column
    public String getTitle() {
        return title;
    }

    public StandardDisplayableModel setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * The user settable path component of the URL at which this page can be accessed, before
     * applying the override/secondary domain (if applicable)
     *
     * @return
     */
    @Override
    @Column
    @UniqueKey
    public String getSlug() {
        return this.slug;
    }

    /**
     * The path component of the URL at which this page can be accessed. This is the slug by default,
     * but if on an override domain, the slug minus the root
     */
    @Override
    public String getRealHttpPath() {
        if (empty(getOverrideDomain())) {
            return getSlug();
        } else {
            SecondaryDomain sd = settings().getSecondaryDomainByDomain(getOverrideDomain());
            if (sd.isStripRootFromPageSlug()) {
                return getSlug().substring(sd.getRewriteRoot().length());
            } else {
                return getSlug();
            }
        }
    }

    /**
     * Empty by default, but if secondary domains are found, and the page slug matches that of a secondary domain,
     * then this method will return the secondary domain.
     *
     * @return
     */
    @Override
    @JsonIgnore
    public String getOverrideDomain() {
        if (overrideDomain != null) {
            return overrideDomain;
        }
        if (Settings.instance() == null) {
            overrideDomain = "";
            return overrideDomain;
        }
        if (Settings.instance().getSecondaryDomains().size() == 0) {
            overrideDomain = "";
            return overrideDomain;
        }
        if (getSlug() == null) {
            overrideDomain = "";
            return overrideDomain;
        }
        for (SecondaryDomain d : Settings.instance().getSecondaryDomains()) {
            if (getSlug().startsWith(d.getRewriteRoot())) {
                overrideDomain = d.getDomain();
                return overrideDomain;
            }
        }
        overrideDomain = "";
        return overrideDomain;
    }

    /**
     * Creates a CSS id for the page &lt;body&gt; tag based on the slug. This is used
     * to make it easy to do per-page stylings in a stylesheet.
     *
     * @return
     */
    public String getSlugForCssId() {
        String s = slug;
        if (slug == null) {
            return "";
        }
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (empty(s)) {
            s = "site-root";
        }
        s = GeneralUtils.slugify(s);
        return s;
    }

    /**
     * The actual page content that gets displayed in the body of the web page. This should
     * already be HTML (for HTML pages).
     *
     * @return
     */
    @Override
    @Column(columnDefinition = "longtext")
    public String getContent() {
        return this.content;
    }

    public <D extends StandardDisplayableModel> D setSlug(String slug) {
        this.slug = slug;
        return (D) this;
    }

    public <D extends StandardDisplayableModel> D setContent(String content) {
        this.content = content;
        return (D) this;
    }

    /**
     * The path of the template used to render the page. Will default to page.jinja if it is not set.
     *
     * @return
     */
    @Override
    @Column
    public String getTemplate() {
        return template;
    }

    public <D extends StandardDisplayableModel> D setTemplate(String template) {
        this.template = template;
        return (D) this;
    }


    /**
     * Get the date at which the page should be live, if this is in the future, the page will
     * not be visible.
     *
     * @return
     */
    @Column
    public ZonedDateTime getPublishDate() {
        return publishDate;
    }

    public <D extends StandardDisplayableModel> D setPublishDate(ZonedDateTime publishDate) {
        this.publishDate = publishDate;
        return (D) this;
    }

    /**
     * If true, the page will not be visible.
     *
     * @return
     */
    @Column
    public Boolean getDraft() {
        return draft;
    }

    public <D extends StandardDisplayableModel> D setDraft(Boolean draft) {
        this.draft = draft;
        return (D) this;
    }

    /**
     * True if draft is false, and publishDate is before the current time.
     *
     * @return
     */
    public Boolean getPublished() {
        if (draft == false && (getPublishDate() == null || getPublishDate().isBefore(DateUtils.utcNow()))) {
            return true;
        } else {
            return false;
        }
    }

    public ZonedDateTime getDate() {
        return this.getPublishDate();
    }

    /**
     * Get the publish date and format it with the passed in DateTimeFormatter
     *
     * @param format
     * @return
     */
    public String formattedDate(String format) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(format);
        return getPublishDate().format(formatter);
    }

    /**
     * Get the full URL to this page
     *
     * @return
     */
    public String getPermalink() {
        if (empty(getOverrideDomain())) {
            return Context.getSettings().getSiteUrl() + getRealHttpPath();
        } else {
            String scheme = settings().getSchemeForSecondaryDomain(getOverrideDomain());

            return scheme + "://" + getOverrideDomain() + getRealHttpPath();
        }

    }

    /**
     * Get the author name of this page.
     *
     * @return
     */
    @Column
    public String getAuthor() {
        return author;
    }

    public <D extends StandardDisplayableModel> D setAuthor(String author) {
        this.author = author;
        return (D) this;
    }

    /**
     * For the description meta tag, often used by Google and others to give a one sentence
     * explanation of the page in search results.
     *
     * @return
     */
    @Column
    public String getMetaDescription() {
        return metaDescription;
    }

    public <D extends StandardDisplayableModel> D setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
        return (D) this;
    }

    /**
     * Used for the canonical tag in the HTML head section, set this manually if this page
     * is accessible at multiple URL's and you do not want to get duplicate content penalties
     * in Google.
     *
     * @return
     */
    @Override
    @Column
    public String getRelCanonical() {
        return relCanonical;
    }

    public <D extends StandardDisplayableModel> D setRelCanonical(String relCanonical) {
        this.relCanonical = relCanonical;
        return (D) this;
    }

    /**
     * Get a list of comma separated keywords for the keywords tag.
     *
     * @return
     */
    @Override
    @Column
    public String getMetaKeywords() {
        return metaKeywords;
    }

    public <D extends StandardDisplayableModel> D setMetaKeywords(String metaKeywords) {
        this.metaKeywords = metaKeywords;
        return (D) this;
    }


    @Override
    @Column
    public String getTitleTag() {
        return titleTag;
    }

    public <D extends StandardDisplayableModel> D setTitleTag(String titleTag) {
        this.titleTag = titleTag;
        return (D) this;
    }

    /**
     * Get used for the og:image meta tag.
     *
     * @return
     */
    @Override
    @Column
    public String getImage() {
        return image;
    }

    public <D extends StandardDisplayableModel> D setImage(String image) {
        this.image = image;
        return (D) this;
    }

    /**
     * OpenGraph page type, for the HTML meta section
     *
     * @return
     */
    @Override
    @Column(length = 30)
    public String getOgType() {
        return ogType;
    }

    public <D extends StandardDisplayableModel> D setOgType(String ogType) {
        this.ogType = ogType;
        return (D) this;
    }

    /**
     * Set this to some custom value, and then you can view an unpublished page via
     * /slug?stPreview=(your preview key)
     *
     * @return
     */
    @Override
    @Column(length = 40)
    public String getPreviewKey() {
        return previewKey;
    }

    public <D extends StandardDisplayableModel> D setPreviewKey(String previewKey) {
        this.previewKey = previewKey;
        return (D) this;
    }

    /**
     * If you change the URL of a page, add the old url here, Stallion will then 301 redirect
     * the old url to the new url.
     *
     * @return
     */
    @Override
    @Column(columnDefinition = "longtext")
    @Converter(cls = JsonListConverter.class)
    public List<String> getOldUrls() {
        return oldUrls;
    }

    @Override
    public <D extends Displayable> D setOldUrls(List<String> oldUrls) {
        this.oldUrls = oldUrls;
        return (D) this;
    }

    @Override
    @Column(columnDefinition = "longtext")
    public String getOriginalContent() {
        return originalContent;
    }

    public <D extends StandardDisplayableModel> D setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
        return (D) this;
    }

    public String getSummary() {
        int i = getContent().indexOf("<!--more-->");
        if (i > -1) {
            return getContent().substring(0, i);
        }
        i = getContent().indexOf("</p>");
        if (i > -1) {
            return getContent().substring(0, i + 4);
        }
        i = getContent().indexOf("</div>");
        if (i > -1) {
            return getContent().substring(0, i + 6);
        }
        return getContent();
    }

    public String getTruncatedSummary(int max) {
        String summary = getSummary();
        if (summary.length() < max) {
            return summary;
        } else {
            max = summary.lastIndexOf(" ", max);
            return summary.substring(0, max) + "&hellip;";
        }
    }


    public String formatPublishDate() {
        return formatPublishDate("MMM d, YYYY");
    }

    public String formatPublishDate(String pattern) {
        ZonedDateTime dt = getPublishDate();
        if (dt == null) {
            dt = DateUtils.utcNow();
        }
        // TODO: Localize
        return this.getPublishDate().format(DateTimeFormatter.ofPattern(pattern));
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public StandardDisplayableModel setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
