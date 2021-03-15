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

package io.stallion.http;

import io.stallion.exceptions.UsageException;
import io.stallion.services.Log;
import io.stallion.utils.GeneralUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.stallion.utils.Literals.UTC;
import static io.stallion.utils.Literals.empty;


public class LocalFileToResponse {


    public Response sendContentResponse(String content, String fullPath) {
        return sendContentResponse(content, 0, fullPath);
    }

    public Response sendContentResponse(String content, long modifyTime, String fullPath) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(bytes);
        return sendAssetResponse(stream, modifyTime, bytes.length, fullPath);
    }

    public Response sendResource(URL url, String path) {
        try {
            int length = url.openConnection().getContentLength();
            return sendAssetResponse(url.openStream(), 0, length, url.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Response sendAssetResponse(File file) {
        try {
            String contentDisposition = "inline; filename=\"" + file.getName() + "\"";
            return sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath(), contentDisposition);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Response sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath) {
        return sendAssetResponse(stream, modifyTime, contentLength, fullPath, null);
    }

    public Response sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath, String contentDisposition) {
        FileToResponse info = new FileToResponse(stream, modifyTime, contentLength, fullPath, contentDisposition);
        return sendAssetResponse(info);
    }

    public Response sendAssetResponse(FileToResponse info) {

        Response.ResponseBuilder builder = Response.status(200);

        // Set the caching headers
        Long duration = 60 * 60 * 24 * 365 * 10L; // 10 years
        Long durationMils = duration * 1000;

        builder.header("Cache-Control", "max-age=" + duration);
        builder.header("Expires",
                DateTimeFormatter.RFC_1123_DATE_TIME.format(
                        ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(System.currentTimeMillis() + durationMils),
                                UTC
                        )
                ));
        if (info.getModifyTime() > 0) {
            builder.header("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(info.getModifyTime()), UTC)
            ));
        }

        // Set the Content-type
        if (!empty(info.getContentType())) {
            builder.type(info.getContentType());
        }

        // TODO implement a max download size/ file size
        builder.header("Content-length", info.getContentLength());

        if (!empty(info.getContentDisposition())) {
            builder.header("Content-Disposition", info.getContentDisposition());
        }


        Integer BUFF_SIZE = 8192;
        byte[] buffer = new byte[BUFF_SIZE];

        if (info.getStream() instanceof FileInputStream) {
            FileInputStream fstream = (FileInputStream)info.getStream();
            //fstream
            builder.entity(fstream);
            return builder.build();
        }

        StreamingOutput streamingOutput = new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                long totalBytesRead = 0;
                // TODO: implement a max file size
                try {
                    while (true) {
                        int byteRead = info.getStream().read(buffer);
                        if (byteRead == -1) {
                            output.flush();
                            info.getStream().close();
                            break;
                        }

                        output.write(buffer, 0, byteRead);

                        totalBytesRead += (byteRead + 1);
                        if (totalBytesRead > (info.getContentLength() + 1000)) {
                            Log.warn("Read one thousand more bytes than expected, breaking out of loop. " +
                                    "Content length was {0} for file {1}", info.getContentLength(), info.getFullPath());
                            output.flush();
                            info.getStream().close();
                            break;
                        }

                        // wb.write(output);
                    }
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };

        builder.entity(streamingOutput);

        return builder.build();

            /*

            response.setBufferSize(8192);


            ServletOutputStream os = response.getOutputStream();
            response.setContentLength((int) contentLength);

            try {
                int byteRead = 0;
                int readCount = 0;
                int totalRead = 0;
                while (true) {
                    byteRead = stream.read(buffer);
                    if (byteRead == -1) {
                        break;
                    }
                    //totalRead += byteRead;
                    //Log.info("Bytes sent {0}", totalRead);
                    os.write(buffer, 0, byteRead);
                }
                //os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //os.close();
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */

    }

public static class FileToResponse {
    private InputStream stream;
    private long modifyTime;
    private long contentLength;
    private String fullPath;
    private String contentDisposition;
    private String contentType;
    private File file;

    public FileToResponse(InputStream stream, long modifyTime, long contentLength, String fullPath) {
        this(stream, modifyTime, contentLength, fullPath, null);
    }

    public FileToResponse(InputStream stream, long modifyTime, long contentLength, String fullPath,
                          String contentDisposition) {
        this.stream = stream;
        this.modifyTime = modifyTime;
        this.contentLength = contentLength;
        this.fullPath = fullPath;
        this.contentDisposition = contentDisposition;
        this.hydrate();
    }

    public FileToResponse(File file) {
        this(file, null);
    }

    public FileToResponse(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
        this.hydrate();
    }

    public FileToResponse hydrate() {
        if (stream == null) {
            if (file == null) {
                if (empty(fullPath)) {
                    throw new UsageException("stream, file, and fullPath are empty");
                }
                file = new File(fullPath);
            }
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (file != null) {
            if (empty(contentDisposition)) {
                contentDisposition = "inline; filename=\"" + file.getName() + "\"";
            }
            contentLength = file.length();
            if (modifyTime == 0) {
                modifyTime = file.lastModified();
            }
        }

        if (empty(contentType)) {
            contentType = GeneralUtils.guessMimeType(fullPath);
            if (empty(contentType)) {
                try {
                    contentType = Files.probeContentType(FileSystems.getDefault().getPath(fullPath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this;


    }

    public InputStream getStream() {
        return stream;
    }


    public long getModifyTime() {
        return modifyTime;
    }


    public long getContentLength() {
        return contentLength;
    }


    public String getFullPath() {
        return fullPath;
    }


    public String getContentType() {
        return contentType;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }


    public File getFile() {
        return file;
    }

}


}
