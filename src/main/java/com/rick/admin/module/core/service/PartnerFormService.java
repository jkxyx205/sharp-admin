package com.rick.admin.module.core.service;

import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.purchase.entity.MaterialSource;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.Form;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/1 14:15
 */
@Component("partnerFormService")
@RequiredArgsConstructor
public class PartnerFormService implements FormAdvice {

    private final DictService dictService;

    private final MaterialService materialService;

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        dictService.rebuild("core_partner");
        dictService.rebuild("core_partner_vendor");
    }

    @Override
    public void afterGetInstance(Form form, Long instanceId, List<FormBO.Property> propertyList, Map<String, Object> valueMap) {
        List<MaterialSource> sourceList = (List<MaterialSource>) valueMap.get("sourceList");
        propertyList.add(new FormBO.Property(null, "sourceList", new CpnConfigurer(), sourceList));
        materialService.fillMaterialDescription(sourceList);
    }
}
