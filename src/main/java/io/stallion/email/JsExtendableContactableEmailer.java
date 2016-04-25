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

import java.util.Map;

/**
 * A version of the ContactableEmailer meant to be extended from Javascript.
 * Every method that one would normally override now has a version that passes in the object.
 * This is to prevent the problem whereby classes extended by nashorn do not get access to "this"
 * So we have to manually pass in the instance as the first argument. It's like python all over again!
 *
 * @param <T>
 */
public abstract class JsExtendableContactableEmailer<T extends Contactable> extends ContactableEmailer<T> {

    public JsExtendableContactableEmailer(T user) {
        super(user);
    }

    public JsExtendableContactableEmailer(T user, Map<String, Object> context) {
        super(user, context);
    }

    public String getTemplate() {
        return getTemplate(this.getContext());
    }
    public abstract String getTemplate(Object self);

    public String getSubject() {
        return getSubjectJs(this.getContext());
    }
    public abstract String getSubjectJs(Object self);

    public String getFromAddress() {
        return getFromAddressJs(this);
    }
    public String getFromAddressJs(JsExtendableContactableEmailer<T> self) {
        return super.getFromAddress();
    }

    public String getReplyTo() {
        return getReplyToJs(this);
    }
    public String getReplyToJs(JsExtendableContactableEmailer<T> self) {
        return super.getReplyTo();
    }


    public String getCc() {
        return getCcJs(this);
    }
    public String getCcJs(JsExtendableContactableEmailer<T> self) {
        return super.getCc();
    }

    public String getUniqueKey() {
        return getUniqueKey(this);
    }
    public String getUniqueKey(JsExtendableContactableEmailer<T> self) {
        return super.getUniqueKey();
    }


    protected void onPrepareContext() {
        onPrepareContext(this);
    }

    protected void onPrepareContext(JsExtendableContactableEmailer<T> self) {
        super.onPrepareContext();
    }


    public String getEmailType() {
        return getEmailType(this);
    }

    public String getEmailType(JsExtendableContactableEmailer<T> self) {
        return super.getEmailType();
    }



    public boolean checkOptOut() {
        return checkOptOut(this);
    }

    public boolean checkOptOut(JsExtendableContactableEmailer<T> self) {
        return super.checkOptOut();
    }

}
