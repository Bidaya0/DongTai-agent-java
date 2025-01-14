package io.dongtai.iast.core.utils;

import io.dongtai.iast.common.constants.PropertyConstant;
import io.dongtai.iast.core.utils.json.GsonUtils;
import io.dongtai.log.DongTaiLog;

import java.io.*;
import java.util.Properties;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PropertyUtils {

    private static PropertyUtils instance;
    public Properties cfg = null;
    private String iastName;
    private String iastServerToken;
    private String dumpClassState;
    private String iastDumpPath;
    private Long heartBeatInterval = -1L;
    private String serverUrl;
    private String serverMode;
    private String proxyEnableStatus;
    private String proxyHost;
    private int proxyPort = -1;
    private String debugFlag;
    private Integer responseLength;
    private String policyPath;

    private final String propertiesFilePath;

    public static PropertyUtils getInstance(String propertiesFilePath) {
        if (null == instance) {
            instance = new PropertyUtils(propertiesFilePath);
        }
        return instance;
    }

    public static PropertyUtils getInstance() {
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    private PropertyUtils(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
        init();
    }

    /**
     * 根据配置文件初始化单例配置类
     */
    private void init() {
        try {
            File propertiesFile = new File(propertiesFilePath);
            if (propertiesFile.exists()) {
                cfg = new Properties();
                FileInputStream fis = new FileInputStream(propertiesFile);
                cfg.load(fis);
                fis.close();
            }
        } catch (IOException e) {
            DongTaiLog.error("PropertyUtils initialization failed", e);
        }
    }

    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir.dongtai");
    }

    public String getIastName() {
        if (null == iastName) {
            iastName = cfg.getProperty("iast.name");
        }
        return iastName;
    }

    /**
     * get OpenAPI Service Address
     *
     * @return OpenAPI Service Address
     */
    public String getBaseUrl() {
        if (null == serverUrl) {
            serverUrl = System.getProperty(PropertyConstant.PROPERTY_SERVER_URL, cfg.getProperty("iast.server.url"));
        }
        return serverUrl;
    }

    public String getServerToken() {
        if (null == iastServerToken) {
            iastServerToken = System.getProperty(PropertyConstant.PROPERTY_SERVER_TOKEN,
                    cfg.getProperty("iast.server.token"));
        }
        return iastServerToken;
    }

    @Override
    public String toString() {
        return "[IastName=" + getIastName() +
                "，IastServerUrl=" + getBaseUrl() +
                "，IastServerToken=" + getServerToken() +
                "]";
    }

    public String getBlackFunctionFilePath() {
        return "com.secnium.iast.resources/blacklistfunc.txt";
    }

    public String getBlackClassFilePath() {
        return "com.secnium.iast.resources/blacklist.txt";
    }

    public String getBlackUrl() {
        return "com.secnium.iast.resources/blackurl.txt";
    }

    public String getBlackExtFilePath() {
        return "com.secnium.iast.resources/blackext.txt";
    }

    public String getDumpClassPath() {
        if (null == iastDumpPath) {
            iastDumpPath = System.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_PATH,
                    cfg.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_PATH));
        }
        return iastDumpPath;
    }

    private String getDumpClassState() {
        if (null == dumpClassState) {
            dumpClassState = System.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_ENABLE,
                    cfg.getProperty(PropertyConstant.PROPERTY_DUMP_CLASS_ENABLE));
        }
        return dumpClassState;
    }

    public boolean isEnableDumpClass() {
        return "true".equalsIgnoreCase(getDumpClassState());
    }

    public long getHeartBeatInterval() {
        if (heartBeatInterval == -1L) {
            heartBeatInterval = Long.valueOf(System.getProperty(PropertyConstant.PROPERTY_SERVICE_HEARTBEAT_INTERVAL,
                    cfg.getProperty(PropertyConstant.PROPERTY_SERVICE_HEARTBEAT_INTERVAL, "60")));
        }
        return heartBeatInterval;
    }

    /**
     * After version 1.2.0, change the default server mode to local.
     *
     * @return server mode
     */
    @Deprecated
    private String getServerMode() {
        if (null == serverMode) {
            serverMode = System.getProperty("iast.server.mode", cfg.getProperty("iast.server.mode", "local"));
        }
        return serverMode;
    }

    @Deprecated
    public boolean isLocal() {
        return "local".equals(getServerMode());
    }

    private String getProxyEnableStatus() {
        if (null == proxyEnableStatus) {
            proxyEnableStatus = System.getProperty(PropertyConstant.PROPERTY_PROXY_ENABLE,
                    cfg.getProperty(PropertyConstant.PROPERTY_PROXY_ENABLE, "false"));
        }
        return proxyEnableStatus;
    }

    public boolean isProxyEnable() {
        return "true".equalsIgnoreCase(getProxyEnableStatus());
    }

    public String getProxyHost() {
        if (null == proxyHost) {
            proxyHost = System.getProperty(PropertyConstant.PROPERTY_PROXY_HOST,
                    cfg.getProperty(PropertyConstant.PROPERTY_PROXY_HOST, ""));
        }
        return proxyHost;
    }

    public int getProxyPort() {
        if (-1 == proxyPort) {
            proxyPort = Integer
                    .parseInt(System.getProperty(PropertyConstant.PROPERTY_PROXY_PORT,
                            cfg.getProperty(PropertyConstant.PROPERTY_PROXY_PORT, "80")));
        }
        return proxyPort;
    }

    private String getDebugFlag() {
        if (debugFlag == null) {
            debugFlag = System.getProperty(PropertyConstant.PROPERTY_DEBUG, "false");
        }
        return debugFlag;
    }

    public Integer getResponseLength() {
        if (responseLength == null) {
            responseLength = Integer.parseInt(System.getProperty(PropertyConstant.PROPERTY_RESPONSE_LENGTH,
                    cfg.getProperty(PropertyConstant.PROPERTY_RESPONSE_LENGTH, "-1")));
        }
        return responseLength;
    }

    public String getPolicyPath() {
        if (null == this.policyPath) {
            this.policyPath = System.getProperty(PropertyConstant.PROPERTY_POLICY_PATH,
                    cfg.getProperty(PropertyConstant.PROPERTY_POLICY_PATH, ""));
        }
        return this.policyPath;
    }

    /**
     * 获取远端同步的本地配置项
     *
     * @param configKey    配置项
     * @param valueType    值类型
     * @param defaultValue 默认值
     * @param cfg          本地properties配置(为空使用PropertyUtils的配置)
     * @return {@link T} 值类型泛型
     */
    public static <T> T getRemoteSyncLocalConfig(String configKey, Class<T> valueType, T defaultValue, Properties cfg) {
        if (configKey == null || valueType == null) {
            return defaultValue;
        }
        final String config = String.format("iast.remoteSync.%s", configKey);
        final Properties localConfig = cfg != null ? cfg : PropertyUtils.getInstance().cfg;
        final String property = System.getProperty(config, localConfig == null ? null : localConfig.getProperty(config, null));
        if (property == null) {
            return defaultValue;
        }
        try {
            if (valueType.isInstance(valueType)) {
                return valueType.cast(property);
            } else {
                return GsonUtils.castBaseTypeString2Obj(property, valueType);
            }
        } catch (Exception e) {
            DongTaiLog.warn("cast remoteSyncConfig failed!key:{}, valueType:{}, property:{}, err:{}", config, valueType, property, e.getMessage());
            return defaultValue;
        }
    }

    public static <T> T getRemoteSyncLocalConfig(String configKey, Class<T> valueType, T defaultValue) {
        return getRemoteSyncLocalConfig(configKey, valueType, defaultValue, null);
    }

}
