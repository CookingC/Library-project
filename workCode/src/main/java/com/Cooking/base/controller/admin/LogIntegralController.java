package com.Cooking.base.controller.admin;

import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.PageBean;
import com.Cooking.base.bean.Result;
import com.Cooking.base.entity.admin.LogIntegral;
import com.Cooking.base.entity.admin.Student;
import com.Cooking.base.service.admin.LogIntegralService;
import com.Cooking.base.service.admin.StudentService;
import com.Cooking.base.util.SessionUtil;
import com.Cooking.base.util.ValidateEntityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("/log")
@Controller
public class LogIntegralController {
    @Autowired
    private LogIntegralService logIntegralService;
    @Autowired
    private StudentService studentService;
    @RequestMapping(value="/list")
    public String list(Model model, Student student, PageBean<Student> pageBean){
        model.addAttribute("title", "积分列表");
        if (student.getUser()!=null){
            model.addAttribute("name", student.getUser().getNickName());
        }
        model.addAttribute("pageBean", studentService.findByName(student, pageBean));
        return "admin/logIntegral/list";
    }


    /**
     * 
     * @param
     * @return
     */
    @RequestMapping(value="/logs")
    @ResponseBody
    public Result logList(LogIntegral logIntegral, PageBean<LogIntegral> pageBean){
        List<LogIntegral> byStudentId = logIntegralService.findByStudentId(logIntegral.getStudent().getId());
        return Result.success(byStudentId);
    }
    /**
     * check current student history 
     * @param
     * @return
     */
    @RequestMapping(value="/stuList")

    public String stuList(LogIntegral logIntegral, PageBean<LogIntegral> pageBean,Model model){
        model.addAttribute("title", "我的信用积分");
        Student byLoginUser = studentService.findByLoginUser();
        logIntegral.setStudent(byLoginUser);
        model.addAttribute("pageBean",logIntegralService.findList(logIntegral,pageBean));
        return "admin/logIntegral/stuList";
    }
    @RequestMapping(value="/add",method= RequestMethod.GET)
    public String add(Model model,Long id){

        return "admin/logIntegral/add";
    }

    @RequestMapping(value="delete",method= RequestMethod.POST)
    @ResponseBody
    public Result delete(long id){
        try {
            logIntegralService.delete(id);
        } catch (Exception e) {

            return Result.error(CodeMsg.ADMIN_ROLE_DELETE_ERROR);
        }

        return Result.success(true);
    }


    /**
     * 
     * @param
     * @return
     */
    @RequestMapping(value="/add",method=RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> add(LogIntegral logIntegral){
        CodeMsg validate = ValidateEntityUtil.validate(logIntegral);
        if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
            return Result.error(validate);
        }

        if (logIntegral.getStudent().getId()==null|| logIntegral.getStudent().getId()==0){
            return Result.error(CodeMsg.DATA_ERROR);
        }
        Student byID = studentService.findByID(logIntegral.getStudent().getId());
        if (logIntegral.getGrade()>byID.getStudentCredits()){
            
            return Result.error(CodeMsg.ADMIN_LOFINTEGRAL_CREDITS);
        }

          logIntegralService.save(logIntegral);

        return Result.success(true);
    }
}
