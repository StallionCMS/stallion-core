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

package io.stallion.settings;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.exceptions.UsageException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.requests.RouteDefinition;
import io.stallion.services.Log;
import io.stallion.settings.childSections.*;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.empty;
import static io.stallion.utils.Literals.*;

public class Settings implements ISettings {



    // Child sections
    private DbConfig database = null;
    private UserSettings users = null;
    private EmailSettings email = null;
    private CustomSettings custom = null;
    private CloudStorageSettings cloudStorage = null;
    private StyleSettings styles = null;

    private OAuthSettings oAuth;


    // Site information
    @SettingMeta()
    private String authorName = null;
    @SettingMeta()
    private String siteName = null;
    @SettingMeta(val="A Stallion Web Site")
    private String defaultTitle = null;
    @SettingMeta()
    private String metaDescription = null;
    @SettingMeta(val="Stallion")
    private String metaGenerator = null;
    @SettingMeta(val="", help = "The email address users of the site should contact in case of problems. This will be publicly viewable.")
    private String supportEmail;
    @SettingMeta(valLong = 1430510589000L)
    private Long appCreatedMillis;



    // File system info
    @SettingMeta()
    private String dataDirectory = null;
    @SettingMeta()
    private String targetFolder = null;

    // Runtime mode config
    @SettingMeta()
    private Boolean localMode;
    @SettingMeta()
    private Boolean devMode;
    @SettingMeta()
    private String logFile;
    @SettingMeta()
    private Boolean logToConsole;
    @SettingMeta()
    private Boolean logToFile;
    @SettingMeta()
    private Boolean debug;
    @SettingMeta()
    private Boolean emailErrors;
    @SettingMeta()
    private StrictnessLevel strictnessLevel;
    @SettingMeta(val="INFO")
    private String logLevel;
    private Map<String, String> packageLogLevels = new HashMap<>();
    private Integer nodeNumber = 1;
    @SettingMeta(val="x-Real-Ip", help="The HTTP header that contains the original client IP address, by default, with nginx proxying, this is x-Real-Ip")
    private String ipHeaderName;
    @SettingMeta(help="The name of the executable jar file, default is stallion.", val = "stallion")
    private String executableName;

    private String env;
    @SettingMeta()
    private Boolean bundleDebug;

    // Routes and rewrites
    @SettingMeta(cls=ArrayList.class)
    private List<RouteDefinition> routes = new ArrayList<>();
    @SettingMeta(cls=HashMap.class)
    private Map<String, String> redirects = null;
    @SettingMeta(cls=HashMap.class)
    private Map<String, String> rewrites = null;
    @SettingMeta(cls=ArrayList.class)
    private List<String[]> rewritePatterns = null;
    @SettingMeta(cls=ArrayList.class)
    private List<Map.Entry<Pattern, String>> rewriteCompiledPatterns = null;
    @SettingMeta(cls=ArrayList.class)
    private List<SecondaryDomain> secondaryDomains;
    private Map<String, SecondaryDomain> secondaryDomainByDomain  = map();

    // Web Serving
    @SettingMeta(valInt=8090)
    private Integer port;
    @SettingMeta(val="http://localhost:{port}")
    private String cdnUrl;
    @SettingMeta(val="http://localhost:{port}")
    private String siteUrl;

    // Default code directories and files
    @SettingMeta(cls=ArrayList.class)
    private List<TargetFolder> folders;
    @SettingMeta(val="page.jinja")
    private String pageTemplate = "page.jinja";

    // Email

    // Publishing
    @SettingMeta(cls=ArrayList.class)
    private List<PublishingConfig> publishing;



    // Time
    @SettingMeta()
    private String timeZone;// = ZoneId.systemDefault().toString();
    @SettingMeta()
    private ZoneId timeZoneId;// = ZoneId.systemDefault();


    // Anti-spam
    @SettingMeta(val = "")
    private String antiSpamSecret;
    @SettingMeta(valBoolean = false)
    private Boolean disableFormSubmissions;

    // Health
    @SettingMeta(cls=ArrayList.class)
    private List<String[]> healthCheckEndpoints = new ArrayList<>();
    @SettingMeta()
    private String healthCheckSecret;

    private static Settings _instance;

