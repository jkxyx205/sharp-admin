package com.rick.admin.init;

import com.rick.admin.module.core.dao.PartnerDAO;
import com.rick.admin.module.core.entity.Partner;
import com.rick.excel.core.ExcelReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/8/15 10:09
 */
@SpringBootTest
public class PartnerDataHandler {

    @Resource
    private PartnerDAO partnerDAO;

    /**
     * 供应商信息导入
     * @throws Exception
     */
    @Test
    public void importVendorData() throws Exception {
        partnerDAO.delete(Collections.emptyMap(), "partner_type = 'VENDOR'");

        String path = "/Users/rick/Space/tmp/py/data/vendor.xlsx";

        ExcelReader.readExcelContent(new FileInputStream(path), (index, data, sheetIndex, sheetName) -> {
            if (sheetIndex == 0 && index >= 3) {
                if (data.length > 0 && StringUtils.isNotBlank((CharSequence) data[1])) {
                    partnerDAO.insert(Partner.builder()
                            .code("V" + String.format("%05d", index - 2))
                            .name((String) data[1])
                            .shortName((String) data[2])
                                    .contactPerson((String) data[3])
                                    .contactNumber((data[4] instanceof Double) ? ((Double)data[4]).longValue() + "" : Objects.toString(data[4], ""))
                                    .address((String) data[5])
                                    .accountName((String) data[6])
                                    .accountNumber(Objects.toString(data[8], ""))
                                    .bankName(Objects.toString(data[9], "").trim())
                                    .bankNumber((String) data[10])
                                    .remark(String.valueOf(data[11]))
                            .partnerType(Partner.PartnerTypeEnum.VENDOR)
                            .build());
                }
            }

            if (sheetIndex > 0) {
                return false;
            }

            return true;
        });
    }

    /**
     * 客户信息导入
     * @throws Exception
     */
    @Test
    public void importCustomerData() throws Exception {
        partnerDAO.delete(Collections.emptyMap(), "partner_type = 'CUSTOMER'");

        String path = "/Users/rick/Space/tmp/py/data/customer.xlsx";

        ExcelReader.readExcelContent(new FileInputStream(path), (index, data, sheetIndex, sheetName) -> {
            if (sheetIndex == 0 && index >= 2) {
                if (data.length > 0 && StringUtils.isNotBlank((CharSequence) data[0])) {
                    String[] accountInfo = parseAccountInfo((String) data[1]);

                    partnerDAO.insert(Partner.builder()
                            .code("C" + String.format("%05d", index - 1))
                            .name((String) data[0])
                            .bankName(accountInfo[0].trim())
                            .accountNumber(accountInfo[1].trim())
                            .taxCode((String) data[2])
                            .invoiceRemark((String) data[3])
                            .invoiceReceiveInfo((String) data[6])
                            .partnerType(Partner.PartnerTypeEnum.CUSTOMER)
                            .build());
                }
            }

            if (sheetIndex > 0) {
                return false;
            }

            return true;
        });
    }

    private String[] parseAccountInfo(String info) {
        if (StringUtils.isBlank(info)) {
            return new String[] {"", ""};
        }

        String[] split = info.split("\\s*\n\\s*");

        if (split.length == 1) {
            split =  new String[] {split[0], ""};
        }

        if (StringUtils.isNotBlank(split[1])) {
            split[1] = split[1].replaceAll("[^0-9]", "");
        }

        return split;
    }
}
