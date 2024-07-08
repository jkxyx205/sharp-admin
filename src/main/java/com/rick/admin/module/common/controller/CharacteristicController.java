package com.rick.admin.module.common.controller;

import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.JsonUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Rick.Xu
 * @date 2024/7/8 17:56
 */
@Controller
@RequestMapping("characteristics")
@RequiredArgsConstructor
public class CharacteristicController {

    final SharpService sharpService;

    @GetMapping
    public String characteristicsPage() {
        return "modules/characteristics";
    }

    @GetMapping("{id}")
    @ResponseBody
    public Result characteristics(@PathVariable Long id) {
        Optional<Map<String, Object>> optional = sharpService.queryForObject("select options from sys_form_configurer where id = :id",
                Params.builder(1).pv("id", id).build());
        if (optional.isPresent()) {
            String options = (String) optional.get().get("options");
            return ResultUtils.success(JsonUtils.toList(options, Map.class));
        }
        return ResultUtils.fail();
    }

    @PostMapping("{id}")
    @ResponseBody
    public Result saveCharacteristics(@PathVariable Long id, String options) {
        String[] optionArray = options.split("\n");
        List<Map> list = new ArrayList<>();
        for (String option : optionArray) {
            option = option.trim();
            if (StringUtils.isBlank(option)) {
                continue;
            }

            Map<String, String> map = new HashMap<>();
            map.put("name", option);
            map.put("label", option);
            list.add(map);
        }

        sharpService.update("update sys_form_configurer set options = :options where id = :id",
                Params.builder(2).pv("options", JsonUtils.toJson(list)).pv("id", id).build());
        return ResultUtils.success();
    }
}
