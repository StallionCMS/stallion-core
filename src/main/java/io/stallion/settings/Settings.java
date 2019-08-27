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

package io.stallion.settings;

import io.stallion.boot.CommandOptionsBase;
import io.stallion.boot.DataEnvironmentType;
import io.stallion.boot.ModeFlags;
import io.stallion.boot.StallionRunAction;
import io.stallion.exceptions.ConfigException;
import io.stallion.exceptions.UsageException;
import io.stallion.reflection.PropertyUtils;
import io.stallion.requests.RouteDefinition;
import io.stallion.services.Log;
import io.stallion.settings.childSections.*;
import io.stallion.utils.DateUtils;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static io.stallion.utils.Literals.*;

public class Settings implements ISettings {

    private ModeFlags modeFlags = null;

    // Child sections
    private DbConfig database = null;
    private UserSettings users = null;
    private EmailSettings email = null;
    private CustomSettings custom = null;
    private CloudStorageSettings cloudStorage = null;
    private StyleSettings styles = null;
    private CorsSettings cors = null;
    private OAuthSettings oAuth;
    private SecretsSettings secrets;
    private UserUploadSettings userUploads;


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

    // Tuning
    @SettingMeta(valLong = 5000000)
    private Long filterCacheSize;


    // File system info
    @SettingMeta()
    private String dataDirectory = null;
    @SettingMeta()
    private String targetFolder = null;


    @SettingMeta()
    private Boolean lightweightMode;

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
    @SettingMeta(cls=HashMap.class)
    private Map<String, String> packageLogLevels;
    private Integer nodeNumber = 1;
    @SettingMeta(val="x-Real-Ip", help="The HTTP header that contains the original client IP address, by default, with nginx proxying, this is x-Real-Ip")
    private String ipHeaderName;


    @SettingMeta(cls=HashMap.class)
    private Map<String, String> systemProperties;

    @SettingMeta(cls=HashMap.class)
    private Map<String, String> jerseyInitParams;


    private String env;
    @SettingMeta()
    private Boolean bundleDebug;
    @SettingMeta(help="Is this settings file for a PRODUCTION, STAGING, TEST, or SANDBOX environment? Will be inferred from the environment name if left null.")
    private DataEnvironmentType dataEnvironmentType;

    // Routes and rewrites
    @SettingMeta(val="SAMEORIGIN")
    private String xFrameOptions;
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
    @SettingMeta(cls=ArrayList.class)
    private List<AssetPreprocessorConfig> assetPreprocessors;

    // Web Serving
    @SettingMeta(valInt=8090)
    private Integer port;
    @SettingMeta(val="http://localhost:{port}")
    private String cdnUrl;
    @SettingMeta(val="http://localhost:{port}")
    private String siteUrl;
    @SettingMeta(val="25M")
    private String nginxClientMaxBodySize;
    @SettingMeta(val="3600")
    private String nginxProxyReadTimeout;

    // Default code directories and files
    @SettingMeta(cls=ArrayList.class)
    private List<ContentFolder> folders;
    @SettingMeta(val="page.jinja")
    private String pageTemplate = "page.jinja";

    // Email

    // Publishing
    //@SettingMeta(cls=ArrayList.class)
    //private List<PublishingConfig> publishing;


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

    public boolean isEmailConfigured() {
        return getEmail() != null && !empty(getEmail().getHost());
    }

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

    public static Settings init(String env, CommandOptionsBase options, StallionRunAction action) {
        _instance = new SettingsLoader().loadSettings(env, options.getTargetPath(), "stallion", Settings.class, options, action);

        _instance.setCdnUrl(_instance.getCdnUrl().replace("{port}", _instance.getPort().toString()));
        _instance.setSiteUrl(_instance.getSiteUrl().replace("{port}", _instance.getPort().toString()));




        return _instance;
    }


