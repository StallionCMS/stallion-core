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

package io.stallion.monitoring;

import com.sun.management.UnixOperatingSystemMXBean;
import io.stallion.services.Log;
import io.stallion.settings.Settings;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.ZonedDateTime;


public class SystemHealth {
    private long jvmMemoryUsage = 0;
    private long jvmMemoryUsageMb = 0;
    private long diskFreeDataDirectory;
    private long diskFreeDataDirectoryMb;
    private long diskFreeAppDirectory;
    private long diskFreeLogDirectory;
    private long fileHandlesOpen = 0;
    private long fileHandlesMax = 4000;
    private long fileHandlesAvailable = 4000;
    private Double memoryPercentFree = 0.0;
    private long memorySwapSize = 0L;
    private long memorySwapFree = 0;
    private long memoryPhysicalSize = 0;
    private long memoryPhysicalFree = 0;
    private Double swapPagingRate = 0.0;
    private Double cpuAppUsage = 0.0;
    private Double cpuSystemUsage = 0.0;
    private Double cpuRollingAppUsage = 0.0;
    private Double cpuRollingSystemUsage = 0.0;
    private long cpusAvailable = 0;
    private boolean sslExpiresWithinMonth = false;
    private ZonedDateTime sslExpiresDate = null;



    public SystemHealth hydrateSystemHealth() {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long memUsage = (rt.totalMemory() - rt.freeMemory());
        long usedMB = memUsage / 1024 / 1024;
        setJvmMemoryUsage(memUsage);
        setJvmMemoryUsageMb(usedMB);
        setDiskFreeAppDirectory(new File(Settings.instance().getTargetFolder()).getUsableSpace());
        setDiskFreeDataDirectory(new File(Settings.instance().getDataDirectory()).getUsableSpace());
        setDiskFreeDataDirectoryMb(new File(Settings.instance().getDataDirectory()).getUsableSpace() / 1024 / 1024);
        File logFile = new File(Settings.instance().getLogFile());
        if (logFile.getParentFile().exists()) {
            setDiskFreeLogDirectory(logFile.getParentFile().getUsableSpace());
        }
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if(os instanceof UnixOperatingSystemMXBean){
            UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean) os;
            // get system load
            // get process CPU load
            // Get free memory
            memorySwapSize = unixBean.getTotalSwapSpaceSize();
            memorySwapFree = unixBean.getFreeSwapSpaceSize();
            memoryPhysicalSize = unixBean.getTotalPhysicalMemorySize();
            setMemoryPhysicalFree(unixBean.getFreePhysicalMemorySize());
            long totalRamFree = unixBean.getFreeSwapSpaceSize() + unixBean.getFreePhysicalMemorySize();
            long totalRam =  unixBean.getTotalPhysicalMemorySize() + unixBean.getTotalSwapSpaceSize();
            setMemoryPercentFree(new Double(totalRamFree) / new Double(totalRam));
            setFileHandlesOpen(unixBean.getOpenFileDescriptorCount());
            setFileHandlesMax(unixBean.getMaxFileDescriptorCount());
            setFileHandlesAvailable(unixBean.getMaxFileDescriptorCount() - unixBean.getOpenFileDescriptorCount());
            setCpusAvailable(unixBean.getAvailableProcessors());

            setCpuAppUsage(unixBean.getProcessCpuLoad());
            setCpuSystemUsage(unixBean.getSystemLoadAverage());
        }
        setCpuRollingAppUsage(HealthTracker.instance().getAverageAppCpuLoad());
        setCpuRollingSystemUsage(HealthTracker.instance().getAverageSystemCpuLoad());
        setSwapPagingRate(HealthTracker.instance().getSwapPages());
        setSslExpiresDate(HealthTracker.instance().getSslExpires());
        setSslExpiresWithinMonth(HealthTracker.instance().getSslExpiresIn30());
        return this;
    }

    public long getJvmMemoryUsage() {
        return jvmMemoryUsage;
    }

    public void setJvmMemoryUsage(long jvmMemoryUsage) {
        this.jvmMemoryUsage = jvmMemoryUsage;
    }

    public long getDiskFreeDataDirectory() {
        return diskFreeDataDirectory;
    }

    public void setDiskFreeDataDirectory(long diskFreeDataDirectory) {
        this.diskFreeDataDirectory = diskFreeDataDirectory;
    }

    public long getDiskFreeAppDirectory() {
        return diskFreeAppDirectory;
    }

    public void setDiskFreeAppDirectory(long diskFreeAppDirectory) {
        this.diskFreeAppDirectory = diskFreeAppDirectory;
    }

    public long getDiskFreeLogDirectory() {
        return diskFreeLogDirectory;
    }

    public void setDiskFreeLogDirectory(long diskFreeLogDirectory) {
        this.diskFreeLogDirectory = diskFreeLogDirectory;
    }

    public long getJvmMemoryUsageMb() {
        return jvmMemoryUsageMb;
    }

    public void setJvmMemoryUsageMb(long jvmMemoryUsageMb) {
        this.jvmMemoryUsageMb = jvmMemoryUsageMb;
    }

    public long getDiskFreeDataDirectoryMb() {
        return diskFreeDataDirectoryMb;
    }

    public void setDiskFreeDataDirectoryMb(long diskFreeDataDirectoryMb) {
        this.diskFreeDataDirectoryMb = diskFreeDataDirectoryMb;
    }

    public long getFileHandlesOpen() {
        return fileHandlesOpen;
    }

    public void setFileHandlesOpen(long fileHandlesOpen) {
        this.fileHandlesOpen = fileHandlesOpen;
    }

    public Double getMemoryPercentFree() {
        return memoryPercentFree;
    }

    public void setMemoryPercentFree(Double memoryPercentFree) {
        this.memoryPercentFree = memoryPercentFree;
    }

    public Double getSwapPagingRate() {
        return swapPagingRate;
    }

    public void setSwapPagingRate(Double swapPagingRate) {
        this.swapPagingRate = swapPagingRate;
    }

    public Double getCpuAppUsage() {
        return cpuAppUsage;
    }

    public void setCpuAppUsage(Double cpuAppUsage) {
        this.cpuAppUsage = cpuAppUsage;
    }

    public Double getCpuSystemUsage() {
        return cpuSystemUsage;
    }

    public void setCpuSystemUsage(Double cpuSystemUsage) {
        this.cpuSystemUsage = cpuSystemUsage;
    }

    public long getFileHandlesMax() {
        return fileHandlesMax;
    }

    public void setFileHandlesMax(long fileHandlesMax) {
        this.fileHandlesMax = fileHandlesMax;
    }

    public long getFileHandlesAvailable() {
        return fileHandlesAvailable;
    }

    public void setFileHandlesAvailable(long fileHandlesAvailable) {
        this.fileHandlesAvailable = fileHandlesAvailable;
    }

    public long getCpusAvailable() {
        return cpusAvailable;
    }

    public void setCpusAvailable(long cpusAvailable) {
        this.cpusAvailable = cpusAvailable;
    }

    public Double getCpuRollingAppUsage() {
        return cpuRollingAppUsage;
    }

    public void setCpuRollingAppUsage(Double cpuRollingAppUsage) {
        this.cpuRollingAppUsage = cpuRollingAppUsage;
    }

    public Double getCpuRollingSystemUsage() {
        return cpuRollingSystemUsage;
    }

    public void setCpuRollingSystemUsage(Double cpuRollingSystemUsage) {
        this.cpuRollingSystemUsage = cpuRollingSystemUsage;
    }

    public long getMemorySwapSize() {
        return memorySwapSize;
    }

    public void setMemorySwapSize(long memorySwapSize) {
        this.memorySwapSize = memorySwapSize;
    }

    public long getMemorySwapFree() {
        return memorySwapFree;
    }

    public void setMemorySwapFree(long memorySwapFree) {
        this.memorySwapFree = memorySwapFree;
    }

    public long getMemoryPhysicalSize() {
        return memoryPhysicalSize;
    }

    public void setMemoryPhysicalSize(long memoryPhysicalSize) {
        this.memoryPhysicalSize = memoryPhysicalSize;
    }

    public long getMemoryPhysicalFree() {
        return memoryPhysicalFree;
    }

    public void setMemoryPhysicalFree(long memoryPhysicalFree) {
        this.memoryPhysicalFree = memoryPhysicalFree;
    }

    public boolean isSslExpiresWithinMonth() {
        return sslExpiresWithinMonth;
    }

    public void setSslExpiresWithinMonth(boolean sslExpiresWithinMonth) {
        this.sslExpiresWithinMonth = sslExpiresWithinMonth;
    }

    public ZonedDateTime getSslExpiresDate() {
        return sslExpiresDate;
    }

    public void setSslExpiresDate(ZonedDateTime sslExpiresDate) {
        this.sslExpiresDate = sslExpiresDate;
    }

}
