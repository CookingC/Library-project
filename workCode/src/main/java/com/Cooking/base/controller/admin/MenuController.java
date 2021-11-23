package com.Cooking.base.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.Result;
import com.Cooking.base.entity.admin.Menu;
import com.Cooking.base.service.admin.MenuService;
import com.Cooking.base.service.admin.OperaterLogService;
import com.Cooking.base.util.MenuUtil;
import com.Cooking.base.util.ValidateEntityUtil;

/**
 * 
 * @author Administrator
 *
 */
@RequestMapping("/menu")
@Controller
public class MenuController {

	@Autowired
	private MenuService menuService;
	
	@Autowired
	private OperaterLogService operaterLogService;
	
	/**
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/list")
	public String list(Model model){
		List<Menu> findAll = menuService.findAll();
		model.addAttribute("title","Menu list");
		model.addAttribute("topMenus",MenuUtil.getTopMenus(findAll));
		model.addAttribute("secondMenus",MenuUtil.getSecondMenus(findAll));
		model.addAttribute("thirdMenus",MenuUtil.getThirdMenus(findAll));
		return "admin/menu/list";
	}
	
	/**
	 * Menu add page
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.GET)
	public String add(Model model){
		List<Menu> findAll = menuService.findAll();
		model.addAttribute("title","Menu list");
		model.addAttribute("topMenus",MenuUtil.getTopMenus(findAll));
		model.addAttribute("secondMenus",MenuUtil.getSecondMenus(findAll));
		return "admin/menu/add";
	}
	
	/**
	 * Menu add submit form processing
	 * @param menu
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST)
	@ResponseBody
	public Result<Boolean> add(Menu menu){
		if(menu == null){
			Result.error(CodeMsg.DATA_ERROR);
		}
		CodeMsg validate = ValidateEntityUtil.validate(menu);
		if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
			return Result.error(validate);
		}
		if(menu.getParent() != null){
			if(menu.getParent().getId() == null){
				menu.setParent(null);
			}
		}
		if(menuService.save(menu) == null){
			Result.error(CodeMsg.ADMIN_MENU_ADD_ERROR);
		}
		operaterLogService.add("Add menu information【" + menu + "】");
		return Result.success(true);
	}
	
	/**
	 * 菜单编辑页面
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/edit",method=RequestMethod.GET)
	public String eidt(Model model,@RequestParam(name="id",required=true)Long id){
		List<Menu> findAll = menuService.findAll();
		model.addAttribute("title","Menu list");
		model.addAttribute("topMenus",MenuUtil.getTopMenus(findAll));
		model.addAttribute("secondMenus",MenuUtil.getSecondMenus(findAll));
		model.addAttribute("menu",menuService.find(id));
		return "admin/menu/edit";
	}
	
	/**
	 * Menu edit page form submission processing
	 * @param
	 * @param menu
	 * @return
	 */
	@RequestMapping(value="/edit",method=RequestMethod.POST)
	@ResponseBody
	public Result<Boolean> edit(Menu menu){
		if(menu == null){
			Result.error(CodeMsg.DATA_ERROR);
		}
		if(menu.getId() == null){
			Result.error(CodeMsg.ADMIN_MENU_ID_EMPTY);
		}
		//Use unified verification example method to verify legality
		CodeMsg validate = ValidateEntityUtil.validate(menu);
		if(validate.getCode() != CodeMsg.SUCCESS.getCode()){
			return Result.error(validate);
		}
		if(menu.getParent() != null){
			if(menu.getParent().getId() == null){
				menu.setParent(null);
			}
		}
		Menu existMenu = menuService.find(menu.getId());
		if(existMenu == null){
			Result.error(CodeMsg.ADMIN_MENU_ID_ERROR);
		}
		//Indicates that the verification is passed, and start to add the database
		existMenu.setIcon(menu.getIcon());
		existMenu.setName(menu.getName());
		existMenu.setParent(menu.getParent());
		existMenu.setSort(menu.getSort());
		existMenu.setUrl(menu.getUrl());
		existMenu.setButton(menu.isButton());
		existMenu.setShow(menu.isShow());
		if(menuService.save(existMenu) == null){
			Result.error(CodeMsg.ADMIN_MENU_ADD_ERROR);
		}
		operaterLogService.add("Edit menu information【" + existMenu + "】");
		return Result.success(true);
	}
	
	/**
	 * Delete menu information
	 * @param request
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/delete",method=RequestMethod.POST)
	@ResponseBody
	public Result<Boolean> delete(@RequestParam(name="id",required=true)Long id){
		try {
			menuService.delete(id);
		} catch (Exception e) {
			return Result.error(CodeMsg.ADMIN_MENU_DELETE_ERROR);
		}
		operaterLogService.add("Delete menu information, menu ID【" + id + "】");
		return Result.success(true);
	}
}