    public static boolean isNull() {
        return _instance == null;
    }

    public static void shutdown() {
        _instance = null;
    }

        public static Settings instance() {
            if (_instance == null) {
            throw new UsageException("You must call load() before instance().");
        }
        return _instance;
    }

    public static Settings init(String env, CommandOptionsBase options) {
        _instance = new SettingsLoader().loadSettings(env, options.getTargetPath(), "stallion", Settings.class, options);

        _instance.setCdnUrl(_instance.getCdnUrl().replace("{port}", _instance.getPort().toString()));
        _instance.setSiteUrl(_instance.getSiteUrl().replace("{port}", _instance.getPort().toString()));
        return _instance;
    }

    public void assignDefaults() {
        if (getLocalMode() == null) {
            if (empty(System.getenv().getOrDefault("STALLION_DEPLOY_TIME", ""))) {
                setLocalMode(true);
            } else {
                setLocalMode(false);
            }
        }

        if (bundleDebug == null) {
            bundleDebug = getLocalMode();
        }



        if (getDebug() == null) {
            if (getEnv().equals("prod") && !getLocalMode()) {
                setDebug(false);
            } else {
                setDebug(true);
            }
        }

        if (timeZone == null) {
            timeZone = ZoneId.systemDefault().toString();
        }
        if (timeZoneId == null) {
            timeZoneId = ZoneId.of(timeZone);
        }


        if (logFile == null) {
            logFile = "/tmp/log/stallion/" + StringUtils.strip(GeneralUtils.slugify(targetFolder), "-") + ".log";
        }
        if (logToConsole == null) {
            logToConsole = getLocalMode();
        }
        if (logToFile == null) {
            logToFile = !logToConsole;
        }

        if (getEmailErrors() == null) {
            if (getEnv().equals("prod") && !getLocalMode()) {
                setEmailErrors(true);
            } else {
                setEmailErrors(false);
            }
        }

        if (getStrictnessLevel() == null) {
            if (getEnv().equals("prod") && !getLocalMode()) {
                setStrictnessLevel(StrictnessLevel.LAX);
            } else {
                setStrictnessLevel(StrictnessLevel.STRICT);
            }
        }

        if (!empty(secondaryDomains)) {
            secondaryDomainByDomain = map();
            for (SecondaryDomain d: secondaryDomains) {
                secondaryDomainByDomain.put(d.getDomain(), d);
            }
        }


        if (!StringUtils.isEmpty(getDataDirectory())) {
            if (!getDataDirectory().startsWith("/")) {
                setDataDirectory(targetFolder + "/" + getDataDirectory());
            }
        } else {
            setDataDirectory(targetFolder + "/app-data");
        }
        if (getDataDirectory().endsWith("/")) {
            setDataDirectory(getDataDirectory().substring(0, getDataDirectory().length() - 1));
        }

        if (getRewrites() != null) {
            // THe Toml library has a bug whereby if a map key is quoted, it keeps the
            // quotes as part of the key, rather than using the String inside the quotes
            Set<String> keys = new HashSet<>(getRewrites().keySet());
            for(String key: keys) {
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    getRewrites().put(key.substring(1, key.length()-1), getRewrites().get(key));
                }
            }
        }

