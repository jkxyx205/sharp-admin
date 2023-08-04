package com.rick.admin.module.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-06-29 13:15:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationDTO {

    private Long id;

    @NotBlank
    private String code;

    @NotBlank
    private String description;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> characteristicCodes;

}
