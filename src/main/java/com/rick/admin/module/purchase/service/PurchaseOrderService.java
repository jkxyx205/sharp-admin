package com.rick.admin.module.purchase.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rick.admin.common.BigDecimalUtils;
import com.rick.admin.common.PropertiesConstants;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.admin.common.model.IdQuantity;
import com.rick.admin.module.core.dao.PartnerDAO;
import com.rick.admin.module.core.entity.Partner;
import com.rick.admin.module.core.service.CodeHelper;
import com.rick.admin.module.inventory.entity.InventoryDocument;
import com.rick.admin.module.material.service.BatchService;
import com.rick.admin.module.material.service.MaterialDescription;
import com.rick.admin.module.material.service.MaterialService;
import com.rick.admin.module.purchase.dao.PurchaseOrderDAO;
import com.rick.admin.module.purchase.dao.PurchaseOrderItemDAO;
import com.rick.admin.module.purchase.entity.PurchaseOrder;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.common.http.exception.BizException;
import com.rick.common.util.Time2StringUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.excel.core.ExcelWriter;
import com.rick.excel.core.model.ExcelCell;
import com.rick.meta.dict.entity.Dict;
import com.rick.meta.props.service.PropertyUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/6/20 13:39
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class PurchaseOrderService {

    PurchaseOrderDAO purchaseOrderDAO;

    SharpService sharpService;

    PurchaseOrderItemDAO purchaseOrderItemDAO;

    PartnerDAO partnerDAO;

    MaterialService materialService;

    BatchService batchService;

    /**
     * 新增或修改
     * @param order
     */
    public void saveOrUpdate(PurchaseOrder order) {
        if (order.getId() == null) {
            order.setCode(CodeHelper.generateCode("PO"));
        }

        order.getItemList().forEach(item -> {
            item.setPurchaseOrderCode(order.getCode());
        });

        batchService.saveBatch(order.getItemList());
        purchaseOrderDAO.insertOrUpdate(order);

    }

    @Transactional(rollbackFor = Exception.class)
    public void save(List<PurchaseOrder> list) {
        for (int i = 0; i < list.size(); i++) {
            PurchaseOrder purchaseOrder = list.get(i);

            purchaseOrder.setCode(CodeHelper.generateCode("PO")  + (i + 1) + RandomStringUtils.randomNumeric(1));
            purchaseOrder.getItemList().forEach(item -> {
                item.setComplete(false);
                item.setPurchaseOrderCode(purchaseOrder.getCode());
            });
        }

        batchService.saveBatch(list.stream().flatMap(purchaseOrder -> purchaseOrder.getItemList().stream()).collect(Collectors.toSet()));

        purchaseOrderDAO.insert(list);
    }

    /**
     * 获取采购订单 open 的数量
     * @param movementType
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> openQuantity(InventoryDocument.MovementTypeEnum movementType, String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        if (movementType == InventoryDocument.MovementTypeEnum.INBOUND) {
            // 同向
            PurchaseOrder purchaseOrder = purchaseOrderDAO.selectByCode(rootReferenceCode).orElseThrow(() -> new ResourceNotFoundException());

            for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
                BigDecimal value = item.getQuantity().subtract(ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(item.getId()), BigDecimal.ZERO));
                histroyGoodsReceiptQuantityMap.put(item.getId(), BigDecimalUtils.lt(value, BigDecimal.ZERO) ? BigDecimal.ZERO : value);
            }
        }

        return histroyGoodsReceiptQuantityMap;
    }

    /**
     * 历史收货数量
     * @param rootReferenceCode
     * @return
     */
    public Map<Long, BigDecimal> historyGoodsReceiptQuantity(String rootReferenceCode) {
        String sql = "select root_reference_item_id, ABS(sum(IF(movement_type = 'OUTBOUND', -1, 1) * quantity)) quantity from inv_document_item where `root_reference_code` = :rootReferenceCode group by root_reference_item_id";
        Map<Long, BigDecimal> histroyGoodsReceiptQuantityMap = sharpService.queryForKeyValue(sql, Params.builder(1).pv("rootReferenceCode", rootReferenceCode).build());

        PurchaseOrder purchaseOrder = purchaseOrderDAO.selectByCode(rootReferenceCode).orElseThrow(() -> new ResourceNotFoundException());

        for (PurchaseOrder.Item item : purchaseOrder.getItemList()) {
            histroyGoodsReceiptQuantityMap.put(item.getId(), ObjectUtils.defaultIfNull(histroyGoodsReceiptQuantityMap.get(item.getId()), BigDecimal.ZERO));
        }

        return histroyGoodsReceiptQuantityMap;
    }

    public Map<Long, BigDecimal> itemOrderQuantity(String rootReferenceCode) {
        return purchaseOrderItemDAO.selectByParamsAsMap
                (Params.builder(1).pv("purchaseOrderCode", rootReferenceCode).build(),
                        "id, quantity", "purchase_order_code = :purchaseOrderCode");
    }

    /**
     * 获取物料的供应商
     * @param materialId
     * @return
     */
    public List<Dict> getVendorByMaterialId(Long materialId) {
        String sql = "select id name, name label from core_partner where `partner_type` = 'VENDOR' and exists(select 1 from pur_source_list left join mm_material on mm_material.`category_id` = pur_source_list.`material_category_id` AND pur_source_list.material_id is null  where (mm_material.id = :materialId or `material_id` = :materialId) AND core_partner.id = partner_id)";
        return sharpService.query(sql, Params.builder(1).pv("materialId", materialId).build(), Dict.class);
    }

    /**
     * 批量获取物料的供应商
     * @param materialIds
     * @return
     */
    public Map<String, List<Dict>> getVendorByMaterialIds(Collection<Long> materialIds) {
        String sql = "select sl.material_id, core_partner.id name, core_partner.name label from core_partner inner join (select distinct `partner_id`, ifnull(`material_id`, mm_material.id) material_id from pur_source_list left join mm_material on mm_material.`category_id` = pur_source_list.`material_category_id` AND pur_source_list.material_id is null  where mm_material.id IN (:materialIds) or `material_id` IN (:materialIds)) sl on sl.partner_id = core_partner.id where `partner_type` = 'VENDOR' ORDER BY core_partner.name ASC";
        List<Map<String, Object>> list = sharpService.query(sql, Params.builder(1).pv("materialIds", materialIds).build());
        Map<Long, List<Map<String, Object>>> map = list.stream().collect(Collectors.groupingBy(m -> (Long) m.get("material_id")));
        Map<String, List<Dict>> resultMap = Maps.newHashMapWithExpectedSize(map.size());
        map.forEach((k, v) -> {
            List<Dict> resultList = Lists.newArrayListWithExpectedSize(v.size());
            for (Map<String, Object> option : v) {
                resultList.add(new Dict(null, String.valueOf(option.get("name")), (String) option.get("label"), null));
            }
            resultMap.put(String.valueOf(k), resultList);

        });
        return resultMap;
    }

    public void setItemCompleteStatus(List<Long> completedIdList, List<Long> uncompletedIdList, String rootReferenceCode) {
        purchaseOrderItemDAO.setCompleted(completedIdList);
        purchaseOrderItemDAO.setUnCompleted(uncompletedIdList);
        ifAllCompleteAndSetDone(rootReferenceCode);
    }

    public void ifAllCompleteAndSetDone(String rootReferenceCode) {
        List<Boolean> list = purchaseOrderItemDAO.complateStatusList(rootReferenceCode);

        boolean hasUnComplete = list.stream().anyMatch(completed -> !completed);
        purchaseOrderDAO.update("status"
                , Params.builder(2).pv("code", rootReferenceCode).pv("status", !hasUnComplete ? PurchaseOrder.StatusEnum.DONE : PurchaseOrder.StatusEnum.PLANNING).build(),
                "code = :code");
    }

    public void checkItemOpenQuantity(InventoryDocument.MovementTypeEnum movementType, String purchaseCode, List<IdQuantity> idQuantityList) {
        Map<Long, BigDecimal> itemHistroyGoodsReceiptQuantityMap = historyGoodsReceiptQuantity(purchaseCode);
        Map<Long, BigDecimal> itemOrderQuantityMap = itemOrderQuantity(purchaseCode);

        Map<Long, BigDecimal> itemOpenQuantityMap = null;
        if (movementType == InventoryDocument.MovementTypeEnum.OUTBOUND) {
            itemOpenQuantityMap = openQuantity(movementType, purchaseCode);
        }

        for (IdQuantity idQuantity : idQuantityList) {
            if (movementType == InventoryDocument.MovementTypeEnum.INBOUND) {
                // 收货最大容差
                BigDecimal overReceiptTolerance = new BigDecimal(PropertyUtils.getProperty(PropertiesConstants.OVER_RECEIPT_TOLERANCE));

                if (BigDecimalUtils.gt(itemHistroyGoodsReceiptQuantityMap.get(idQuantity.getId()).add(idQuantity.getQuantity()),
                        itemOrderQuantityMap.get(idQuantity.getId()).multiply(BigDecimal.ONE.add(overReceiptTolerance.divide(BigDecimal.valueOf(100)))))) {
                    throw new BizException(idQuantity.getDescription() + " 收货超过订单总数的 " + overReceiptTolerance + "%，不能入库！");
                }
            } else {
                if (BigDecimalUtils.gt(idQuantity.getQuantity(), itemOpenQuantityMap.get(idQuantity.getId()))) {
                    throw new BizException(ExceptionCodeEnum.MATERIAL_OVER_MAX_MOVEMENT_ERROR, new Object[]{idQuantity.getDescription()});
                }
            }
        }

    }

    public void downloadById(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        PurchaseOrder purchaseOrder = purchaseOrderDAO.selectById(id).get();
        Partner partner = partnerDAO.selectById(purchaseOrder.getPartnerId()).get();
        OutputStream os = HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, partner.getName() + "_" + purchaseOrder.getCode() + ".xlsx");

        // 这样就不会改变模版文件
        final ClassPathResource classPathResource = new ClassPathResource("templates/excel/po.xlsx");
        byte[] bytes = IOUtils.toByteArray(classPathResource.getInputStream());

        ExcelWriter excelWriter = new ExcelWriter(new XSSFWorkbook(new ByteArrayInputStream(bytes)));

        excelWriter.writeCell(new ExcelCell(8, 3, "PO NO: "+ purchaseOrder.getCode()));
        excelWriter.writeCell(new ExcelCell(1, 5, "供方（Vendor）：" + partner.getName()));
        excelWriter.writeCell(new ExcelCell(2, 6, purchaseOrder.getContactPerson() + " " + purchaseOrder.getContactNumber()));
        excelWriter.writeCell(new ExcelCell(2, 7, partner.getContactFax()));
        excelWriter.writeCell(new ExcelCell(2, 8, partner.getContactFax()));
        excelWriter.writeCell(new ExcelCell(1, 9, "ADD：  " + partner.getAddress()));

//        excelWriter.writeCell(new ExcelCell(7, 5, "需方：XX制造（苏州）有限公司"));
//        excelWriter.writeCell(new ExcelCell(7, 6, "慧博士 18898876623"));
//        excelWriter.writeCell(new ExcelCell(7, 7, "0512-77359511"));
//        excelWriter.writeCell(new ExcelCell(7, 8, "0512-77359511"));
//        excelWriter.writeCell(new ExcelCell(7, 10, "交货地点：苏州市XXX一号厂房大厅"));

        ArrayList<Object[]> data = new ArrayList<>();
        materialService.fillMaterialDescription(purchaseOrder.getItemList());
        for (int i = 0; i < purchaseOrder.getItemList().size(); i++) {
            PurchaseOrder.Item item = purchaseOrder.getItemList().get(i);
            MaterialDescription materialDescription = item.getMaterialDescription();
            //        data.add(new Object[]{1, "资材编号1", "品 名", "型号规格", 3, "单位", 1, 11, "2022-11-16", "备注"});
            data.add(new Object[] {i + 1, materialDescription.getCode(), materialDescription.getName(),
                    materialDescription.getSpecification() + " " + materialDescription.getCharacteristic(),
                    item.getQuantity(), materialDescription.getUnitText(), item.getUnitPrice(), item.getAmount(),
                    Time2StringUtils.format(item.getDeliveryDate()),
                    item.getRemark() + (item.getPurchaseSend() ? "\n直发客户：" + item.getSoInfo().get("name") : "发普源")
            });
        }

        int rowSize = data.size();

        // 获取 cell 样式
        XSSFRow row = excelWriter.getActiveSheet().getRow(11);
        int physicalNumberOfCells = row.getPhysicalNumberOfCells();
        XSSFCellStyle[] cellStyles = new XSSFCellStyle[physicalNumberOfCells];
        for (int i = 0; i < physicalNumberOfCells; i++) {
            cellStyles[i] = row.getCell(i).getCellStyle();
        }

        XSSFColor redColor = new XSSFColor(Color.RED, new DefaultIndexedColorMap());

        // 文字色
        XSSFFont font = excelWriter.getBook().createFont();
        font.setColor(redColor);
        font.setBold(true);

        excelWriter.insertAndWriteRow(1, 12, data, row.getHeightInPoints(), cellStyles, (ecell, cell) -> {
            if (ecell.getX() == 9 && !String.valueOf(ecell.getValue()).matches("\\d{4}-\\d{2}-\\d{2}")) {
                XSSFCellStyle newStyle = cell.getCellStyle().copy();
                font.setFamily(newStyle.getFont().getFamily());
                font.setFontName(newStyle.getFont().getFontName());
                font.setFontHeight(newStyle.getFont().getFontHeight());
                newStyle.setFont(font);
                cell.setCellStyle(newStyle);
            }
        });

        BigDecimal amount = purchaseOrder.getAmount().setScale(2, RoundingMode.HALF_UP);
        String downloadDate = Time2StringUtils.format(new Date());
        excelWriter.writeCell(new ExcelCell(3, 13 + rowSize, "¥" + amount));
        excelWriter.writeCell(new ExcelCell(3, 14 + rowSize, "RMB" + amount));
        excelWriter.writeCell(new ExcelCell(2, 15 + rowSize, purchaseOrder.getRemark()));

        // 地址表
        int addressSize = 0;
        for (int i = 0; i < purchaseOrder.getItemList().size(); i++) {
            PurchaseOrder.Item item = purchaseOrder.getItemList().get(i);
            if (item.getPurchaseSend()) {
                excelWriter.insertAndWriteCell(new ExcelCell(1, 16 + rowSize + addressSize, 30f, cellStyles[1],
                        (MapUtils.isNotEmpty(item.getSoInfo()) ? item.getSoInfo().get("name") + ":" : "") + item.getContactInfo(), 1, 10));
                addressSize++;
            }
        }

        excelWriter.writeCell(new ExcelCell(4, 28 + rowSize + addressSize, "本公司确认：方慧" + downloadDate));

        excelWriter.getBook().setSheetName(0, downloadDate);

        // 插入图片
        //创建一个excel文件，名称为：
        XSSFWorkbook workbook = excelWriter.getBook();
        //创建一个sheet，名称为工作簿1
        XSSFSheet sheet = excelWriter.getActiveSheet();

        BufferedImage bufferImg;
        //先把读进来的图片放到一个ByteArrayOutputStream中，以便产生ByteArray
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        final ClassPathResource classPathResource2 = new ClassPathResource("templates/excel/seal.png");

        //获取图片后缀
        bufferImg = ImageIO.read(classPathResource2.getInputStream());
        ImageIO.write(bufferImg, "png", byteArrayOut);

        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        XSSFDrawing patriarch = sheet.createDrawingPatriarch();
        //anchor主要用于设置图片的属性
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) 8, 19 + rowSize + addressSize, (short) 10, 30 + rowSize +addressSize);
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        //插入图片
        patriarch.createPicture(anchor, workbook.addPicture(byteArrayOut.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG));

        excelWriter.toFile(os);
    }

}