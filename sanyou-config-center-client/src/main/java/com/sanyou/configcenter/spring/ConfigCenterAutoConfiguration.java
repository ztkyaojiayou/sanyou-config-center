package com.sanyou.configcenter.spring;

import com.sanyou.configcenter.client.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动装配，即把需要的bean统一装配到ioc容器
 * 微信公众号：三友的java日记
 *
 * @author sanyou
 * @date 2022/9/30 00:38
 */
@Configuration
public class ConfigCenterAutoConfiguration {

    /**
     * 当前配置中心的配置文件，即对应的web端地址
     * @return
     */
    @Bean
    public ConfigCenterProperties configCenterProperties() {
        return new ConfigCenterProperties();
    }

    /**
     * 配置服务核心类
     * 注意功能：实例化configClient，刷新最新配置到本地缓存
     * @param configCenterProperties
     * @return
     */
    @Bean
    public ConfigService configService(ConfigCenterProperties configCenterProperties) {
        //这个需要一个web端的ip地址，目的就是获取与web端交互的ConfigClient对象，如获取配置文件的最新数据
        return new ConfigService(configCenterProperties.getServerAddr());
    }

    /**
     * 配置刷新器
     * @return
     */
    @Bean
    public ConfigContextRefresher configContextRefresher() {
        return new ConfigContextRefresher();
    }

}
