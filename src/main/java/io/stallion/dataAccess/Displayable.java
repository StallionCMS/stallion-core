/*
 * Stallion Core: A Modern Web Framework
 *
 * Copyright (C) 2015 - 2016 Stallion Software LLC.
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

import java.time.ZonedDateTime;
import java.util.List;


public interface Displayable extends Model {
    public String getSlug();
    public String getRealHttpPath();
    public String getSlugForCssId();
    public String getOverrideDomain();
    public String getContent();
    public String getOriginalContent();
    public String getTemplate();
    public ZonedDateTime getPublishDate();
    public Boolean getDraft();
    public Boolean getPublished();
    public String getPermalink();
    public String getRelCanonical();
    public String getMetaKeywords();
    public String getContentType();
    public String getMetaDescription();
    public String getTitleTag();
    public String getTitle();
    public String getOgType();
    public String getImage();
    public String getPreviewKey();
    public List<String> getOldUrls();
    public <T extends Displayable> T setOldUrls(List<String> oldUrls);

}