        if (getRedirects() != null) {
            // THe Toml library has a bug whereby if a map key is quoted, it keeps the
            // quotes as part of the key, rather than using the String inside the quotes
            Set<String> keys = new HashSet<>(getRedirects().keySet());
            for(String key: keys) {
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    getRedirects().put(key.substring(1, key.length()-1), getRedirects().get(key));
                }
            }
        }

        if (getRewritePatterns() != null && getRewritePatterns().size() > 0) {
            if (rewriteCompiledPatterns == null) {
                rewriteCompiledPatterns = new ArrayList();
            }
            for(String[] entry: getRewritePatterns()) {
                if (entry.length != 2) {
                    Log.warn("Invalid rewritePatterns entry, size should be 2 but is {0} {1}", entry.length, entry);
                }
                Pattern pattern = Pattern.compile(entry[0]);
                Map.Entry<Pattern, String> kv = new AbstractMap.SimpleEntry<Pattern, String>(pattern, entry[1]);
                getRewriteCompiledPatterns().add(kv);
            }
        }

        if (getEmail() != null) {
            // By default, in debug mode, we do not want to send real emails to people
            if (getEmail().getRestrictOutboundEmails() == null) {
                getEmail().setRestrictOutboundEmails(getDebug());
            }
            if (getEmail().getOutboundEmailTestAddress() == null) {
                if (getEmail().getAdminEmails() != null && getEmail().getAdminEmails().size() > 0) {
                    getEmail().setOutboundEmailTestAddress(getEmail().getAdminEmails().get(0));
                }
            }
            if (!empty(getEmail().getAllowedTestingOutboundEmailPatterns())) {
                if (getEmail().getAllowedTestingOutboundEmailCompiledPatterns() == null) {
                    getEmail().setAllowedTestingOutboundEmailCompiledPatterns(new ArrayList<>());
                }
                for (String emailPattern : getEmail().getAllowedTestingOutboundEmailPatterns()) {
                    getEmail().getAllowedTestingOutboundEmailCompiledPatterns().add(Pattern.compile(emailPattern));
                }
            }
        }


    }

    /**
     * Gets the scheme (https or http) for a given secondary domain
     * @return
     */
    public String getSchemeForSecondaryDomain(String domain) {
        SecondaryDomain d = secondaryDomainByDomain.get(domain);
        return or(d.getScheme(), getSiteUrlScheme());
    }

    /**
     * Get siteUrl scheme
     * @return
     */
    public String getSiteUrlScheme() {
        if (getSiteUrl().startsWith("https:")) {
            return "https";
        } else {
            return "http";
        }
    }

    public SecondaryDomain getSecondaryDomainByDomain(String domain) {
        return secondaryDomainByDomain.get(domain);
    }

    public boolean isStrict() {
        return StrictnessLevel.STRICT.equals(getStrictnessLevel());
    }

    public List<TargetFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<TargetFolder> folders) {
        this.folders = folders;
    }

    public DbConfig getDatabase() {
        return database;
    }

    public void setDatabase(DbConfig database) {
        this.database = database;
    }

    public UserSettings getUsers() {
        return users;
    }

    public void setUsers(UserSettings users) {
        this.users = users;
    }

    public EmailSettings getEmail() {
        return email;
    }

    public void setEmail(EmailSettings email) {
        this.email = email;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public CustomSettings getCustom() {
        return custom;
    }

    public void setCustom(CustomSettings custom) {
        this.custom = custom;
    }

    public String getPageTemplate() {
        return pageTemplate;
    }

    public void setPageTemplate(String pageTemplate) {
        this.pageTemplate = pageTemplate;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public List<RouteDefinition> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDefinition> routes) {
        this.routes = routes;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public ZoneId getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(ZoneId timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public Boolean getDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public Map<String, String> getPackageLogLevels() {
        return packageLogLevels;
    }

    public void setPackageLogLevels(Map<String, String> packageLogLevels) {
        this.packageLogLevels = packageLogLevels;
    }



    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Boolean getBundleDebug() {
        return bundleDebug;
    }

    public void setBundleDebug(Boolean bundleDebug) {
        this.bundleDebug = bundleDebug;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public StrictnessLevel getStrictnessLevel() {
        return strictnessLevel;
    }

    public void setStrictnessLevel(StrictnessLevel strictnessLevel) {
        this.strictnessLevel = strictnessLevel;
    }

    public List<String[]> getHealthCheckEndpoints() {
        return healthCheckEndpoints;
    }

    public void setHealthCheckEndpoints(List<String[]> healthCheckEndpoints) {
        this.healthCheckEndpoints = healthCheckEndpoints;
    }

    public String getHealthCheckSecret() {
        return healthCheckSecret;
    }

    public void setHealthCheckSecret(String healthCheckSecret) {
        this.healthCheckSecret = healthCheckSecret;
    }


    public Boolean getEmailErrors() {
        return emailErrors;
    }

    public void setEmailErrors(Boolean emailErrors) {
        this.emailErrors = emailErrors;
    }

    public Map<String, String> getRedirects() {
        return redirects;
    }

    public void setRedirects(Map<String, String> redirects) {
        this.redirects = redirects;
    }

    public String getMetaGenerator() {
        return metaGenerator;
    }

    public void setMetaGenerator(String metaGenerator) {
        this.metaGenerator = metaGenerator;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public Boolean getLogToConsole() {
        return logToConsole;
    }

    public void setLogToConsole(Boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

    public Boolean getLogToFile() {
        return logToFile;
    }

    public void setLogToFile(Boolean logToFile) {
        this.logToFile = logToFile;
    }

    public Boolean getLocalMode() {
        return localMode;
    }

    public void setLocalMode(Boolean localMode) {
        this.localMode = localMode;
    }



    public Map<String, String> getRewrites() {
        return rewrites;
    }

    public void setRewrites(Map<String, String> rewrites) {
        this.rewrites = rewrites;
    }


    public List<SecondaryDomain> getSecondaryDomains() {
        return secondaryDomains;
    }

    public Settings setSecondaryDomains(List<SecondaryDomain> secondaryDomains) {
        this.secondaryDomains = secondaryDomains;
        return this;
    }

    public List<Map.Entry<Pattern, String>> getRewriteCompiledPatterns() {
        return rewriteCompiledPatterns;
    }

    public void setRewriteCompiledPatterns(List<Map.Entry<Pattern, String>> rewriteCompiledPatterns) {
        this.rewriteCompiledPatterns = rewriteCompiledPatterns;
    }

    public List<String[]> getRewritePatterns() {
        return rewritePatterns;
    }

    public void setRewritePatterns(List<String[]> rewritePatterns) {
        this.rewritePatterns = rewritePatterns;
    }

    public CloudStorageSettings getCloudStorage() {
        return cloudStorage;
    }

    public void setCloudStorage(CloudStorageSettings cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    public StyleSettings getStyles() {
        return styles;
    }

    public void setStyles(StyleSettings style) {
        this.styles = style;
    }

    public Integer getNodeNumber() {
        return nodeNumber;
    }

    public Settings setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
        return this;
    }

    public String getAntiSpamSecret() {
        return antiSpamSecret;
    }

    public Settings setAntiSpamSecret(String antiSpamSecret) {
        this.antiSpamSecret = antiSpamSecret;
        return this;
    }

    public Boolean getDisableFormSubmissions() {
        return disableFormSubmissions;
    }

    public Settings setDisableFormSubmissions(Boolean disableFormSubmissions) {
        this.disableFormSubmissions = disableFormSubmissions;
        return this;
    }


    public String getIpHeaderName() {
        return ipHeaderName;
    }

    public Settings setIpHeaderName(String ipHeaderName) {
        this.ipHeaderName = ipHeaderName;
        return this;
    }

    public String getExecutableName() {
        return executableName;
    }

    public Settings setExecutableName(String executableName) {
        this.executableName = executableName;
        return this;
    }

    public OAuthSettings getoAuth() {
        return oAuth;
    }

    public Settings setoAuth(OAuthSettings oAuth) {
        this.oAuth = oAuth;
        return this;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public Settings setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public List<PublishingConfig> getPublishing() {
        return publishing;
    }


    public Settings setPublishing(List publishing) {
        if (publishing == null) {
            this.publishing = null;
            return this;
        }
        if (publishing.size() == 0) {
            this.publishing = new ArrayList<>();
            return this;
        }
        this.publishing = new ArrayList<>();
        if (publishing.get(0) instanceof Map) {
            for(Object o: publishing) {
                PublishingConfig config = new PublishingConfig();
                Map<String, Object> m = (Map<String, Object>)o;
                for(Map.Entry<String, Object> e: m.entrySet()) {
                    Object value = e.getValue();
                    if (e.getKey().equals("basePort") && e.getValue() instanceof Long) {
                        value = Math.toIntExact((Long)value);
                    }
                    PropertyUtils.setProperty(config, e.getKey(), value);
                }
                this.publishing.add(config);
            }
        } else {
            this.publishing.addAll(publishing);
        }
        return this;
    }

    public Long getAppCreatedMillis() {
        return appCreatedMillis;
    }

    public Settings setAppCreatedMillis(Long appCreatedMillis) {
        this.appCreatedMillis = appCreatedMillis;
        return this;
    }
}
