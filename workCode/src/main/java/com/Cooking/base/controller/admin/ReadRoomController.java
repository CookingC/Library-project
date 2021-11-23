package com.Cooking.base.controller.admin;

import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.PageBean;
import com.Cooking.base.bean.Result;
import com.Cooking.base.entity.admin.ReadingRoom;
import com.Cooking.base.entity.admin.ReadingType;
import com.Cooking.base.service.admin.ReadRoomService;
import com.Cooking.base.service.admin.ReadRoomTypeService;
import com.Cooking.base.util.ValidateEntityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/***
 * Admin
 */
@Controller
@RequestMapping(value = "/readRoom")
public class ReadRoomController {

    @Autowired
    private ReadRoomService readRoomService;

    @Autowired
    private ReadRoomTypeService readRoomTypeService;
    @RequestMapping(value="/list")
    public String list(Model model, ReadingRoom readingRoom, PageBean<ReadingRoom> pageBean){
        model.addAttribute("title", "阅览室列表");
        model.addAttribute("name",readingRoom.getName());
        model.addAttribute("pageBean", readRoomService.findAll(readingRoom, pageBean));
        return "admin/readroom/list";
    }

    /***
     * Delete by id
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> delete(Long id){
        try {
            readRoomService.deleteId(id);
        }catch (Exception e){
            return Result.error(CodeMsg.ADMIN_READING_DELETE);
        }
        return Result.success(true);
    }

    /***
     * 
     * @return
     */
    @RequestMapping(value = "/add")
    public  String tzSave(Model model)
    {
        List<ReadingType> all = readRoomTypeService.findAll();
        model.addAttribute("readType",all);
        return "/admin/readroom/add";
    }

    /***
     * New room
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean>  save(ReadingRoom readingRoom){
        
        CodeMsg validate = ValidateEntityUtil.validate(readingRoom);
        if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
            return Result.error(validate);
        }
        
        if(readRoomService.isExistName(readingRoom.getName(),readingRoom.getReadingType().getId())){
            return  Result.error(CodeMsg.ADMIN_READING_ISEXIST);
        }
        readRoomService.save(readingRoom);
        return Result.success(true);
    }

    /***
     * To edit page
     * @return
     */
    @RequestMapping(value = "/edit")
    public  String update(@RequestParam(name="id",required=true)Long id, Model model){
        ReadingRoom byId = readRoomService.findById(id);
        model.addAttribute("read",byId);
        model.addAttribute("readType",readRoomTypeService.findAll());
        return "/admin/readroom/edit";
    }

    /***
     * edit room
     * @return
     */
    @RequestMapping(value = "/edit",method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean>  edit(ReadingRoom readingRoom){
        readingRoom.setCreateTime(new Date());
        CodeMsg validate = ValidateEntityUtil.validate(readingRoom);
        if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
            return Result.error(validate);
        }
        if(readRoomService.isExistName(readingRoom.getId(),readingRoom.getName(),readingRoom.getReadingType().getId())){
            return Result.error(CodeMsg.ADMIN_READING_ISEXIST);
        }
        ReadingRoom byId = readRoomService.findById(readingRoom.getId());
        BeanUtils.copyProperties(readingRoom,byId, "id","createTime","updateTime");
        readRoomService.save(byId);
        return Result.success(true);
    }

}
