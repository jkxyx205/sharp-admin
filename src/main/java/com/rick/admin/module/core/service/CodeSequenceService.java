package com.rick.admin.module.core.service;

import com.rick.admin.module.core.entity.CodeSequence;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/10/10 17:27
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CodeSequenceService {

    EntityDAO<CodeSequence, Long> codeSequenceDAO;

    public int getNextSequence(String prefix, String name) {
        return getNextSequence(prefix, name, 1);
    }

    public int getNextSequence(String prefix, String name, int num) {
        Optional<CodeSequence> codeSequenceOptional = OptionalUtils.expectedAsOptional(codeSequenceDAO.selectByParams(CodeSequence.builder()
                .prefix(prefix)
                .name(name)
                .build()));

        int sequence;
        if (codeSequenceOptional.isPresent()) {
            CodeSequence codeSequence = codeSequenceOptional.get();
            sequence =  codeSequence.getSequence();
        } else {
            sequence = 0;
        }

        codeSequenceDAO.update("sequence, name", new Object[] {sequence + num, name, prefix}, "prefix = ?");

        return sequence + 1;
    }

}