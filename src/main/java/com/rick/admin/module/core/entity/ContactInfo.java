package com.rick.admin.module.core.entity;

import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2023/9/25 14:14
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_contact", comment = "联系表")
public class ContactInfo extends BaseEntity<Long> {

    @Column(comment = "联系主体")
    String contactSubject;

    @Column(comment = "联系人")
    String contactPerson;

    @Column(comment = "联系方式")
    String contactNumber;

    @Column(comment = "联系邮箱")
    String contactMail;

    @Column(comment = "联系地址")
    String address;

    Long instanceId;

    @Override
    public String toString() {
        return address + " " + contactPerson + " " + contactNumber + " " + contactMail;
    }
}