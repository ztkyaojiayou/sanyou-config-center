package com.sanyou.configcenter.client;

import com.sanyou.configcenter.client.pojo.ConfigFile;
import org.springframework.web.client.RestTemplate;

/**
 * 配置中心client，与配置中心server端进行交互
 * 本质上就是最朴素的http请求！！！
 * 微信公众号：三友的java日记
 *
 * @author sanyou
 * @date 2022/9/30 00:16
 */
public class ConfigClient {

    //即http请求调用工具类
    private final RestTemplate restTemplate = new RestTemplate();

    private final String serverAddr;

    public ConfigClient(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    /**
     * 获取指定配置文件的最新数据
     * 本质就是http请求
     * @param fileId
     * @return
     */
    public ConfigFile getConfig(String fileId) {
        return restTemplate.getForObject("http://" + serverAddr + "/v1/config/" + fileId, ConfigFile.class);
    }

}
