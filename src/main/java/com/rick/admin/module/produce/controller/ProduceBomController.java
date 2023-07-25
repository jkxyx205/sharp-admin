package com.rick.admin.module.produce.controller;

import com.rick.admin.module.produce.entity.Bom;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.service.BomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Rick.Xu
 * @date 2023/7/7 16:32
 */
@Controller
@RequestMapping("produce")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProduceBomController {

    final BomService bomService;

    @GetMapping("bom")
    public String gotoBomForm() {
        return "modules/bom_template";
    }

//    @GetMapping("bom/materials/{id}")
//    @ResponseBody
//    public Bom gotoBomForm(@PathVariable Long id) {
//        return bomService.findByMaterialId(id);
//    }

    @GetMapping("bom/materials/{id}")
    @ResponseBody
    public BomTemplate gotoBomForm(@PathVariable Long id) {
        return bomService.getBomTemplateMaterialId(id);
    }

//    @PostMapping("bom")
//    @ResponseBody
//    public Bom saveBom(@RequestBody Bom bom) {
//        return bomService.saveOrUpdate(bom);
//    }

    @PostMapping("bom")
    @ResponseBody
    public BomTemplate saveBom(@RequestBody Bom bom) {
        bomService.saveOrUpdate(bom);
        return gotoBomForm(bom.getMaterialId());
    }
}
