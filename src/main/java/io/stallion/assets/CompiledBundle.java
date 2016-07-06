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

package io.stallion.assets;

import static io.stallion.utils.Literals.*;
import static io.stallion.Context.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.stallion.exceptions.NotFoundException;
import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.ResourceHelpers;
import net.sf.ehcache.search.expression.Not;

import javax.jws.soap.SOAPBinding;


public class CompiledBundle {
    private List<BundleFileBase> bundleFiles = list();
    private List<BundleFileBase> expandedBundleFiles = null;
    private Map<String, BundleFileBase> byPath = map();
    private String name;
    private String concatenatedCss = null;
    private String concatenatedJavaScript = null;
    private String concatenatedHeadJavaScript = null;

    public CompiledBundle(String name, BundleFileBase...bfs) {
        if (name.contains(".")) {
            throw new UsageException("A CompiledBundle name should not have an extension in it.");
        }
        this.name = name;
        this.bundleFiles = asList(bfs);
    }

    public String renderHtmlIncludes(String path) {
        if (Settings.instance().getBundleDebug()) {
            return renderHtmlIncludesForDebug(path);
        } else {
            return renderHtmlIncludesForProduction(path);
        }
    }

    public String renderHtmlIncludesForDebug(String path) {
        ensureBundleFilesHydrated();
        List<BundleFileBase> bfs = list();
        String extension = ".js";
        if (path.endsWith(".head.js")) {
            extension = ".head.js";
        } else if (path.endsWith(".css")) {
            extension = ".css";
        }
        boolean isCss = extension.equals(".css");
        boolean isHeadJavaScript = extension.equals(".head.js");
        boolean isJavaScript = extension.equals(".js");
        for (BundleFileBase bf: expandedBundleFiles) {
            bf.ensureHydrated();
            if (isCss && !empty(bf.getCss())) {
                bfs.add(bf);
            } else if (isHeadJavaScript && !empty(bf.getHeadJavascript())) {
                bfs.add(bf);
            } else if (isJavaScript && !empty(bf.getJavascript())) {
                bfs.add(bf);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (BundleFileBase bf: bfs) {
            if (isCss) {
                builder.append(MessageFormat.format("<link href=\"{0}/st-bundle-file/{1}/st-file-path/{2}{3}\" rel=\"stylesheet\" >\n        ", Settings.instance().getCdnUrl(), name, bf.getCurrentPath(), extension));
            } else {
                builder.append(MessageFormat.format("<script type=\"text/javascript\" src=\"{0}/st-bundle-file/{1}/st-file-path/{2}{3}\"></script>\n        ", Settings.instance().getCdnUrl(), name, bf.getCurrentPath(), extension));
            }
        }
        return builder.toString();
    }

    public String renderBundleFile(String originalPath) {
        String path = originalPath;
        boolean isCss = false;
        boolean isHeadJavaScript = false;
        boolean isJavaScript = false;
        if (originalPath.endsWith(".css")) {
            path = originalPath.substring(0, originalPath.length() - 4);
            isCss = true;
        } else if(originalPath.endsWith(".head.js")) {
            path = originalPath.substring(0, originalPath.length() - 8);
            isHeadJavaScript = true;
        } else if (originalPath.endsWith(".js")) {
            path = originalPath.substring(0, originalPath.length() - 3);
            isJavaScript = true;
        } else {
            throw new UsageException("Uknown extension on file " + originalPath);
        }

        if (expandedBundleFiles == null) {
            ensureBundleFilesHydrated();
        }
        BundleFileBase bf = byPath.getOrDefault(path, null);
        if (bf == null) {
            throw new NotFoundException("Could not find bundle file:" + path + " in bundle:" + name);
        }

        bf.ensureHydrated();

        if (isCss) {
            return bf.getCss();
        } else if (isHeadJavaScript) {
            return bf.getHeadJavascript();
        } else {
            return bf.getJavascript();
        }

    }

    public String renderHtmlIncludesForProduction(String path) {
        int i = path.indexOf(".");
        String extension = ".js";
        if (i > -1) {
            extension = path.substring(i);
        }
        if (path.endsWith(".css")) {
            return MessageFormat.format("<link href=\"{0}/st-concatenated-bundle/{1}{2}\" rel=\"stylesheet\" >", Settings.instance().getCdnUrl(), name, extension);
        } else {
            return MessageFormat.format("<script type=\"text/javascript\" src=\"{0}/st-concatenated-bundle/{1}{2}\"></script>", Settings.instance().getCdnUrl(), name, extension);
        }
    }

    public String renderConcatenated(String path) {
        if (path.endsWith(".head.js")) {
            return renderConcatenatedJavaScript();
        } else if (path.endsWith(".css")) {
            return renderConcatenatedCss();
        } else if (path.endsWith(".js")) {
            return renderConcatenatedJavaScript();
        }
        throw new NotFoundException("Uknown bundle file extension for path: " + path);
    }

    public String renderConcatenatedJavaScript() {
        ensureConcatenatedHydrated();
        return concatenatedJavaScript;
    }

    public String renderConcatenatedHeadJavaScript() {
        ensureConcatenatedHydrated();
        return concatenatedHeadJavaScript;
    }

    public String renderConcatenatedCss() {
        ensureConcatenatedHydrated();
        return concatenatedCss;
    }




    protected void ensureConcatenatedHydrated() {
        if (concatenatedCss != null && concatenatedHeadJavaScript != null && concatenatedJavaScript != null) {
            return;
        }
        ensureBundleFilesHydrated();
        StringBuilder cssBuilder = new StringBuilder();
        StringBuilder javaScriptBuilder = new StringBuilder();
        StringBuilder headJavaScriptBuilder = new StringBuilder();
        for(BundleFileBase bf: expandedBundleFiles) {
            bf.ensureHydrated();
            cssBuilder.append(bf.getCss());
            javaScriptBuilder.append(bf.getJavascript());
            headJavaScriptBuilder.append(bf.getHeadJavascript());
        }
        concatenatedCss = cssBuilder.toString();
        concatenatedJavaScript = javaScriptBuilder.toString();
        concatenatedHeadJavaScript = headJavaScriptBuilder.toString();
    }

    protected void ensureBundleFilesHydrated() {
        if (expandedBundleFiles == null || Settings.instance().getDevMode()) {
            List<BundleFileBase> toProcess = new ArrayList<>(bundleFiles);
            List<BundleFileBase> expanded = list();
            for(int x: safeLoop(1000)) {
                if (toProcess.size() == 0) {
                    break;
                }
                BundleFileBase bf = toProcess.remove(0);
                // If this is a directory, expand and add all files
                if (bf.getCurrentPath().endsWith("/") || bf.getCurrentPath().contains("*") || !bf.getCurrentPath().contains(".")) {
                    for(String path: ResourceHelpers.listFilesInDirectory(bf.getPlugin(), bf.getCurrentPath())) {
                        BundleFileBase newBf;
                        if (byPath.containsKey(path)) {
                            newBf = byPath.get(path);
                        } else if (path.endsWith(".vue")) {
                            newBf = new VueResourceBundleFile();
                        } else {
                            newBf = new ResourceBundleFile();
                        }
                        toProcess.add(newBf
                                .setProductionPath(path)
                                .setJsInHead(path.contains(".head."))
                                .setPlugin(bf.getPlugin())
                        );

                    }
                    continue;
                }
                byPath.put(bf.getCurrentPath(), bf);
                expanded.add(bf);
             }
            this.expandedBundleFiles = expanded;
        }

    }

    protected List<BundleFileBase> getBundleFiles() {
        return bundleFiles;
    }

    protected CompiledBundle setBundleFiles(List<BundleFileBase> bundleFiles) {
        this.bundleFiles = bundleFiles;
        return this;
    }

    public String getName() {
        return name;
    }
}
