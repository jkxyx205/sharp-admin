package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Batch;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2023/8/4 18:15
 */
@Repository
public class BatchDAO extends EntityDAOImpl<Batch, Long> {

    @Resource
    private JdbcTemplate jdbcTemplate;

    public Optional<Long> selectIdByKeyCode(String materialCode, String batchCode) {
        return expectedAsOptional(jdbcTemplate.queryForList("select id from mm_batch where material_code = ? AND code = ?", new Object[] {materialCode, batchCode}, Long.class));
//        return expectedAsOptional(selectByParams(Params.builder(2)
//                .pv("code", batchCode)
//                .pv("materialCode", materialCode).build(), "id", Long.class));
    }
}