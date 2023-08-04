package com.rick.admin.module.material.dao;

import com.rick.admin.module.material.entity.Classification;
import com.rick.db.plugin.dao.core.EntityDAOImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/8/4 11:23
 */
@Repository("materialClassificationDAO")
public class ClassificationDAO extends EntityDAOImpl<Classification, Long> {

}