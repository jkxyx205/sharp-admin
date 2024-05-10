package com.rick.admin.module.produce.controller;

import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.meta.dict.service.DictService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/12 22:20
 */
@Controller
@RequestMapping("produce_bom")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BomController {

    EntityCodeDAO<BomTemplate, Long> bomTemplateDAO;

    MaterialService materialService;

    DictService dictService;

    @GetMapping("new")
    public String gotoNewPage() {
        return "modules/produce/produce_bom";
    }

    @GetMapping("{id}")
    public String gotoDetailPage(@PathVariable Long id, Model model) {
        BomTemplate bomTemplate = bomTemplateDAO.selectById(id).get();

        Set<BomTemplate.ComponentDetail> detailSet = bomTemplate.getComponentList().stream().flatMap(component -> component.getComponentDetailList().stream())
                .collect(Collectors.toSet());

        materialService.fillMaterialDescription(detailSet);

        model.addAttribute("bomTemplate", bomTemplate);
        return "modules/produce/produce_bom";
    }

    @PostMapping
    @ResponseBody
    public Result saveOrUpdate(@RequestBody BomTemplate bomTemplate) {
        bomTemplateDAO.insertOrUpdate(bomTemplate);
        dictService.rebuild("material_bom");
        return ResultUtils.success(bomTemplate.getId());
    }
}
