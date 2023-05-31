package com.rick.admin;

import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.user.entity.User;
import com.rick.db.plugin.dao.core.TableGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class TableGeneratorTest {

    @Autowired
    private TableGenerator tableGenerator;

    @Test
    public void generateTable() {
        tableGenerator.createTable(User.class);
        tableGenerator.createTable(Role.class);
        tableGenerator.createTable(Permission.class);
    }
}
