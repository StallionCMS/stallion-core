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

package io.stallion.email;

/**
 * Represents a person who can be emailed. Implemented by the User class, and the Contact class.
 *
 */
public interface Contactable {
    public Long getId();
    public String getEmail();
    public String getGivenName();
    public String getFamilyName();
    public String getDisplayName();
    public String getHonorific();

    /**
     * Opted out of all standard, non-transactional mailings.
     * @return
     */
    public boolean isOptedOut();

    /**
     * Opted out of all emails, even transactional emails, even password reset emails and such.
     * @return
     */
    public boolean isTotallyOptedOut();
    public Boolean getDeleted();

    /**
     * Is the user disabled, will by default exclude from mailings
     *
     * @return
     */
    public boolean isDisabled();
}
