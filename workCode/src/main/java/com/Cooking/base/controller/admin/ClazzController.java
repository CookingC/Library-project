package com.Cooking.base.controller.admin;
import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.PageBean;
import com.Cooking.base.bean.Result;
import com.Cooking.base.dao.admin.ClazzDao;
import com.Cooking.base.entity.admin.Clazz;
import com.Cooking.base.service.admin.ClazzService;
import com.Cooking.base.service.admin.OperaterLogService;
import com.Cooking.base.service.admin.TeacherService;
import com.Cooking.base.util.ValidateEntityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/clbum")
@Controller
public class ClazzController {

    @Autowired
    private ClazzDao clazzDao;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private OperaterLogService operaterLogService;

    /**
     * 
     * @param model
     * @param clazz
     * @param pageBean
     * @return
     */
    @RequestMapping(value="/list")
    public String list(Model model, Clazz clazz, PageBean<Clazz> pageBean){
        model.addAttribute("title", "班级列表");
        model.addAttribute("teacher" , teacherService.findAll());
        model.addAttribute("claname", clazz.getClaname());
        model.addAttribute("pageBean",clazzService.findList(clazz,pageBean));
        return "admin/clbum/list";
    }

    /**
     * 添加班级页面
     * @param model
     * @return
     */
    @RequestMapping(value="/add",method= RequestMethod.GET)
    public String add(Model model){
        model.addAttribute("teachers",teacherService.findAll());
        model.addAttribute("clazz", clazzService.findAll());
        return "admin/clbum/add";
    }

    /**
     * Add form submission processing to the class
     * @param clazz
     * @return
     */
    @RequestMapping(value="/add",method=RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> add(Clazz clazz){
        CodeMsg validate = ValidateEntityUtil.validate(clazz);
        if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
            return Result.error(validate);
        }
        if(clazz.getTeacher() == null || clazz.getTeacher().getTeacherSno() == null){
            return Result.error(CodeMsg.ADMIN_ClAZZ_EMPTY );
        }
        if(clazzService.isExistClaName(clazz.getClaname(), 0l)){
            return Result.error(CodeMsg.ADMIN_ClAZZNAME_EXIST);
        }
        if(clazzService.save(clazz) == null){
            return Result.error(CodeMsg.ADMIN_ClAZZ_ADD_ERROR);
        }
        operaterLogService.add("Add class, class name" + clazz.getClaname());
        return Result.success(true);
    }
    /**
     * Class page
     * @param model
     * @return
     */
    @RequestMapping(value="/edit")
    public String edit(Model model,@RequestParam(name="id",required=true)Long id){
        model.addAttribute("teachers",teacherService.findAll());
        model.addAttribute("clazz",clazzService.find(id));
        return "admin/clbum/edit";
    }

    /**
     * Edit class information form submission processing
     * @param clazz
     * @return
     */
    @RequestMapping(value="/edit",method=RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> edit(Clazz clazz){
        
        CodeMsg validate = ValidateEntityUtil.validate(clazz);
        if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
            return Result.error(validate);
        }
        if(clazz.getTeacher() == null || clazz.getTeacher().getId() == null){
            return Result.error(CodeMsg.ADMIN_CLAZZ_TEACHER_EMPTY);
        }
        if(clazz.getId() == null || clazz.getId().longValue() <= 0){
            return Result.error(CodeMsg.ADMIN_CLAZZ_NOEXIST);
        }
        if(clazzService.isExistClaName(clazz.getClaname(), clazz.getId())){
            return Result.error(CodeMsg.ADMIN_CLAZZNAME_EXIST);
        }
        Clazz clazzs = clazzService.find(clazz.getId());
        BeanUtils.copyProperties(clazz, clazzs, "id","createTime","updateTime");
        if(clazzService.save(clazzs) == null){
            return Result.error(CodeMsg.ADMIN_USE_EDIT_ERROR);
        }
        operaterLogService.add("Class Name：" + clazz.getClaname());
        return Result.success(true);
    }

    /**
     * Delete class
     * @param id
     * @return
     */
    @RequestMapping(value="/delete",method=RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> delete(@RequestParam(name="id",required=true)Long id){
        try {
            clazzService.delete(id);
        } catch (Exception e) {
            return Result.error(CodeMsg.ADMIN_USE_DELETE_ERROR);
        }
        operaterLogService.add("Delete class, class id：" + id);
        return Result.success(true);
    }
}
