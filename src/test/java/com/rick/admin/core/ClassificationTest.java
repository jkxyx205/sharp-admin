package com.rick.admin.core;

import com.rick.admin.module.core.model.ClassificationDTO;
import com.rick.admin.module.core.service.ClassificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
/**
 * @author Rick.Xu
 * @date 2023/7/20 17:00
 */
@SpringBootTest
public class ClassificationTest {

    @Resource
    private ClassificationService classificationService;

    /**
     * 添加或修改分类
     */
    @Test
    public void testInsertOrUpdate() {
        classificationService.saveOrUpdate(new ClassificationDTO(null, "COLOR", "颜色", Arrays.asList("COLOR")));
    }
}
