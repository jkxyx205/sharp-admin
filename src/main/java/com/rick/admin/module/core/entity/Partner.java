package com.rick.admin.module.core.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.admin.module.purchase.entity.MaterialSource;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/6/1 16:37
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_partner", comment = "合作伙伴")
public class Partner extends BaseCodeEntity {

    PartnerTypeEnum partnerType;

    String name;

    String shortName;

    String remark;

    @Column(comment = "联系人")
    String contactPerson;

    @Column(comment = "联系方式")
    String contactNumber;

    @Column(comment = "联系邮箱")
    String contactMail;

    @Column(comment = "传真")
    String contactFax;

    @Column(comment = "开户行")
    String bankName;

    @Column(comment = "行号")
    String bankNumber;

    @Column(comment = "账户名")
    String accountName;

    @Column(comment = "银行账户")
    String accountNumber;

    @Column(comment = "纳税人识别号")
    String taxCode;

    @Column(comment = "公司地址", columnDefinition = "varchar(128)")
    String address;

    @Column(comment = "发票收件信息", columnDefinition = "varchar(1024)")
    String invoiceReceiveInfo;

    @Column(comment = "发票备注", columnDefinition = "varchar(512)")
    String invoiceRemark;

    @OneToMany(subTable = "pur_source_list", cascadeInsertOrUpdate = true, joinValue = "partner_id", reversePropertyName="partnerId")
    List<MaterialSource> sourceList;

    @AllArgsConstructor
    @Getter
    public enum PartnerTypeEnum {
        /**
         * 客户
         */
        CUSTOMER("CUSTOMER"),
        /**
         * 供应商
         */
        VENDOR("VENDOR");

        @JsonValue
        public String getCode() {
            return this.name();
        }

        private final String label;

        public static PartnerTypeEnum valueOfCode(String code) {
            return valueOf(code);
        }
    }
}