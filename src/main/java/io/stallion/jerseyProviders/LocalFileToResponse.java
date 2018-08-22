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

package io.stallion.jerseyProviders;

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
            return sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Response sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath) {
        Response.ResponseBuilder builder = Response.status(200);
        try {
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
            if (modifyTime > 0) {
                builder.header("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(
                        ZonedDateTime.ofInstant(Instant.ofEpochMilli(modifyTime), UTC)
                ));
            }

            // Set the Content-type
            String contentType = GeneralUtils.guessMimeType(fullPath);
            if (empty(contentType)) {
                contentType = Files.probeContentType(FileSystems.getDefault().getPath(fullPath));
            }
            builder.type(contentType);

            // TODO implement a max download size/ file size
            builder.header("Content-length", contentLength);


            Integer BUFF_SIZE = 8192;
            byte[] buffer = new byte[BUFF_SIZE];

            StreamingOutput streamingOutput = new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    long totalBytesRead = 0;
                    // TODO: implement a max file size
                    try {
                        while (true) {
                            int byteRead = stream.read(buffer);
                            if (byteRead == -1) {
                                output.flush();
                                stream.close();
                                break;
                            }

                            output.write(buffer, 0, byteRead);

                            totalBytesRead += (byteRead + 1);
                            if (totalBytesRead > contentLength) {
                                Log.warn("Read more bytes than expected, breaking out of loop");
                                output.flush();
                                stream.close();
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
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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

}
