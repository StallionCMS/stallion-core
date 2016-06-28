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


public abstract class BaseWatchEventHandler implements IWatchEventHandler {
    private String watchedFolder = "";
    private Boolean watchTree = false;
    private String extension = "";

    public BaseWatchEventHandler() {

    }

    public BaseWatchEventHandler(String watchedFolder) {
        this.watchedFolder = watchedFolder;
    }

    @Override
    public String getWatchedFolder() {
        return watchedFolder;
    }

    @Override
    public BaseWatchEventHandler setWatchedFolder(String watchedFolder) {
        this.watchedFolder = watchedFolder;
        return this;
    }

    @Override
    public Boolean getWatchTree() {
        return watchTree;
    }

    @Override
    public BaseWatchEventHandler setWatchTree(Boolean watchTree) {
        this.watchTree = watchTree;
        return this;
    }

    public String getExtension() {
        return extension;
    }

    public BaseWatchEventHandler setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public String getInternalHandlerLabel() {
        return this.getClass().getSimpleName();
    }

}
