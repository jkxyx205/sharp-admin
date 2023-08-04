package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Batch;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/8/4 18:15
 */
@Repository
public class BatchDAO extends EntityDAOImpl<Batch, Long> {

    public Optional<Long> selectIdByKeyCode(String materialCode, String batchCode) {
        return expectedAsOptional(selectByParams(Params.builder(2)
                .pv("code", batchCode)
                .pv("materialCode", materialCode).build(), "id", Long.class));
    }
}