    public void assignDefaults() {


        if (bundleDebug == null) {
            bundleDebug = getModeFlags().isDeveloperMode();
        }

        if (systemProperties != null) {
            stripKeyQuotes(systemProperties);
        }
        if (jerseyInitParams != null) {
            stripKeyQuotes(jerseyInitParams);
        }


        if (getDebug() == null) {
            if ((getModeFlags().isProduction() || getModeFlags().isStaging()) && !getModeFlags().isDeveloperMode()) {
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
            String nowString = DateUtils.formatNow("yyyy-MM-dd-HHmmss");
            String base = "";
            try {
                if (!empty(siteUrl)) {
                    base = new URL(siteUrl.replace(":{port}", "")).getHost();
                }
            } catch(IOException e) {
                Log.exception(e, "Error parsing siteUrl " + siteUrl);
            }
            logFile = "/tmp/log/stallion/" + base + "-" + nowString + "-" + StringUtils.strip(GeneralUtils.slugify(targetFolder), "-") + ".log";
        }
        if (logToConsole == null) {
            logToConsole = getLocalMode();
        }
        if (logToFile == null) {
            logToFile = !logToConsole;
        }

        if (getEmailErrors() == null) {
            if ((getEnv().equals("prod") || getEnv().equals("qa")) && !getLocalMode()) {
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
            stripKeyQuotes(getRedirects());

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

        if (getSecrets() == null) {
            setSecrets(new SecretsSettings());
        }

        if (new File(targetFolder + "/pages").isDirectory()) {
            if (folders == null) {
                folders = new ArrayList<>();
            }
            folders.add(new ContentFolder().setPath(targetFolder + "/pages").setType("markdown").setItemTemplate(getPageTemplate()));
        }

        if (userUploads != null && empty(userUploads.getUploadsDirectory())) {
            userUploads.setUploadsDirectory(getDataDirectory() + "/st-user-file-uploads");
        }



    }

    private void stripKeyQuotes(Map<String, String> theMap) {
        Set<String> keysCopy = set();
        for(String key: theMap.keySet()) {
            keysCopy.add(key);
        }

        for(String key :keysCopy) {
            if (key.startsWith("\"") && key.endsWith("\"")) {
                theMap.put(key.substring(1, key.length()-1), theMap.get(key));
            }
            theMap.remove(key);
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

    /**
     * In strict mode, more errors will be exceptions that stop all processing. For instance, missing template variables
     * will cause exceptions rather than being ignored.
     * @return
     */
    public boolean isStrict() {
        return StrictnessLevel.STRICT.equals(getStrictnessLevel());
    }

    /**
     * ContentFolders are folders in your local site directory that are to be registered with the data access controller.
     * The .txt will be converted into accessible web pages in your site.
     * @return
     */
    public List<ContentFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<ContentFolder> folders) {
        this.folders = folders;
    }

    /**
     * The database configuration
     * @return
     */
    public DbConfig getDatabase() {
        return database;
    }

    public void setDatabase(DbConfig database) {
        this.database = database;
    }

    /**
     * The user configuration
     * @return
     */
    public UserSettings getUsers() {
        return users;
    }

    public void setUsers(UserSettings users) {
        this.users = users;
    }


    public UserUploadSettings getUserUploads() {
        return userUploads;
    }

    public Settings setUserUploads(UserUploadSettings userUploads) {
        this.userUploads = userUploads;
        return this;
    }

    /**
     * Email configuration for Stallion sending emails via SMTP
     * @return
     */
    public EmailSettings getEmail() {
        return email;
    }

    public void setEmail(EmailSettings email) {
        this.email = email;
    }


    /**
     * Cross-Origin Request configuration -- allow endpoints to respond to cross-origin requests.
     * @return
     */
    public CorsSettings getCors() {
        return cors;
    }

    public Settings setCors(CorsSettings cors) {
        this.cors = cors;
        return this;
    }

    /**
     * Where all data stored to flat-file by the Controllers and Persisters will actually live in the
     * file system. This will be "app-data" under the site directory by default.
     * @return
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * A hashtable of arbitrary user defined settings.
     * @return
     */
    public CustomSettings getCustom() {
        return custom;
    }

    public void setCustom(CustomSettings custom) {
        this.custom = custom;
    }

    /**
     * The default template to use to render pages
     *
     * @return
     */
    public String getPageTemplate() {
        return pageTemplate;
    }

    public void setPageTemplate(String pageTemplate) {
        this.pageTemplate = pageTemplate;
    }

    /**
     * The base URL for CDN (Content Delivery Network) that will pass through to your site. This is used
     * by AssetsController.url() or AssetsController.bundle() to build URL's to your assets. If empty, will use
     * the site URL instead. Using a CDN can improve performance as it will cache requested assets in data centers
     * nearer to the end user.
     *
     * @return
     */
    public String getCdnUrl() {
        return cdnUrl;
    }

    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    /**
     * The base URL at which the site lives. Used to build links internally, also used for checking cross-origin
     * requests.
     *
     * @return
     */
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

    /**
     * Run in debug mode. True by default when env=local. In debug mode, the full stack trace of exceptions
     * will be rendered to the HTML output.
     *
     * @return
     */
    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    /**
     * Should be a time zone parseable by ZoneId.of() Used by the DateUtils.renderLocalDate() method when there
     * is no time zone for the particular user.
     *
     * @return
     */
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

    /**
     * Usually set by the command line options, rather than the stallion.toml file.
     *
     * If true, templates and assets will automatically be checked for changes from the original source
     * and re-compiled if they have changed.
     *
     * @return
     */
    public Boolean getDevMode() {
        return getModeFlags().isDeveloperMode();
    }


    /**
     * Set the log level - INFO, FINE, FINER, FINEST
     *
     * @return
     */
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


    /**
     * The current environment name, set by command line options.
     *
     * @return
     */
    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    /**
     * True by default if in debug mode, false on production. If true,
     * bundle files will be included on the page as individual source files, rather than as a concatenated,
     * minified file.
     *
     * @return
     */
    public Boolean getBundleDebug() {
        return bundleDebug;
    }

    public void setBundleDebug(Boolean bundleDebug) {
        this.bundleDebug = bundleDebug;
    }

    /**
     * The name of your site, accessible in templates via {{ site.name }}
     *
     * @return
     */
    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * The default title of your site, will be used in the HTML title tag in the default base template
     * if no other title for a page is defined.
     *
     *
     * @return
     */
    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    /**
     * The default meta tag description value. Used if there is no override for the particular page or endpoint.
     * @return
     */
    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    /**
     * Get the name of the author of the site
     *
     * @return
     */
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * The port the server should run on. Usually set by the command line options.
     *
     * @return
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The root site folder that contains the conf directory and the conf/stallion.toml files.
     * This is always set via the command line options, or defaults to the current working directory.
     *
     * @return
     */
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

    /**
     * A list of paths that will be checked for a 200 response when the health check
     * is run.
     *
     * @return
     */
    public List<String[]> getHealthCheckEndpoints() {
        return healthCheckEndpoints;
    }

    public void setHealthCheckEndpoints(List<String[]> healthCheckEndpoints) {
        this.healthCheckEndpoints = healthCheckEndpoints;
    }

    /**
     * A secret token that must be passed in when accessing the health endpoints.
     *
     * @return
     */
    public String getHealthCheckSecret() {
        return healthCheckSecret;
    }

    public void setHealthCheckSecret(String healthCheckSecret) {
        this.healthCheckSecret = healthCheckSecret;
    }


    /**
     * If true, exceptions will emailed to the admin defined in the email configuration.
     *
     * @return
     */
    public Boolean getEmailErrors() {
        return emailErrors;
    }

    public void setEmailErrors(Boolean emailErrors) {
        this.emailErrors = emailErrors;
    }

    /**
     * A map of 301 redirects in the form of original URL or path to destination URL or path.
     *
     * @return
     */
    public Map<String, String> getRedirects() {
        return redirects;
    }

    public void setRedirects(Map<String, String> redirects) {
        this.redirects = redirects;
    }

    /**
     * The meta tag value for generator. "Stallion" by default.
     * @return
     */
    public String getMetaGenerator() {
        return metaGenerator;
    }

    public void setMetaGenerator(String metaGenerator) {
        this.metaGenerator = metaGenerator;
    }

    /**
     * The path to the log file when file-based logging is on.
     *
     * @return
     */
    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    /**
     * If true, log to the console instead of file. This defaults to true in local mode.
     *
     * @return
     */
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

    /**
     * True if this is a developer running locally, false if this is running deployed on a server.
     *
     * If localMode is true, production jobs and tasks will not be run and logging will go to the console.
     *
     *
     * @return
     */
    public Boolean getLocalMode() {
        return getModeFlags().isLocalMode();
    }



    public Boolean getLightweightMode() {
        return lightweightMode;
    }

    public Settings setLightweightMode(Boolean lightweightMode) {
        this.lightweightMode = lightweightMode;
        return this;
    }

    /**
     * A mapping of internal rewrites, from source path to destintion path.
     * @return
     */
    public Map<String, String> getRewrites() {
        return rewrites;
    }

    public void setRewrites(Map<String, String> rewrites) {
        this.rewrites = rewrites;
    }

    /**
     * A list of domains that content is also accesible at, and their mapping
     * to URL paths.
     *
     * @return
     */
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

    /**
     * A list of arrays, each array has two values, a source regular expression and a destination path.
     * @return
     */
    public List<String[]> getRewritePatterns() {
        return rewritePatterns;
    }

    public void setRewritePatterns(List<String[]> rewritePatterns) {
        this.rewritePatterns = rewritePatterns;
    }

    /**
     * Configuration for integration with a cloud file storage, such as S3.
     *
     * @return
     */
    public CloudStorageSettings getCloudStorage() {
        return cloudStorage;
    }

    public void setCloudStorage(CloudStorageSettings cloudStorage) {
        this.cloudStorage = cloudStorage;
    }

    /**
     * Default CSS styles and colors that will be used in the default templates
     * for log in, password reset emails, etc.
     *
     * @return
     */
    public StyleSettings getStyles() {
        return styles;
    }

    public void setStyles(StyleSettings style) {
        this.styles = style;
    }

    /**
     * For deployed sites using stablehand, which node this is.
     *
     * @return
     */
    public Integer getNodeNumber() {
        return nodeNumber;
    }

    public Settings setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
        return this;
    }

    /**
     * A random token used for preventing spam in form submissions and comments.
     *
     * @return
     */
    public String getAntiSpamSecret() {
        return antiSpamSecret;
    }

    public Settings setAntiSpamSecret(String antiSpamSecret) {
        this.antiSpamSecret = antiSpamSecret;
        return this;
    }

    /**
     * If true, the default form submission endpoint will be disabled.
     * @return
     */
    public Boolean getDisableFormSubmissions() {
        return disableFormSubmissions;
    }

    public Settings setDisableFormSubmissions(Boolean disableFormSubmissions) {
        this.disableFormSubmissions = disableFormSubmissions;
        return this;
    }


    /**
     * If there is a proxy server in front of Stallion, that proxy server will set
     * the IP address of the original requester and put it in a header. Add that header
     * name here so that Stallion can know the IP of the original requester.
     *
     * @return
     */
    public String getIpHeaderName() {
        return ipHeaderName;
    }

    public Settings setIpHeaderName(String ipHeaderName) {
        this.ipHeaderName = ipHeaderName;
        return this;
    }

    /**
     * Configuration for enabling users to give out OAuth access
     *
     * @return
     */
    public OAuthSettings getoAuth() {
        return oAuth;
    }

    public Settings setoAuth(OAuthSettings oAuth) {
        this.oAuth = oAuth;
        return this;
    }

    public Long getFilterCacheSize() {
        return filterCacheSize;
    }

    public Settings setFilterCacheSize(Long filterCacheSize) {
        this.filterCacheSize = filterCacheSize;
        return this;
    }

    public SecretsSettings getSecrets() {
        return secrets;
    }

    public Settings setSecrets(SecretsSettings secrets) {
        this.secrets = secrets;
        return this;
    }

    /**
     * Included in the templates for error pages.
     *
     * @return
     */
    public String getSupportEmail() {
        return supportEmail;
    }

    public Settings setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }


    /*
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
    */

    public Long getAppCreatedMillis() {
        return appCreatedMillis;
    }

    public Settings setAppCreatedMillis(Long appCreatedMillis) {
        this.appCreatedMillis = appCreatedMillis;
        return this;
    }

    public List<AssetPreprocessorConfig> getAssetPreprocessors() {
        return assetPreprocessors;
    }

    public Settings setAssetPreprocessors(List assetPreProcessors) {
        this.assetPreprocessors = convertMapListToObjects(assetPreProcessors, AssetPreprocessorConfig.class);
        for (AssetPreprocessorConfig config: this.assetPreprocessors) {
            if (empty(config.getCommand()) || empty(config.getName()) || empty(config.getExtension())) {
                throw new ConfigException("Asset Pre-processors must have a valid command, name, and extension");
            }
        }
        return this;
    }

    protected List convertMapListToObjects(List maps, Class targetClass) {
        if (maps.size() == 0) {
            return maps;
        }
        if (targetClass.isAssignableFrom(maps.get(0).getClass())) {
            return maps;
        }
        List items = new ArrayList<>();
        for (Object o: maps) {
            Map<String, Object> map = (Map<String, Object>)o;
            try {
                Object instance = targetClass.newInstance();
                for(Map.Entry<String, Object> e: map.entrySet()) {
                    Object value = e.getValue();
                    if (e.getKey().equals("basePort") && e.getValue() instanceof Long) {
                        value = Math.toIntExact((Long)value);
                    }
                    PropertyUtils.setProperty(instance, e.getKey(), value);
                }
                items.add(instance);

            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return items;
    }



    public String getxFrameOptions() {
        return xFrameOptions;
    }

    public Settings setxFrameOptions(String xFrameOptions) {
        this.xFrameOptions = xFrameOptions;
        return this;
    }



    public String getNginxClientMaxBodySize() {
        return nginxClientMaxBodySize;
    }

    public Settings setNginxClientMaxBodySize(String nginxClientMaxBodySize) {
        this.nginxClientMaxBodySize = nginxClientMaxBodySize;
        return this;
    }


    public String getNginxProxyReadTimeout() {
        return nginxProxyReadTimeout;
    }

    public Settings setNginxProxyReadTimeout(String nginxProxyReadTimeout) {
        this.nginxProxyReadTimeout = nginxProxyReadTimeout;
        return this;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public Settings setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
        return this;
    }

    public Map<String, String> getJerseyInitParams() {
        return jerseyInitParams;
    }

    public Settings setJerseyInitParams(Map<String, String> jerseyInitParams) {
        this.jerseyInitParams = jerseyInitParams;
        return this;
    }


    public ModeFlags getModeFlags() {
        return modeFlags;
    }

    public Settings setModeFlags(ModeFlags modeFlags) {
        this.modeFlags = modeFlags;
        return this;
    }

    public DataEnvironmentType getDataEnvironmentType() {
        return dataEnvironmentType;
    }

    public Settings setDataEnvironmentType(DataEnvironmentType dataEnvironmentType) {
        this.dataEnvironmentType = dataEnvironmentType;
        return this;
    }
}
