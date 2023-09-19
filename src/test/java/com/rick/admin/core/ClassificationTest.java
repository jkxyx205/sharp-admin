package com.rick.admin.core;

import com.rick.admin.module.core.service.ClassificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
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
        // classificationService.saveOrUpdate(new ClassificationDTO(null, "COLOR", "颜色", Arrays.asList("COLOR")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "LHQ", "离合器组件", Arrays.asList("LHQ")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "NLCL", "尼龙齿轮组件", Arrays.asList("NLCL", "ZHC")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "ZHZ", "转子组件", Arrays.asList("ZHZ", "ZHC")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "DG", "端盖组件", Arrays.asList("DG", "COLOR", "ZHC")));

//        classificationService.saveOrUpdate(new ClassificationDTO(null, "HFTL", "高速辐条轮毂组件", Arrays.asList("FTL", "COLOR", "ZHC", "NCQ")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "DFTL", "低速辐条轮毂组件", Arrays.asList("FTL", "COLOR", "ZHC")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "HYTL", "高速一体轮毂组件", Arrays.asList("YTL", "COLOR", "ZHC", "NCQ")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "DYTL", "低速一体轮毂组件", Arrays.asList("YTL", "COLOR", "ZHC")));

//        classificationService.saveOrUpdate(new ClassificationDTO(null, "XIANBAO", "线包组件", Arrays.asList("TIEX", "TONGX")));

//        classificationService.saveOrUpdate(new ClassificationDTO(null, "LINE", "普通线", Arrays.asList("XIN", "AREA", "XIANGXIAN","HUOERXIAN","LENGTH", "LINE_BRAND")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "LINE_FS", "防水线", Arrays.asList("XIN", "AREA", "JIETOU", "XIANGXIAN","HUOERXIAN","LENGTH", "LINE_BRAND")));
//        classificationService.saveOrUpdate(new ClassificationDTO(null, "LINE_GQ", "过桥线", Arrays.asList("LENGTH", "LINE_BRAND")));
    }
}
