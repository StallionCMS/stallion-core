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
import io.stallion.asyncTasks.SimpleAsyncRunner;
import io.stallion.exceptions.ClientException;
import io.stallion.requests.StResponse;
import io.stallion.services.Log;
import io.stallion.settings.Settings;
import io.stallion.utils.DateUtils;
import io.stallion.utils.ProcessHelper;
import io.stallion.utils.GeneralUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.stallion.utils.Literals.empty;


public class HealthTracker {
    private CircularFifoQueue<ExceptionInfo> exceptionQueue = new CircularFifoQueue(100);
    private CircularFifoQueue<MinuteInfo> response500s = new CircularFifoQueue<>(50);
    private CircularFifoQueue<MinuteInfo> response400s = new CircularFifoQueue<>(50);
    private CircularFifoQueue<MinuteInfo> response404s = new CircularFifoQueue<>(50);
    private CircularFifoQueue<MinuteInfo> responseCounts = new CircularFifoQueue<>(50);
    private ScheduledThreadPoolExecutor timedChecker;
    private RollingMetrics metrics = new RollingMetrics();
    private DailyMetrics dailyMetrics = new DailyMetrics();
    private static HealthTracker _instance = new HealthTracker();

    private HealthTracker() {
    }

    public static void start() {
        instance().timedChecker = new ScheduledThreadPoolExecutor(2);
        instance().timedChecker.scheduleAtFixedRate(instance().metrics, 0, 1, TimeUnit.MINUTES);
        instance().timedChecker.scheduleAtFixedRate(instance().dailyMetrics, 0, 24*60, TimeUnit.MINUTES);
    }

    public static HealthTracker instance() {
        if (_instance == null) {
            _instance = new HealthTracker();
        }
        return _instance;
    }

    public static void shutdown() {
        if (_instance != null) {
            if (_instance.timedChecker != null) {
                _instance.timedChecker.shutdown();
            }
            _instance = null;
        }
    }

    public Double getAverageSystemCpuLoad() {
        int periods = 0;
        Double total = 0.0;
        for (Double usage: metrics.getSystemCpuUsage()) {
            total += usage;
            periods++;
        }
        return total / periods;
    }

    public Double getAverageAppCpuLoad() {
        int periods = 0;
        Double total = 0.0;
        for (Double usage: metrics.getAppCpuUsage()) {
            total += usage;
            periods++;
        }
        return total / periods;
    }

    public Double getSwapPages() {
        int periods = 0;
        Double total = 0.0;
        for (Long pages: metrics.getSwapRate()) {
            total += pages;
            periods++;
        }
        return total / periods;
    }

    public ZonedDateTime getSslExpires() {
        return dailyMetrics.getSslExpires();
    }

    public boolean getSslExpiresIn30() {
        return dailyMetrics.isSslExpiresWithin30();
    }

    public HttpHealthInfo getHttpHealthInfo() {
        HttpHealthInfo health = new HttpHealthInfo();
        health.setError400s(lastTenMinutesCount(response400s));
        health.setError500s(lastTenMinutesCount(response500s));
        health.setError404s(lastTenMinutesCount(response404s));
        health.setRequestCount(lastTenMinutesCount(responseCounts));
        return health;
    }

    public void logException(Throwable e) {
        if (e instanceof ClientException) {
            return;
        }
        if (e instanceof InvocationTargetException) {
            if (((InvocationTargetException) e).getTargetException() instanceof ClientException) {
                return;
            }
        }
        ExceptionInfo info = ExceptionInfo.newForException(e);
        exceptionQueue.add(info);
        if (SimpleAsyncRunner.instance() != null && Settings.instance().getEmailErrors() == true) {
            SimpleAsyncRunner.instance().submit(new ExceptionEmailRunnable(info));
        }
    }

    public int lastTenMinutesCount(CircularFifoQueue<MinuteInfo> queue) {
        ZonedDateTime tenAgo = MinuteInfo.getCurrentMinute().minusMinutes(10);
        int count = 0;
        for (MinuteInfo info: queue) {
            if (info.getMinute().isBefore(tenAgo)) {
                continue;
            }
            count += info.getCount().get();
        }
        return count;
    }

    public void logResponse(StResponse response) {
        incrementQueue(responseCounts);
        if (response.getStatus() >= 500) {
            incrementQueue(response500s);
        } else if (response.getStatus() == 404) {
            incrementQueue(response404s);
        } else if (response.getStatus() >= 400) {
            incrementQueue(response400s);
        }
    }


    public void incrementQueue(CircularFifoQueue<MinuteInfo> queue) {
        ZonedDateTime now = MinuteInfo.getCurrentMinute();
        MinuteInfo minuteInfo = null;
        if (!queue.isEmpty()) {
            minuteInfo = queue.get(queue.size()-1);
            //minuteInfo = queue.get(0);
            //Log.info("first: {0}", queue.get(0).getMinute());
            //Log.info("last:  {0}", queue.get(queue.size() -1).getMinute());
            //Log.info("now:   {0}", now);
            if (!minuteInfo.getMinute().equals(now)) {
                //Log.info("Minutes do not matched, prepare for new minute");
                minuteInfo = null;
            }
        }
        if (minuteInfo == null) {
            minuteInfo = new MinuteInfo();
            minuteInfo.setMinute(now);
            queue.add(minuteInfo);
        }
        //Log.info("Increment minute {0} {1}", minuteInfo.getMinute().toString(), minuteInfo.getCount().get());
        minuteInfo.getCount().incrementAndGet();
    }




