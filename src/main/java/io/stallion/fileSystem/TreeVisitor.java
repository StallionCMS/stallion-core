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

package io.stallion.fileSystem;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for finding a list of all text files in a directory.
 */
public class TreeVisitor implements FileVisitor<Path> {
    private List<Path> paths = new ArrayList<Path>();

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)  {
        return FileVisitResult.CONTINUE;
    }
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (file != null) {
            // ignore emacs autosave files
            if (file.toString().contains(".#")) {
                return FileVisitResult.CONTINUE;
            }
            if (file.toString().toLowerCase().endsWith(".html")
                    || file.toString().toLowerCase().endsWith(".htm")
                    || file.toString().toLowerCase().endsWith(".md")
                    || file.toString().toLowerCase().endsWith(".toml")
                    || file.toString().toLowerCase().endsWith(".json")
                    || file.toString().toLowerCase().endsWith(".txt")) {
                getPaths().add(file);
            }
        }
        return FileVisitResult.CONTINUE;
    }
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }


    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }


    public List<Path> getPaths() {
        return paths;
    }

    public void setPaths(List<Path> paths) {
        this.paths = paths;
    }
}