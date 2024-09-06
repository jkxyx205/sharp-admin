package com.rick.admin.module.core.entity;

import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * @author Rick.Xu
 * @date 2023/6/1 11:11
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "core_plant", comment = "库房")
public class Plant extends BaseCodeEntity<Long> {

    @NotBlank(message = "库房名称不能为空")
    @Length(max = 32, message = "库房名称不能超过16个字符")
    String name;

    String province;

    String city;

    String area;

    String town;

    @Column(comment = "联系人")
    String contactPerson;

    @Column(comment = "联系方式")
    String contactNumber;

    @Column(comment = "联系邮箱")
    String contactMail;

    @Length(max = 128, message = "详细地址不能超过128个字符")
    String detailAddress;
}