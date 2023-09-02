package com.rick.admin.module.produce.controller;

import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.BomService;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelRow;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Rick.Xu
 * @date 2023/9/1 14:34
 */
@Controller
@RequestMapping("produce_bom")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProduceBOMDownloadController {

    MaterialService materialService;

    BatchService batchService;

    ProduceOrderItemDAO produceOrderItemDAO;

    BomService bomService;

    @GetMapping("{itemId}/download")
    public void download(@PathVariable Long itemId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProduceOrder.Item item = produceOrderItemDAO.selectById(itemId).get();
        Map<Long, ProduceOrder.Item.Detail> valueMapping = item.getItemList().stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getComponentDetailId, v -> v));

        batchService.fillCharacteristicValue(Stream.concat(item.getItemList().stream(), Stream.of(item)).collect(Collectors.toSet()));

        BomTemplate bomTemplate = bomService.getBomTemplateMaterialId(item.getMaterialId(), valueMapping, false);
        materialService.fillMaterialDescription(Arrays.asList(item));

        float heightInPoints = 25f;

        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.getActiveSheet().setColumnWidth(0, 5000);
        excelWriter.getActiveSheet().setColumnWidth(1, 9000);

        excelWriter.writeRow(new ExcelRow(1,1, heightInPoints, "生产单号：" + item.getProduceOrderCode()));
        excelWriter.writeRow(new ExcelRow(3,1, heightInPoints, new Object[]{"交货日期：" + Time2StringUtils.format(item.getDeliveryDate())}));
        excelWriter.writeRow(new ExcelRow(1,2));

        excelWriter.writeRow(new ExcelRow(1,3, heightInPoints, new Object[]{"产成品名称", "规格 & 特征值", "数量", "单位", "备注"}));
        excelWriter.writeRow(new ExcelRow(1,4, heightInPoints, new Object[]{item.getMaterialName(), (StringUtils.isBlank(item.getMaterialSpecification()) ? "" : item.getMaterialSpecification() + " ") + item.getCharacteristic(),
        item.getQuantity(), item.getUnitText(), item.getRemark()}));

        excelWriter.writeRow(new ExcelRow(1,5));
        excelWriter.writeRow(new ExcelRow(1,6, heightInPoints, new Object[]{"BOM:"}));
        excelWriter.writeRow(new ExcelRow(1,7));

        AtomicInteger integer = new AtomicInteger(8);

        writeBomList(item.getQuantity(), bomTemplate, excelWriter, integer);
        excelWriter.toFile(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, item.getProduceOrderCode() + "_" + item.getMaterialName() + ".xlsx"));
    }

    private void writeBomList(BigDecimal itemQuantity, BomTemplate bomTemplate, ExcelWriter excelWriter, AtomicInteger integer) {
        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                BomTemplate subBomTemplate = componentDetail.getBomTemplate();
                if (Objects.nonNull(subBomTemplate)) {
                    writeBomList(itemQuantity, subBomTemplate, excelWriter, integer);
                } else {
                    ProduceOrder.Item.Detail value = componentDetail.getValue();
                    excelWriter.writeRow(new ExcelRow(1, integer.getAndIncrement(), 25f, value.getMaterialName(), (StringUtils.isBlank(value.getMaterialSpecification()) ? "" : value.getMaterialSpecification() + " ") + value.getCharacteristic(),
                            value.getQuantity().multiply(itemQuantity), value.getUnitText(), value.getRemark()));
                }
            }
        }
    }

}
