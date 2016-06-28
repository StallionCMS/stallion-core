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

package io.stallion.monitoring;

import java.util.Map;

import static io.stallion.utils.Literals.*;


public class SystemInformation {
    private String remoteAddr = "";
    private String xForwardedFor = "";
    private String xRealIp = "";
    private String guessedIp = "";
    private Map<String, String> jarBuildDates = map();
    private String instanceHostName = "";
    private String instanceDomain = "";
    private String targetPath = "";
    private String deployDate = "";
    private int port;
    private String env;
    private String xForwardedHost = "";

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getxForwardedFor() {
        return xForwardedFor;
    }

    public void setxForwardedFor(String xForwardedFor) {
        this.xForwardedFor = xForwardedFor;
    }

    public String getxRealIp() {
        return xRealIp;
    }

    public void setxRealIp(String xRealIp) {
        this.xRealIp = xRealIp;
    }

    public String getGuessedIp() {
        return guessedIp;
    }

    public void setGuessedIp(String guessedIp) {
        this.guessedIp = guessedIp;
    }

    public Map<String, String> getJarBuildDates() {
        return jarBuildDates;
    }

    public void setJarBuildDates(Map<String, String> jarBuildDates) {
        this.jarBuildDates = jarBuildDates;
    }

    public String getInstanceHostName() {
        return instanceHostName;
    }

    public void setInstanceHostName(String instanceHostName) {
        this.instanceHostName = instanceHostName;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getDeployDate() {
        return deployDate;
    }

    public void setDeployDate(String deployDate) {
        this.deployDate = deployDate;
    }

    public String getInstanceDomain() {
        return instanceDomain;
    }

    public void setInstanceDomain(String instanceDomain) {
        this.instanceDomain = instanceDomain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getxForwardedHost() {
        return xForwardedHost;
    }

    public void setxForwardedHost(String xForwardedHost) {
        this.xForwardedHost = xForwardedHost;
    }
}
