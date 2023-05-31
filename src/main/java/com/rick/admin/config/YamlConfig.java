package com.rick.admin.config;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/13/20 5:07 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Configuration
public class YamlConfig {

    @Bean
    public YamlMapFactoryBean YamlMapFactoryBean() {
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
        yamlMapFactoryBean.setSingleton(false);
        return yamlMapFactoryBean;
    }
}
