package com.sanyou.configcenter.spring;

import com.sanyou.configcenter.client.ConfigService;
import com.sanyou.configcenter.client.pojo.ConfigFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 核心接口
 * PropertySourceLocator就是起到在SpringCloud环境下从配置中心获取配置的作用。
 * PropertySourceLocator是一个接口，所以只要不同的配置中心实现这个接口，
 * 那么不同的配置中心就可以整合到了SpringCloud，从而实现从配置中心加载配置属性到Spring环境中了。
 * 参考：https://mp.weixin.qq.com/s/840rQ8GapAcQYZAcJ13sfQ
 * <p>
 * 微信公众号：三友的java日记
 *
 * @author sanyou
 * @date 2022/9/30 00:35
 */
@Component
@Slf4j
public class ConfigCenterPropertySourceLocator implements PropertySourceLocator {
    /**
     * 获取所有的配置文件加载器，用于解析我们的配置文件！！！
     * <p>
     * SpringBoot 的配置文件内置支持 properties、xml、yml、yaml 几种格式，
     * 其中 properties和xml 对应的Loader类为 PropertiesPropertySourceLoader，
     * 其中，yml和yaml对应的Loader类为 YamlPropertySourceLoader。
     * 观察这2个类可以发现，都实现自接口 PropertySourceLoader 。
     * 所以我们要新增支持别的格式的配置文件，就可以通过实现接口 PropertySourceLoader 来实现了。
     */
    private final List<PropertySourceLoader> propertySourceLoaderList =
            SpringFactoriesLoader.loadFactories(PropertySourceLoader.class,
                    Thread.currentThread().getContextClassLoader());

    @Resource
    private ConfigService configService;

    @Resource
    private ConfigCenterProperties configCenterProperties;

    /**
     * 从配置中心加载最新配置
     * 项目启动的时候SpringCloud是如何从配置中心加载数据的，
     * 主要是通过新建一个容器，加载bootstrap配置文件和一些配置类，
     * 最后会调用PropertySourceLocator中的该方法来从配置中心获取到最新的配置信息。
     *
     * @param environment
     * @return
     */
    @Override
    public PropertySource<?> locate(Environment environment) {
        /**
         * 用于存储配置文件中的值，这个name即为配置文件的名称，比如实际项目中的主配置类就是properties，或其他自定义名称！
         */
        CompositePropertySource composite = new CompositePropertySource("tkzou-config");

        try {
            //解析并获取配置信息
            loadConfig(composite, configCenterProperties.getFileId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return composite;
    }

    /**
     * 根据所配置的文件的id，从配置中心拉取配置信息，然后解析配置。
     *
     * @param composite
     * @param fileId
     * @throws IOException
     */
    private void loadConfig(CompositePropertySource composite, String fileId) throws IOException {
        //配置文件信息
        ConfigFile configFile = configService.getConfig(fileId);

        if (configFile == null) {
            throw new RuntimeException(fileId + "未找到对应的配置文件");
        }

        for (PropertySourceLoader propertySourceLoader : propertySourceLoaderList) {
            //使用能解析当前配置文件的解析器解析配置文件
            //具体就是使用文件后缀匹配！
            if (Arrays.asList(propertySourceLoader.getFileExtensions()).contains(configFile.getExtension())) {
                //获取配置文件中的属性值，返回的是个list，我们取第一个
                List<PropertySource<?>> propertySourceList = propertySourceLoader.load(configFile.getFileId(),
                        new ByteArrayResource(configFile.getContent().getBytes(StandardCharsets.ISO_8859_1)));
                for (PropertySource<?> propertySource : propertySourceList) {
                    //添加该属性至spring-cloud
                    composite.addFirstPropertySource(propertySource);
                }
            }
        }
    }

}
