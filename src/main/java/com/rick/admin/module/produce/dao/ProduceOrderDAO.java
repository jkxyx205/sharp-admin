package com.rick.admin.module.produce.dao;

import com.rick.admin.module.produce.entity.ProduceOrder;
import com.rick.common.http.exception.BizException;
import com.rick.db.plugin.dao.core.EntityCodeDAOImpl;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/8/29 11:43
 */
@Repository
public class ProduceOrderDAO extends EntityCodeDAOImpl<ProduceOrder, Long> {

    private SharpService sharpService;

    public String findActiveProduceOrderByKeyCode(String keyCode) {
        List<String> codeList = selectByParams(Params.builder(1).pv("code", keyCode).build(),
                "code",
                "code LIKE CONCAT('%',:code) AND TIMESTAMPDIFF(DAY, update_time, now()) < 90", String.class);

        if (codeList.size() == 0) {
            throw new BizException("没有找到订单号，请输入更多的编号或者完整订单号！");
        } else if (codeList.size() > 1) {
            throw new BizException("系统找到了多个订单号，请输入更多的编号来确定唯一订单号");
        }

        return codeList.get(0);
    }
}