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

package io.stallion.dataAccess.file;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.stallion.Context;
import io.stallion.dataAccess.ModelController;
import io.stallion.services.Log;
import io.stallion.services.PermaCache;
import io.stallion.settings.Settings;
import io.stallion.utils.Literals;
import io.stallion.utils.Markdown;
import io.stallion.utils.GeneralUtils;
import io.stallion.utils.json.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static io.stallion.utils.Literals.*;



public class TextFilePersister<T extends TextItem> extends FilePersisterBase<T> {
    private String bucket;
    private Class<T> clazz = null;
    private ModelController controller;
    private String targetPath;

    private static DateTimeFormatter[] localDateFormats = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };
    private static DateTimeFormatter[] zonedDateFormats = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm VV"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
    };


    private static final Set<String> allowedExtensions = set("md", "txt", "html", "htm");


    @Override
    public Set<String> getFileExtensions() {
        return allowedExtensions;
    }

    @Override
    public T doFetchOne(File file) {
        Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int lineCount = 0;
        StringBuffer buffer = new StringBuffer();
        for(int i: safeLoop(50000)) {
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (line == null) {
                break;
            }
            buffer.append(line + "\n");
            lineCount = i;
        }
        if (lineCount < 2) {
            return null;
        }
        String fileContent = buffer.toString();
        return fromString(fileContent, path);
    }



    @Override
    public void persist(T obj) {
        if (obj.getId() == null) {
            obj.setId(Context.dal().getTickets().nextId());
        }
        String filePath = fullFilePathForObj(obj);
        TextItem item = (TextItem)obj;
        String s = null;
        if (filePath.endsWith(".html") || filePath.endsWith(".html")) {
            s = toHtml(item);
        } else {
            s = stringify(item);
        }
        try {
            Files.write(Paths.get(filePath), s.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private static Pattern tomlPattern = Pattern.compile("\\-\\-\\-toml\\-\\-\\-([\\s\\S]*)\\-\\-\\-end\\-toml\\-\\-\\-");
    public T fromString(String fileContent, Path fullPath) {
        if (fullPath.toString().endsWith(".html") || fullPath.toString().endsWith(".htm")) {
            return fromHtml(fileContent, fullPath);
        }
        String relativePath = fullPath.toString().replace(getBucketFolderPath(), "");
        Path path = fullPath;

        T item = null;
        try {
            item = getModelClass().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        item.setTags(new ArrayList<String>())
                .setElementById(new HashMap<>())
                .setElements(new ArrayList<>())
                .setPublishDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("UTC")))
                .setTitle("")
                .setDraft(false)
                .setTemplate("")
                .setContent("")
                .setSlug("")
        ;

        /* Get the id and slug */

        item.setSlug(FilenameUtils.removeExtension(relativePath));
        if (!item.getSlug().startsWith("/")) {
            item.setSlug("/" + item.getSlug());
        }
        if (item.getSlug().endsWith("/index")) {
            item.setSlug(item.getSlug().substring(item.getSlug().length()-6));
        }

        if (empty(fileContent.trim())) {
            return item;
        }

        /* Parse out toml properties, if found */
        String tomlContent;
        Matcher tomlMatcher = tomlPattern.matcher(fileContent);
        if (tomlMatcher.find()) {
            tomlContent = tomlMatcher.group(1).trim();
            fileContent = tomlMatcher.replaceAll("\n");
            Map tomlMap = new Toml().read(tomlContent).to(HashMap.class);
            for (Object key: tomlMap.keySet()) {
                Object value = tomlMap.get(key);
                setProperty(item, key.toString(), value);
            }
        }


        List<String> allLines = Arrays.asList(fileContent.split("\n"));


        if (allLines.size() == 0) {
            return item;
        }

        if (empty(item.getTitle())) {
            item.setTitle(allLines.get(0));
        }

        String titleLine = "";
        List<String> propertiesSection = list();
        String rawContent = "";
        int propertiesStartAt = 0;
        if (allLines.size() > 1) {
            if (allLines.get(1).startsWith("----") || allLines.get(1).startsWith("====")) {
                titleLine = allLines.get(0);
                propertiesStartAt = 2;
                item.setTitle(titleLine);
            }
        }


        int propertiesEndAt = propertiesStartAt;
        for (;propertiesEndAt<allLines.size();propertiesEndAt++) {
            String line = allLines.get(propertiesEndAt);
            if (line.trim().equals("---")) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon == -1) {
                break;
            }
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1, line.length()).trim();
            if ("oldUrls".equals(key)) {
                setProperty(item, key, apply(list(StringUtils.split(value, ";")), (aUrl) ->aUrl.trim()));
            } else {
                setProperty(item, key, value);
            }
        }
        if (propertiesEndAt < allLines.size()) {
            rawContent = StringUtils.join(allLines.subList(propertiesEndAt, allLines.size()), "\n").trim();
        }

        Boolean isMarkdown = false;
        if (path.toString().toLowerCase().endsWith(".txt") || path.toString().toLowerCase().endsWith(".md")) {
            isMarkdown = true;
        }
        item.setElements(StElementParser.parseElements(rawContent, isMarkdown));
        List<StElement> items = item.getElements();
        for (StElement ele : items) {
            item.getElementById().put(ele.getId(), ele);
        }

        String itemContent = StElementParser.removeTags(rawContent).trim();
        item.setOriginalContent(itemContent);



        if (isMarkdown) {
            Log.fine("Parse for page {0} {1} {2}", item.getId(), item.getSlug(), item.getTitle());
            String cacheKey = DigestUtils.md5Hex("markdown-to-html" + Literals.GSEP + itemContent);
            String cached = null;
            if (!"test".equals(Settings.instance().getEnv())) {
                cached = PermaCache.get(cacheKey);
            }
            if (cached == null) {
                itemContent = Markdown.instance().process(itemContent);
                PermaCache.set(cacheKey, itemContent);
            } else {
                itemContent = cached;
            }

            item.setContent(itemContent);
        }

        if (empty(item.getId())) {
            item.setId(makeIdFromFilePath(relativePath));
        }

        Log.fine("Loaded text item: id:{0} slug:{1} title:{2} draft:{3}", item.getId(), item.getSlug(), item.getTitle(), item.getDraft());
        return item;
    }


    public T fromHtml(String fileContent, Path fullPath) {

        String relativePath = fullPath.toString().replace(getBucketFolderPath(), "");
        Path path = fullPath;


        List<String> htmlLines = list();
        List<String> tomlLines = list();
        boolean inToml = false;
        for (String line: StringUtils.split(fileContent.trim(), "\n")) {
            if (line.trim().equals("<!--start-toml")) {
                inToml = true;
                continue;
            } else if (line.trim().equals("end-toml-->")) {
                inToml = false;
                continue;
            }
            if (inToml) {
                tomlLines.add(line);
            } else {
                htmlLines.add(line);
            }
        }

        String toml = StringUtils.join(tomlLines, "\n");
        String html = StringUtils.join(htmlLines, "\n");

        T item = null;
        if (!empty(toml)) {
            item = new Toml().read(toml).to(getModelClass());
        } else {
            try {
                item = getModelClass().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            item.setTags(new ArrayList<String>())
                    .setElementById(new HashMap<>())
                    .setElements(new ArrayList<>())
                    .setTitle("")
                    .setDraft(true)
                    .setTemplate("")
                    .setContent("")
                    .setSlug(null)
            ;
        }

        /* Set the content */
        item.setContent(html);
        item.setOriginalContent(fileContent);

        /* Set the slug from the file path, if it does not exist */

        if (item.getSlug() == null) {
            item.setSlug(FilenameUtils.removeExtension(relativePath));
            if (!item.getSlug().startsWith("/")) {
                item.setSlug("/" + item.getSlug());
            }
            if (item.getSlug().endsWith("/index")) {
                item.setSlug(item.getSlug().substring(item.getSlug().length() - 6));
            }
        }

        Log.fine("Loaded text item: id:{0} slug:{1} title:{2} draft:{3}", item.getId(), item.getSlug(), item.getTitle(), item.getDraft());
        return item;
    }


    public String makePathForObject(T obj) {
        String slug = obj.getSlug();
        if (empty(slug)) {
            slug = obj.getId().toString();
        }
        return GeneralUtils.slugify(slug).replace('/', '-') + ".txt";
    }




    protected void setProperty(TextItem item, String key, Object value) {
        if (key.equals("slug")) {
            item.setSlug(value.toString());
        } else if (key.equals("title")) {
            item.setTitle(value.toString());
        } else if (key.equals("publishDate")) {
            for(DateTimeFormatter formatter: localDateFormats) {
                if (item.getSlug().equals("/future-dated")) {
                    Log.info("future");
                }
                try {
                    LocalDateTime dt = LocalDateTime.parse(value.toString(), formatter);
                    ZoneId zoneId = ZoneId.systemDefault();
                    if (Context.getSettings() != null && Context.getSettings().getTimeZoneId() != null) {
                        zoneId = Context.getSettings().getTimeZoneId();
                    }
                    item.setPublishDate(ZonedDateTime.of(dt, zoneId));
                    return;
                } catch (DateTimeParseException e) {

                }
            }
            for(DateTimeFormatter formatter: zonedDateFormats) {
                try {
                    ZonedDateTime dt = ZonedDateTime.parse(value.toString(), formatter);
                    item.setPublishDate(dt);
                    return;
                } catch (DateTimeParseException e) {

                }
            }

        } else if (key.equals("draft")) {
            item.setDraft(value.equals("true"));
        } else if (key.equals("template")) {
            item.setTemplate(value.toString());
        } else if (key.equals("author")) {
            item.setAuthor(value.toString());
        } else if (key.equals("tags")) {
            if (value instanceof List) {
                item.setTags((List<String>) value);
            } else {
                ArrayList<String> tags = new ArrayList<String>();
                for (String tag : value.toString().split("(;|,)")) {
                    tags.add(tag.trim());
                }
                item.setTags(tags);

            }
        } else if (key.equals("contentType")) {
            item.setContentType(value.toString());
        } else {
            item.put(key, value);
        }

    }


    public String stringify(TextItem obj)  {

        StringBuffer buffer = new StringBuffer();

        if (obj.getTitle() != null) {
            buffer.append(String.format("title: %s\n", obj.getTitle()));
        }
        if (obj.getSlug() != null) {
            buffer.append(String.format("slug: %s\n", obj.getSlug()));
        }
        if (obj.getPublishDate() != null) {
            buffer.append(String.format("publishDate: %s\n", obj.getPublishDate().format(zonedDateFormats[0])));
        }
        if (obj.getDraft() != null) {
            buffer.append(String.format("isDraft: %s\n", obj.getDraft().toString().toLowerCase()));
        }
        if (obj.getTags() != null && obj.getTags().size() > 0) {
            buffer.append(String.format("tags: %s\n", StringUtils.join(obj.getTags(), ",")));
        }
        if (!StringUtils.isEmpty(obj.getAuthor())) {
            buffer.append(String.format("author: %s\n", obj.getAuthor()));
        }

        for(Map.Entry<String, Object> attr: obj.getAttributes().entrySet()) {
            buffer.append(String.format("%s: %s\n", attr.getKey(), attr.getValue()));
        }

        if (obj.getElements() != null) {
            for (StElement element : obj.getElements()) {
                // TODO: write the attributes, write the tag, write anything else
                buffer.append(String.format("\n<st-element id=\"%s\">%s</st-element>\n", element.getId(), element.getRawInnerContent()));
            }
        }

        if (obj.getOriginalContent() != null) {
            buffer.append(String.format("\n%s", obj.getOriginalContent()));
        }

        return buffer.toString();

    }

    public String toHtml(TextItem obj) {
        TomlWriter writer = new TomlWriter();
        String html = obj.getContent();
        // Don't want to mutate the original
        TextItem clone = JSON.parse(JSON.stringify(obj), obj.getClass());
        clone.setContent(null);
        String toml = writer.write(obj);

        html = "<!--start-toml\n" + toml + "\n--end-toml-->\n\n" + html;
        return html;
    }



}
