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

import io.stallion.requests.IRequest;
import io.stallion.utils.GeneralUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import static io.stallion.utils.Literals.empty;

/**
 * A collection of methods for sending static assets files as a buffered servlet responses.
 */
public class ServletFileSender {
    private HttpServletResponse response;
    private IRequest request;

    public ServletFileSender(IRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public void sendContentResponse(String content, String fullPath) {
        sendContentResponse(content, 0, fullPath);
    }

    public void sendContentResponse(String content, long modifyTime, String fullPath) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(bytes);
        sendAssetResponse(stream, modifyTime, bytes.length, fullPath);
    }

    public void sendResource(URL url, String path) {
        try {
            int length = url.openConnection().getContentLength();
            sendAssetResponse(url.openStream(), 0, length, url.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendAssetResponse(File file) {
        try {
            sendAssetResponse(new FileInputStream(file), file.lastModified(), file.length(), file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAssetResponse(InputStream stream, long modifyTime, long contentLength, String fullPath) {
        try {
            // Set the caching headers
            Long duration = 60 * 60 * 24 * 365 * 10L; // 10 years
            Long durationMils = duration * 1000;
            response.addHeader("Cache-Control", "max-age=" + duration);
            response.setDateHeader("Expires", System.currentTimeMillis() + durationMils);
            if (modifyTime > 0) {
                response.setDateHeader("Last-Modified", modifyTime);
            }

            // Set the Content-type
            String contentType = GeneralUtils.guessMimeType(fullPath);
            if (empty(contentType)) {
                contentType = Files.probeContentType(FileSystems.getDefault().getPath(fullPath));
            }
            response.setContentType(contentType);


            Integer BUFF_SIZE = 8192;
            response.setBufferSize(8192);

            byte[] buffer = new byte[BUFF_SIZE];
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

    }


}
