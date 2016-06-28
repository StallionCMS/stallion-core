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

package io.stallion.restfulEndpoints;

import io.stallion.users.Role;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.empty;

/**
 * Represents a RESTful endpoint, for handling incoming HTTP requests based
 * on the path and HTTP Method.
 *
 */
public class RestEndpointBase {
    private String route = "";
    private List<RequestArg> args = new ArrayList<>();
    private Role role = null;
    private Boolean checkXSRF = null;
    private String method = "";
    private String _produces = "";
    private String _consumes = "";
    private Class jsonViewClass;
    private String scope;

    public RestEndpointBase() {

    }

    public String getRoute() {
        return route;
    }

    public RestEndpointBase setRoute(String route) {
        this.route = route;
        return this;
    }

    public List<RequestArg> getArgs() {
        return args;
    }

    public RestEndpointBase setArgs(List<RequestArg> args) {
        this.args = args;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public RestEndpointBase setRole(String role) {
        this.role = Enum.valueOf(Role.class, role);
        return this;
    }

    public RestEndpointBase setRole(Role role) {
        this.role = role;
        return this;
    }

    /**
     * If checkXSRF, is not null, return that.
     * Otherwise:
     * - don't check HTML endpoints
     * - do check non-GET endpoints
     * - do check if requires Role of member or higher
     * @return
     */
    public boolean shouldCheckXSRF() {
        if (getCheckXSRF() != null) {
            return getCheckXSRF();
        }
        if (!"GET".equals(getMethod().toUpperCase())) {
            return true;
        }
        if (getProduces().equals("text/html")) {
            return false;
        }
        if (getRole().getValue() >= Role.MEMBER.getValue()) {
            return true;
        }
        return false;
    }

    public Boolean getCheckXSRF() {
        return checkXSRF;
    }

    public RestEndpointBase setCheckXSRF(Boolean checkXSRF) {
        this.checkXSRF = checkXSRF;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public RestEndpointBase setMethod(String method) {
        this.method = method;
        return this;
    }


    public String getProduces() {
        return _produces;
    }

    public RestEndpointBase setProduces(String _produces) {
        this._produces = _produces;
        return this;
    }

    public String getConsumes() {
        return _consumes;
    }

    public RestEndpointBase setConsumes(String _consumes) {
        this._consumes = _consumes;
        return this;
    }

    public RestEndpointBase produces(String _produces) {
        setProduces(_produces);
        return this;
    }

    public RestEndpointBase consumes(String _consumes) {
        setConsumes(_consumes);
        return this;
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getRoute()).
                append(getMethod()).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RestEndpointBase))
            return false;
        if (obj == this)
            return true;

        RestEndpointBase otherEndpoint = (RestEndpointBase) obj;
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                append(getRoute(), otherEndpoint.getRoute()).
                append(getMethod(), otherEndpoint.getMethod()).
                isEquals();
    }

    public Class getJsonViewClass() {
        return jsonViewClass;
    }

    public void setJsonViewClass(Class jsonViewClass) {
        this.jsonViewClass = jsonViewClass;
    }

    public String getScope() {
        return scope;
    }

    public RestEndpointBase setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public boolean isScoped() {
        return !empty(scope);
    }
}



