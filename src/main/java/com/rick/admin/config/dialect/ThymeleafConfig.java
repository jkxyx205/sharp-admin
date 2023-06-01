package com.rick.admin.config.dialect;

import com.rick.meta.dict.service.DictService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * @author Rick.Xu
 * @date 2023/5/29 13:45
 */
@Configuration
public class ThymeleafConfig {

    @Bean
    public DictSelectDialect dictSelectDialect(DictService dictService) {
        return new DictSelectDialect(dictService);
    }

    @Bean
    public TemplateEngine htmlTemplateEngine(){
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setEnableSpringELCompiler(true);
        StringTemplateResolver templateResolver = new StringTemplateResolver();

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
