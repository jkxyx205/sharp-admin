package com.rick.admin.module.produce.controller;

import com.google.common.collect.Lists;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.produce.dao.ProduceOrderDAO;
import com.rick.admin.module.produce.dao.ProduceOrderItemDAO;
import com.rick.admin.module.produce.entity.BomTemplate;
import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.admin.module.produce.service.BomService;
import com.rick.admin.module.produce.service.ProduceScheduleService;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelRow;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

    EntityDAO<ProduceOrder.Item.Schedule, Long> produceOrderItemScheduleDAO;

    ProduceScheduleService produceScheduleService;

    ProduceOrderDAO produceOrderDAO;

    @PutMapping("schedule/{scheduleId}")
    @ResponseBody
    public Result markStatus(@PathVariable Long scheduleId) {
        produceScheduleService.markStatus(scheduleId);
        return ResultUtils.success();
    }
    @GetMapping("schedule/{scheduleId}/view")
    public String preview(@PathVariable Long scheduleId, Model model) {
        ProduceOrder.Item.Schedule schedule = produceOrderItemScheduleDAO.selectById(scheduleId).get();
        ProduceOrder.Item item = produceOrderItemDAO.selectById(schedule.getProduceOrderItemId()).get();
        BomTemplate bomTemplate = resolveItemAndReturnBomTemplate(item);
        model.addAttribute("schedule", schedule);
        model.addAttribute("item", item);
        model.addAttribute("bomTemplate", bomTemplate);

        List<Object[]> data = Lists.newArrayList();
        fetchBomList(schedule.getQuantity(), bomTemplate, data);
        model.addAttribute("data", data);

        model.addAttribute("startDate", Time2StringUtils.format(schedule.getStartDate()));
        model.addAttribute("deliveryDate", Time2StringUtils.format(item.getDeliveryDate()));
        model.addAttribute("remark", Objects.toString(produceOrderDAO.selectSingleValueById(item.getProduceOrderId(), "remark", String.class).get(), "") +  Objects.toString(item.getRemark(), "") + Objects.toString(schedule.getRemark(), ""));
        model.addAttribute("itemMaterialSpecification", (StringUtils.isBlank(item.getMaterialSpecification()) ? "" : item.getMaterialSpecification() + " ") + item.getCharacteristic() +    " " + item.getSpecification());
        model.addAttribute("scheduleQuantity", schedule.getQuantity().stripTrailingZeros().toPlainString());
        return "modules/produce_schedule_detail";
    }

    @GetMapping("schedule/{scheduleId}/download")
    public void scheduleDownload(@PathVariable Long scheduleId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProduceOrder.Item.Schedule schedule = produceOrderItemScheduleDAO.selectById(scheduleId).get();
        ProduceOrder.Item item = produceOrderItemDAO.selectById(schedule.getProduceOrderItemId()).get();
        BomTemplate bomTemplate = resolveItemAndReturnBomTemplate(item);
        String startDate = Time2StringUtils.format(schedule.getStartDate());
        float heightInPoints = 25f;

        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.getActiveSheet().setColumnWidth(0, 5000);
        excelWriter.getActiveSheet().setColumnWidth(1, 9000);
        excelWriter.getBook().setSheetName(0, startDate);

        excelWriter.writeRow(new ExcelRow(1,1, heightInPoints, "生产单号：" + schedule.getCode()));
        excelWriter.writeRow(new ExcelRow(3,1, heightInPoints, new Object[]{"交货日期：" + Time2StringUtils.format(item.getDeliveryDate())}));

        excelWriter.writeRow(new ExcelRow(1,2, heightInPoints, new Object[]{"计划生产日期：" + startDate}));
        excelWriter.writeRow(new ExcelRow(1,3, heightInPoints, new Object[]{"备注：" + Objects.toString(produceOrderDAO.selectSingleValueById(item.getProduceOrderId(), "remark", String.class).get(), "") +  Objects.toString(item.getRemark(), "") + Objects.toString(schedule.getRemark(), "")}));

        excelWriter.writeRow(new ExcelRow(1,4, heightInPoints, new Object[]{"产成品名称", "规格 & 特征值", "数量", "单位", "备注"}));
        excelWriter.writeRow(new ExcelRow(1,5, heightInPoints, new Object[]{item.getMaterialName(), (StringUtils.isBlank(item.getMaterialSpecification()) ? "" : item.getMaterialSpecification() + " ") + item.getCharacteristic() +    " " + item.getSpecification(),
                schedule.getQuantity(), item.getUnitText(), item.getRemark()}));

        excelWriter.writeRow(new ExcelRow(1,6));
        excelWriter.writeRow(new ExcelRow(1,7, heightInPoints, new Object[]{"BOM:"}));
        excelWriter.writeRow(new ExcelRow(1,8));

        AtomicInteger integer = new AtomicInteger(9);

        writeBomList(schedule.getQuantity(), bomTemplate, excelWriter, integer);
        excelWriter.toFile(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, schedule.getCode() + "_" + item.getMaterialName() + "_" + Time2StringUtils.format(schedule.getStartDate()) + ".xlsx"));
    }

    @GetMapping("{itemId}/download")
    public void download(@PathVariable Long itemId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProduceOrder.Item item = produceOrderItemDAO.selectById(itemId).get();
        BomTemplate bomTemplate = resolveItemAndReturnBomTemplate(item);

        float heightInPoints = 25f;

        ExcelWriter excelWriter = new ExcelWriter();
        excelWriter.getActiveSheet().setColumnWidth(0, 5000);
        excelWriter.getActiveSheet().setColumnWidth(1, 9000);

        excelWriter.writeRow(new ExcelRow(1,1, heightInPoints, "销售单号：" + item.getProduceOrderCode()));
        excelWriter.writeRow(new ExcelRow(3,1, heightInPoints, new Object[]{"交货日期：" + Time2StringUtils.format(item.getDeliveryDate())}));
        excelWriter.writeRow(new ExcelRow(1,2));

        excelWriter.writeRow(new ExcelRow(1,3, heightInPoints, new Object[]{"产成品名称", "规格 & 特征值", "数量", "单位", "备注"}));
        excelWriter.writeRow(new ExcelRow(1,4, heightInPoints, new Object[]{item.getMaterialName(), (StringUtils.isBlank(item.getMaterialSpecification()) ? "" : item.getMaterialSpecification() + " ") + item.getCharacteristic() +    " " + item.getSpecification(),
        item.getQuantity(), item.getUnitText(), item.getRemark()}));

        excelWriter.writeRow(new ExcelRow(1,5));
        excelWriter.writeRow(new ExcelRow(1,6, heightInPoints, new Object[]{"BOM:"}));
        excelWriter.writeRow(new ExcelRow(1,7));

        AtomicInteger integer = new AtomicInteger(8);

        writeBomList(item.getQuantity(), bomTemplate, excelWriter, integer);
        excelWriter.toFile(HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, item.getProduceOrderCode() + "_" + item.getMaterialName() + ".xlsx"));
    }

    private void writeBomList(BigDecimal qantity, BomTemplate bomTemplate, ExcelWriter excelWriter, AtomicInteger integer) {
        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                BomTemplate subBomTemplate = componentDetail.getBomTemplate();
                if (Objects.nonNull(subBomTemplate)) {
                    writeBomList(qantity, subBomTemplate, excelWriter, integer);
                } else {
                    ProduceOrder.Item.Detail value = componentDetail.getValue();
                    if (Objects.nonNull(value.getMaterialId())) {
                        excelWriter.writeRow(new ExcelRow(1, integer.getAndIncrement(), 25f, value.getMaterialName(), (StringUtils.isBlank(value.getMaterialSpecification()) ? "" : value.getMaterialSpecification() + " ") + value.getCharacteristic(),
                                value.getQuantity().multiply(qantity), value.getUnitText(), value.getRemark()));
                    }
                }
            }
        }
    }

    private void fetchBomList(BigDecimal quantity, BomTemplate bomTemplate, List<Object[]> dataList) {
        for (BomTemplate.Component component : bomTemplate.getComponentList()) {
            for (BomTemplate.ComponentDetail componentDetail : component.getComponentDetailList()) {
                BomTemplate subBomTemplate = componentDetail.getBomTemplate();
                if (Objects.nonNull(subBomTemplate)) {
                    fetchBomList(quantity, subBomTemplate, dataList);
                } else {
                    ProduceOrder.Item.Detail value = componentDetail.getValue();
                    if (Objects.nonNull(value.getMaterialId())) {
                        dataList.add(new Object[]{value.getMaterialName(), (StringUtils.isBlank(value.getMaterialSpecification()) ? "" : value.getMaterialSpecification() + " ") + value.getCharacteristic(),
                                value.getQuantity().multiply(quantity).stripTrailingZeros().toPlainString(), value.getUnitText(), value.getRemark()});
                    }
                }
            }
        }
    }

    private BomTemplate resolveItemAndReturnBomTemplate(ProduceOrder.Item item) {
        Map<Long, ProduceOrder.Item.Detail> valueMapping = item.getItemList().stream().collect(Collectors.toMap(ProduceOrder.Item.Detail::getComponentDetailId, v -> v));

        batchService.fillCharacteristicValue(Stream.concat(item.getItemList().stream(), Stream.of(item)).collect(Collectors.toSet()));
        BomTemplate bomTemplate = bomService.getBomTemplateMaterialId(item.getMaterialId(), valueMapping, false);
        materialService.fillMaterialDescription(Arrays.asList(item));
        return bomTemplate;
    }

}
