package com.rick.admin.init;

import com.rick.admin.module.core.dao.PartnerDAO;
import com.rick.admin.module.core.entity.Partner;
import com.rick.excel.core.ExcelReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/8/15 10:09
 */
@SpringBootTest
public class PartnerDataHandler {

    @Resource
    private PartnerDAO partnerDAO;

//    @Test
    public void importData() throws Exception {
        partnerDAO.deleteAll();

        String path = "/Users/rick/Space/Yodean/苏州普源电机/data/partner.xlsx";

        ExcelReader.readExcelContent(new FileInputStream(path), (index, data, sheetIndex, sheetName) -> {
            if (sheetIndex == 0 && index >= 3) {
                if (StringUtils.isNotBlank((CharSequence) data[1])) {
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
}
