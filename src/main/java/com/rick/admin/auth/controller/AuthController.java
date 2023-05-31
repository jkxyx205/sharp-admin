package com.rick.admin.auth.controller;

import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.permission.PermissionService;
import com.rick.admin.sys.permission.model.UserPermissionVO;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.role.model.RoleInfoDTO;
import com.rick.admin.sys.role.service.RoleService;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.admin.sys.user.service.UserService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.service.SharpService;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/10/19 1:55 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final UserDAO userDAO;

    private final RoleDAO roleDAO;
    
    private final SharpService sharpService;

    private final PermissionService permissionService;

    private final RoleService roleService;

    private final DictService dictService;

    /**
     * login页面
     *
     * @return
     */
    @GetMapping("login")
    public String login(Principal principal) {
        if (Objects.nonNull(principal)) {
            return "redirect:/";
        }
        return "login";
    }

    @DeleteMapping("users/{id}")
    @ResponseBody
    public Result deleteUser(@PathVariable Long id) {
        return ResultUtils.success(userDAO.deleteLogicallyById(id));
    }

    /**
     * 修改密码
     *
     * @return
     */
    @PostMapping("password/update")
    @ResponseBody
    public Result updatePassword(@RequestBody User user, Principal principal) {
        Long userId = ((AdminUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getId();
        userService.updatePassword(userId, user.getPassword());
        return ResultUtils.success();
    }

    /**
     * 重置密码
     *
     * @return
     */
    @PostMapping("password/{userId}/reset")
    @PreAuthorize("hasAuthority('user_management')")
    @ResponseBody
    public Result updatePassword(@PathVariable Long userId) {
        String newPassword = AuthConstants.DEFAULT_PASSWORD;
        userService.updatePassword(userId, newPassword);
        return ResultUtils.success(newPassword);
    }

    /**
     * 原密码验证
     *
     * @return
     */
    @GetMapping("password/check")
    @ResponseBody
    public boolean checkPassword(String password, Principal principal) {
        Long userId = ((AdminUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getId();
        return userService.checkPassword(userId, password);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role_management')")
    public String gotoAuthPage(Model model) {
        List<Role> roleList = roleDAO.selectAll();
        List<User> userList = userDAO.selectAll();

        Long roleId = roleList.get(0).getId();
        RoleInfoDTO roleInfo = roleService.getSettingsInfoByRoleId(roleId);
        // language=SQL
        List<TreeNode> permissionFullList = sharpService.query("SELECT p.id as \"id\", p.name as \"name\", pid as \"pId\", 1 as open\n" +
                "   FROM sys_permission P where is_deleted = 0 \n" +
                "  order by p.permission_order asc", null, TreeNode.class);

        model.addAttribute("roleList", roleList);
        model.addAttribute("userList", userList);
        model.addAttribute("roleInfo", roleInfo);
        model.addAttribute("userIds", roleInfo.getUserList().stream().map(user -> String.valueOf(user.getId())).collect(Collectors.toList()));
        model.addAttribute("permissionFullList", permissionFullList);

        return "sys/auth";
    }

    @GetMapping("/auth/{roleId}/info")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<RoleInfoDTO> getPermissionNodesByRoleId(@PathVariable Long roleId) {
        return ResultUtils.success(roleService.getSettingsInfoByRoleId(roleId));
    }

    @GetMapping("/auth/permission")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<UserPermissionVO> getPermissionNodesByUserId(Long userId) {
        User user = userDAO.selectById(userId).get();

        List<TreeNode> treeNodeList = permissionService.findTreeNodeByRoleIds(user.getRoleIds().stream().map(roleId -> Long.parseLong(roleId)).collect(Collectors.toSet()));

        UserPermissionVO userPermissionVO = UserPermissionVO.builder().permissionList(treeNodeList)
                .roleList(user.getRoleList()).build();

        return ResultUtils.success(userPermissionVO);
    }

    /**
     * 用户保存
     * @param roleId
     * @param userIds
     * @return
     */
    @PostMapping("/auth/assign/role/{roleId}/user")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result assignAuthUser(@PathVariable Long roleId, @RequestParam(value = "userIds[]", required = false) Set<String> userIds) {
        roleService.addUser(roleId, userIds);
        return ResultUtils.success();
    }

    /**
     * 权限保存
     * @param roleId
     * @param permissionIds
     * @return
     */
    @PostMapping("/auth/assign/role/{roleId}/permission")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result assignAuthPermission(@PathVariable Long roleId, @RequestParam(value = "permissionIds[]", required = false) Set<Long> permissionIds) {
        roleService.addPermission(roleId, permissionIds);
        return ResultUtils.success();
    }

    /**
     * 角色保存
     * @param roleList
     * @return
     */
    @PostMapping("/auth/add/roles")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result editAuthRole(@RequestBody List<Role> roleList) {
        Result<List<Role>> success = ResultUtils.success(roleService.editRoles(roleList));
        dictService.rebuild("sys_role");
        return success;
    }

    /**
     * 移除用户角色
     * @param roleId
     * @param userId
     * @return
     */
    @PostMapping("/auth/assign/role/{roleId}/{userId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result removeUserRoleAuth(@PathVariable Long roleId, @PathVariable String userId) {
        roleService.removeRoleByUserId(roleId, userId);
        return ResultUtils.success();
    }

}
