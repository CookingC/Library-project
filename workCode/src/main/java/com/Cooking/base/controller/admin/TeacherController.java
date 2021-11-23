package com.Cooking.base.controller.admin;
import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.PageBean;
import com.Cooking.base.bean.Result;
import com.Cooking.base.entity.admin.Teacher;
import com.Cooking.base.entity.admin.User;
import com.Cooking.base.service.admin.TeacherService;
import com.Cooking.base.service.admin.TeacherTypeService;
import com.Cooking.base.util.StringUtil;
import com.Cooking.base.util.ValidateEntityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/tea")
@Controller
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
 private TeacherTypeService teacherTypeService;

    @RequestMapping(value = "/list")
    public String list(Model model, Teacher teacher, PageBean<Teacher> pageBean) {
        model.addAttribute("title", "教师列表");
        if (teacher.getUser() != null) {
            model.addAttribute("name", teacher.getUser().getNickName());
        }
        model.addAttribute("pageBean", teacherService.findByName(teacher, pageBean));
        return "admin/teacher/list";
    }

    /**
     * 教师修改
     *
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String edit(Model model, @RequestParam(name = "id") Long id) {
        model.addAttribute("teacherTypes",teacherTypeService.findAll());
        model.addAttribute("teacher", teacherService.findByID(id));
        return "admin/teacher/edit";
    }

    /**
     * 教师添加页面
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("teacherTypes",teacherTypeService.findAll());
        return "admin/teacher/add";
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(long id) {
        try {
            teacherService.delete(id);
        } catch (Exception e) {

            return Result.error(CodeMsg.ADMIN_ROLE_DELETE_ERROR);
        }

        return Result.success(true);
    }

    /**
     * 编辑信息表单提交处理
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> add(Teacher teacher) {
        CodeMsg validate = ValidateEntityUtil.validate(teacher);
        if (validate.getCode() != CodeMsg.SUCCESS.getCode()) {
            return Result.error(validate);
        }

        if (!StringUtil.emailFormat(teacher.getUser().getEmail())){
            return   Result.error(CodeMsg.ADMIN_PUBLIC_EMAIL);
        }
        if (!StringUtil.isMobile(teacher.getUser().getMobile())){
            return   Result.error(CodeMsg.ADMIN_PUBLIC_MOBILE);
        }
        /**
        /**
         * 有id是修改需要判断
         * 无id是增加无需判断
         */
        if (teacher.getId() != null) {
            Teacher byID = teacherService.findByID(teacher.getId());
            byID.setTeacherType(teacher.getTeacherType());
            User user = teacher.getUser();
            BeanUtils.copyProperties(user, byID.getUser(), "id", "createTime", "updateTime", "password", "username", "role","status");
            teacherService.update(byID);
        } else {
            //学生学号
            teacherService.add(teacher);
        }
        return Result.success(true);
    }

}
