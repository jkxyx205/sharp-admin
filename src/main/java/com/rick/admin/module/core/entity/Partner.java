package com.rick.admin.module.core.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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

    String remark;

    @Column(comment = "联系人")
    String contactPerson;

    @Column(comment = "联系方式")
    String contactNumber;

    @Column(comment = "联系邮箱")
    String contactMail;

    @Column(comment = "开户银行")
    String bankName;

    @Column(comment = "银行账户")
    String bankAccount;

    @Column(comment = "纳税人识别号")
    String taxCode;

    @Column(comment = "公司地址", columnDefinition = "varchar(128)")
    String address;

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