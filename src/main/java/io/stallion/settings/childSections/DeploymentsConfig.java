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

package io.stallion.settings.childSections;

import io.stallion.settings.SettingMeta;

import java.util.ArrayList;
import java.util.List;

import static io.stallion.utils.Literals.list;


public class DeploymentsConfig implements SettingsSection {
    @SettingMeta
    private String env = "prod";
    @SettingMeta
    List<String> hosts;
    @SettingMeta
    private String user = "";
    @SettingMeta
    private String rootFolder;
    @SettingMeta
    private int basePort = 12500;
    @SettingMeta(cls= ArrayList.class)
    private List<String> checkUrls = list("/");
    @SettingMeta
    private String domain;
    @SettingMeta(cls=ArrayList.class)
    private List<String> aliasDomains = list();

    @SettingMeta(cls=ArrayList.class)
    private List<String> redirectDomains = list();

    @SettingMeta
    private String sslCertChain = "";
    @SettingMeta
    private String sslPrivateKey = "";

    @SettingMeta(valBoolean = false)
    private Boolean redirectToSsl = false;


    public String getEnv() {
        return env;
    }

    public DeploymentsConfig setEnv(String env) {
        this.env = env;
        return this;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public DeploymentsConfig setHosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public int getBasePort() {
        return basePort;
    }

    public void setBasePort(int basePort) {
        this.basePort = basePort;
    }

    public List<String> getCheckUrls() {
        return checkUrls;
    }

    public void setCheckUrls(List<String> checkUrls) {
        this.checkUrls = checkUrls;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getAliasDomains() {
        return aliasDomains;
    }

    public void setAliasDomains(List<String> aliasDomains) {
        this.aliasDomains = aliasDomains;
    }

    @Deprecated
    public String getSslCrt() {
        return sslCertChain;
    }

    @Deprecated
    public void setSslCrt(String sslCrt) {
        this.sslCertChain = sslCrt;
    }

    @Deprecated
    public String getSslKey() {
        return sslPrivateKey;
    }

    @Deprecated
    public void setSslKey(String sslKey) {
        this.sslPrivateKey = sslKey;
    }

    public String getSslCertChain() {
        return sslCertChain;
    }

    public void setSslCertChain(String sslCrt) {
        this.sslCertChain = sslCrt;
    }

    public String getSslPrivateKey() {
        return sslPrivateKey;
    }

    public void setSslPrivateKey(String sslKey) {
        this.sslPrivateKey = sslKey;
    }

    public Boolean getRedirectToSsl() {
        return redirectToSsl;
    }

    public DeploymentsConfig setRedirectToSsl(Boolean redirectToSsl) {
        this.redirectToSsl = redirectToSsl;
        return this;
    }


    public List<String> getRedirectDomains() {
        return redirectDomains;
    }

    public DeploymentsConfig setRedirectDomains(List<String> redirectDomains) {
        this.redirectDomains = redirectDomains;
        return this;
    }
}