    public CircularFifoQueue<ExceptionInfo> getExceptionQueue() {
        return exceptionQueue;
    }

    public static class MinuteInfo {
        private ZonedDateTime minute;
        private AtomicInteger count = new AtomicInteger(0);
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HH:mm");

        public static ZonedDateTime getCurrentMinute() {
            ZonedDateTime now = DateUtils.utcNow();
            return ZonedDateTime.of(
                    now.getYear(), now.getMonth().getValue(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), 0, 0, ZoneId.of("UTC"));
        }

        public AtomicInteger getCount() {
            return count;
        }

        public void setCount(AtomicInteger count) {
            this.count = count;
        }

        public ZonedDateTime getMinute() {
            return minute;
        }

        public void setMinute(ZonedDateTime minute) {
            this.minute = minute;
        }
    }

    public static class DailyMetrics implements Runnable {
        private double ntpOffset = 0;
        private ZonedDateTime sslExpires = null;
        private boolean sslExpiresWithin30 = false;


        public void run() {
            try {
                if (Settings.instance().getSiteUrl().startsWith("https")) {
                    checkSslExpiration();
                }
            } catch(Exception e) {
                Log.exception(e, "Error checking SSL");
            }

        }

        public void checkSslExpiration() throws Exception {
            // configure the SSLContext with a TrustManager
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);
            URL url = new URL(Settings.instance().getSiteUrl());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
            System.out.println(conn.getResponseCode());
            Certificate[] certs = conn.getServerCertificates();
            Date maxDate = new Date(Long.MAX_VALUE);
            for (Certificate cert :certs){
                X509Certificate xCert = (X509Certificate)cert;
                if (xCert.getNotAfter().before(maxDate)) {
                    maxDate = xCert.getNotAfter();
                }
            }

            setSslExpires(ZonedDateTime.ofInstant(maxDate.toInstant(), GeneralUtils.UTC));
            setSslExpiresWithin30(DateUtils.utcNow().plusMonths(1).isAfter(getSslExpires()));
            conn.disconnect();
        }


        public double getNtpOffset() {
            return ntpOffset;
        }

        public void setNtpOffset(double ntpOffset) {
            this.ntpOffset = ntpOffset;
        }

        public ZonedDateTime getSslExpires() {
            return sslExpires;
        }

        public void setSslExpires(ZonedDateTime sslExpires) {
            this.sslExpires = sslExpires;
        }

        public boolean isSslExpiresWithin30() {
            return sslExpiresWithin30;
        }

        public void setSslExpiresWithin30(boolean sslExpiresWithin30) {
            this.sslExpiresWithin30 = sslExpiresWithin30;
        }
    }


    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static class RollingMetrics implements Runnable {
        private CircularFifoQueue<Double> systemCpuUsage = new CircularFifoQueue<>(5);
        private CircularFifoQueue<Double> appCpuUsage = new CircularFifoQueue<>(5);
        private CircularFifoQueue<Long> swapRate = new CircularFifoQueue<>(5);
        private boolean vmstatAvailable = true;

        public void run() {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            if(os instanceof UnixOperatingSystemMXBean){
                UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean) os;
                getAppCpuUsage().add(unixBean.getProcessCpuLoad());
                getSystemCpuUsage().add(unixBean.getSystemLoadAverage());
            }
            if (Settings.instance().getEnv().equals("local") || Settings.instance().getDevMode()) {
                vmstatAvailable = false;
            }
            if (vmstatAvailable) {
                Runtime rt = Runtime.getRuntime();
                int exitVal = 1;
                Process proc = null;
                try {
                    proc = rt.exec("vmstat");
                    exitVal = proc.exitValue();
                } catch (IOException e) {
                }
                if (exitVal != 0) {
                    // Don't check for vmstat again
                    Log.warn("vmstat executable not found, skipping monitoring of swap rate");
                    vmstatAvailable = false;
                } else {
                    ProcessHelper.CommandResult result = ProcessHelper.run("vmstat", "4", "2");
                    if (result.succeeded()) {
                        String[] lines = result.getOut().split("\n");
                        String lastLine = lines[lines.length - 1];
                        if (empty(lastLine.trim())) {
                            lastLine = lines[lines.length - 2];
                        }
                        String[] stats = lastLine.trim().trim().replaceAll("\\s+", "\t").split("\t");
                        Long swapIn = Long.parseLong(stats[6]);
                        Long swapOut = Long.parseLong(stats[7]);
                        getSwapRate().add(swapIn + swapOut);
                    }
                }
            }


        }


        public CircularFifoQueue<Double> getSystemCpuUsage() {
            return systemCpuUsage;
        }

        public void setSystemCpuUsage(CircularFifoQueue<Double> systemCpuUsage) {
            this.systemCpuUsage = systemCpuUsage;
        }

        public CircularFifoQueue<Double> getAppCpuUsage() {
            return appCpuUsage;
        }

        public void setAppCpuUsage(CircularFifoQueue<Double> appCpuUsage) {
            this.appCpuUsage = appCpuUsage;
        }

        public CircularFifoQueue<Long> getSwapRate() {
            return swapRate;
        }

        public void setSwapRate(CircularFifoQueue<Long> swapRate) {
            this.swapRate = swapRate;
        }
    }
}
