package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintDictAddDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsNameWithEnglishComma() {
        ConstraintDictAddDTO dto = validDTO();
        dto.setName("色盲,色弱");

        assertThat(validator.validate(dto))
                .extracting(ConstraintViolation::getMessage)
                .contains("约束名称不能包含逗号");
    }

    @Test
    void rejectsNameWithChineseComma() {
        ConstraintDictAddDTO dto = validDTO();
        dto.setName("色盲，色弱");

        assertThat(validator.validate(dto))
                .extracting(ConstraintViolation::getMessage)
                .contains("约束名称不能包含逗号");
    }

    @Test
    void acceptsNameWithoutComma() {
        ConstraintDictAddDTO dto = validDTO();
        dto.setName("色盲不可报考");

        assertThat(validator.validate(dto)).isEmpty();
    }

    private ConstraintDictAddDTO validDTO() {
        ConstraintDictAddDTO dto = new ConstraintDictAddDTO();
        dto.setCode("NO_COLOR_BLIND");
        dto.setName("色盲不可报考");
        dto.setCategory("身体条件");
        dto.setSeverity("HARD");
        dto.setIsActive(true);
        return dto;
    }
}
