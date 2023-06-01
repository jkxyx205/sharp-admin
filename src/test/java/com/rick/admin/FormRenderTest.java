package com.rick.admin;

import com.google.common.collect.Maps;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class FormRenderTest {

    @Autowired
    private FormService formService;

    @Autowired
    @Qualifier("templateEngine")
    private TemplateEngine stringTemplateEngine;

    @Test
    public void testRender() {
        FormBO formBO = formService.getFormBOById(695708313425285120L);

        // language=HTML
        Map<String, Object> params = new HashMap<>();
        params.put("formBO", formBO);
        params.put("model", getDataModel(formBO.getPropertyList()));
        String html = stringTemplateEngine.process("tpl/form", new Context(LocaleContextHolder.getLocale(), params));
        System.out.println(html);
    }

    private Map<String, Object> getDataModel(List<FormBO.Property> propertyList) {
        Map<String, Object> dataModel = Maps.newLinkedHashMapWithExpectedSize(propertyList.size());
        for (FormBO.Property property : propertyList) {
            dataModel.put(property.getName(), property.getValue());
        }
        return dataModel;
    }
}
