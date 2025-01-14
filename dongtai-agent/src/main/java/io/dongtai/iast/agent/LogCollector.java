package io.dongtai.iast.agent;

import io.dongtai.iast.agent.report.AgentRegisterReport;
import io.dongtai.iast.agent.util.FileUtils;
import io.dongtai.log.DongTaiLog;

import java.io.File;
import java.io.IOException;

import static io.dongtai.iast.agent.Agent.*;

public class LogCollector {
    private static String FLUENT_FILE;
    private static String FLUENT_FILE_CONF;
    private static Process fluent;
    private static Thread shutdownHook;

    public static void extractFluent() {
        if (IastProperties.getInstance().getLogDisableCollector() || DongTaiLog.getLogPath().isEmpty()) {
            return;
        }
        try {
            if (!isMacOs() && !isWindows()) {
                FLUENT_FILE = IastProperties.getInstance().getTmpDir() + "fluent";
                FileUtils.getResourceToFile("bin/fluent", FLUENT_FILE);

                String agentId = String.valueOf(AgentRegisterReport.getAgentFlag());
                FLUENT_FILE_CONF = IastProperties.getInstance().getTmpDir() + "fluent-" + agentId + ".conf";
                FileUtils.getResourceToFile("bin/fluent.conf", FLUENT_FILE_CONF);
                FileUtils.confReplace(FLUENT_FILE_CONF);
                if (!(new File(FLUENT_FILE)).setExecutable(true)) {
                    DongTaiLog.info("fluent setExecutable failure. please set execute permission, file: {}", FLUENT_FILE);
                }
                doFluent();
            }
        } catch (IOException e) {
            DongTaiLog.error("fluent extract failure", e);
        }
    }

    public static void doFluent() {
        String[] execution = {
                "nohup",
                FLUENT_FILE,
                "-c",
                FLUENT_FILE_CONF
        };
        try {
            fluent = Runtime.getRuntime().exec(execution);
            DongTaiLog.info("fluent process started");
            shutdownHook = new Thread(new Runnable() {
                public void run() {
                    stopFluent();
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IOException e) {
            DongTaiLog.error("fluent process start failed", e);
        }
    }

    public static void stopFluent() {
        if (fluent == null) {
            return;
        }
        try {
            fluent.destroy();
            if (shutdownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            DongTaiLog.info("fluent process stopped");
        } catch (Exception ignored) {
        } finally {
            fluent = null;
            shutdownHook = null;
        }
    }
}
