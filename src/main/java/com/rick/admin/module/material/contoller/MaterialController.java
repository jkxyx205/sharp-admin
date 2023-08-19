package com.rick.admin.module.material.contoller;

import com.rick.admin.module.core.entity.Classification;
import com.rick.admin.module.material.dao.ClassificationDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/8/19 01:34
 */
@RestController
@RequestMapping("materials")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MaterialController {

    ClassificationDAO materialClassificationDAO;

    @GetMapping("{id}/classifications")
    public List<Classification> listClassifications(@PathVariable Long id) {
        return materialClassificationDAO.findClassificationByMaterialId(id);
    }

    @GetMapping("classifications")
    public Map<Long, List<Classification>> listClassifications(@RequestParam String materialIds) {
        Set<Long> materialIdSet = Arrays.asList(materialIds.split(",")).stream().map(r -> Long.parseLong(r)).collect(Collectors.toSet());
        return materialClassificationDAO.findClassificationByMaterialIds(materialIdSet);
    }
}
