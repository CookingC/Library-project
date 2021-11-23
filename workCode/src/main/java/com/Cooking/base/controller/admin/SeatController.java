package com.Cooking.base.controller.admin;


import com.Cooking.base.bean.CodeMsg;
import com.Cooking.base.bean.PageBean;
import com.Cooking.base.bean.Result;
import com.Cooking.base.dto.SeatDTO;
import com.Cooking.base.entity.admin.*;
import com.Cooking.base.service.admin.ReadRoomService;
import com.Cooking.base.service.admin.SeatOrderService;
import com.Cooking.base.service.admin.SeatService;
import com.Cooking.base.service.admin.StudentService;
import com.Cooking.base.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Autowired
    private ReadRoomService readRoomService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private SeatOrderService seatOrderService;



    @RequestMapping(value = "/list")
    public String list(Model model, ReadingRoom readingRoom, PageBean<SeatDTO> pageBean) {

        model.addAttribute("title", "座位管理");
        model.addAttribute("name", readingRoom.getName());
        model.addAttribute("pageBean", seatService.findList(readingRoom, pageBean));
        return "admin/seat/list";
    }

    /**
     * 跳转设置座位可用页面
     * @param model
     * @param id
     * @return
     */
    @RequestMapping(value = "/able", method = RequestMethod.GET)
    public String ableSeat(Model model,@RequestParam(name="id",required=true)Long id) {
        model.addAttribute("reading", readRoomService.findById(id));
        List<Seat> seats = seatService.findByReadingRoomId(id);
        model.addAttribute("seats", seats);
        return "admin/seat/able";
    }

    @RequestMapping(value = "/able", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> ableSeat(@RequestParam(name = "id",required = true) Long id,
                                    @RequestParam(name = "status",required = true) int status) {


        Seat seat = seatService.find(id);
        if (Objects.isNull(seat)) {
            Result.error(CodeMsg.ADMIN_SEAT_NO_EXIST);
        }
        seat.setStatus(status);
        seatService.save(seat);

        return Result.success(true);
    }


    /**
     * 跳转选座页面
     * @param model
     * @param date
     * @param timeCode
     * @param id
     * @return
     */
    @RequestMapping(value = "/choose", method = RequestMethod.GET)
    public String choose(Model model,
                         @RequestParam(name = "date",required = true) Long date,
                         @RequestParam(name = "timeCode",required = true) int timeCode,
                         @RequestParam(name="id",required=true)Long id) {


        if (Objects.isNull(date)) {
            return "error/500";
        }

        if (Objects.isNull(timeCode)) {
            return "error/500";
        }

        model.addAttribute("title", "选座管理");
        model.addAttribute("date_time", DateUtil.millisecondToFormatDate(date));
        model.addAttribute("timeCode", timeCode);
        model.addAttribute("reading", readRoomService.findById(id));

        List<Seat> seats = seatService.findByReadingRoomId(id);
        List<Long> disableSeats = seatService.findDisableSeat(id, DateUtil.millisecondToDate(date), timeCode);
        if (!disableSeats.isEmpty()) { //给已经占座的状态赋值2
            seats = seats.stream().map(o -> {
                Seat seat = o;
                for (Long disableSeat : disableSeats) {
                    if (seat.getId() == disableSeat) {
                        seat.setStatus(2);
                    }
                }
                return seat;
            }).collect(Collectors.toList());
        }

        model.addAttribute("seats", seats);

        return "admin/seat/choose";
    }

    @RequestMapping(value = "/choose", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> choose(@RequestParam(name = "date",required = true) Long date,
                                  @RequestParam(name = "timeCode",required = true) int timeCode,
                                  @RequestParam(name="seatId",required=true) Long seatId) {

        Student student = studentService.findByLoginUser();

        if (Objects.isNull(student) || Objects.isNull(student.getId())) {// 没拿到studentId 说明不是学生登录
            return Result.error(CodeMsg.ADMIN_SEAT_STUDENT_ERROR);
        }

        Seat seat = seatService.find(seatId);
        Integer readingRoomIntegral = seat.getReadingRoom().getReadingType().getIntegral();

        if (student.getStudentCredits() < readingRoomIntegral) {// 判断学生的积分是否符合
            return Result.error(CodeMsg.ADMIN_SEAT_STUDENT_INTEGRAL_ERROR);
        }


        long currentDay = DateUtil.day(new Date()).getTime();//当天的时间戳

        long day = DateUtil.day(DateUtil.millisecondToDate(date)).getTime(); //获取预约时间当天的时间戳

        if (day < currentDay) { //传过来的预约时间小于当前时间
            return Result.error(CodeMsg.ADMIN_SEAT_ORDER_DATE_ERROR);
        }

        int pm = DateUtil.isPM(); //判断当前时间是否为下午，0为上午 1为下午

        if (day == currentDay) {
            if (pm == 1) {
                if (TimeEnum.AM.getCode() == timeCode) { //当前时间是下午 但是传过来的时间去预约上午的
                    return Result.error(CodeMsg.ADMIN_SEAT_ORDER_DATE_ERROR);
                }
            }
        } //判断预约时间是否正确

        List<Long> disableSeats = seatService.findDisableSeat(seat.getReadingRoom().getId(), DateUtil.millisecondToDate(date), timeCode);//拿到传过来的时间段内已占座的座位ID

        for (Long disableSeat : disableSeats) { //判断该座位是否被已预约
            if (disableSeat == seatId) {
                return Result.error(CodeMsg.ADMIN_SEAT_ORDER_ADD_EXIST);
            }
        }

        SeatOrder stuIsOrdered = seatService.stuIsOrdered(student.getId(), currentDay);
        if (Objects.nonNull(stuIsOrdered)) {
            return Result.error(CodeMsg.ADMIN_SEAT_ORDER_STUDENT_EXIST);
        }

        SeatOrder seatOrder = new SeatOrder();
        seatOrder.setReadingRoom(seat.getReadingRoom());
        seatOrder.setStudent(student);
        seatOrder.setSeat(seat);
        seatOrder.setSubscribeTime(DateUtil.millisecondToDate(date));
        seatOrder.setTimeCode(timeCode);

        seatOrderService.add(seatOrder);

        return Result.success(true);
    }



    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> add(@RequestParam(name = "id", required = true) Long id) {

        ReadingRoom readingRoom = readRoomService.findById(id);

        if (seatService.isExistReadingRoomId(id)) {
            return Result.error(CodeMsg.ADMIN_SEAT_EXIST);
        }

        List<Seat> seats = new ArrayList<>();
        for (int x = 1; x <= readingRoom.getLie(); x++) {
            for (int y = 1; y <= readingRoom.getRow(); y++) {
                Seat seat = new Seat();
                seat.setReadingRoom(readingRoom);
                seat.setXAxis(x);
                seat.setYAxis(y);
                seats.add(seat);
            }
        }
        if (seatService.saveAll(seats).isEmpty()) {
            return Result.error(CodeMsg.ADMIN_SEAT_ADD_ERROR);
        }
        return Result.success(true);
    }

    @RequestMapping(value = "/stu/order", method = RequestMethod.GET)
    public String studentOrder(Model model,SeatOrder seatOrder,PageBean<SeatOrder> pageBean) {

        model.addAttribute("title", "座位预约记录");
        model.addAttribute("pageBean", seatOrderService.findList(seatOrder, pageBean));
        return "admin/seat/order";
    }

    @RequestMapping(value="/delete",method=RequestMethod.POST)
    @ResponseBody
    public Result<Boolean> delete(@RequestParam(name="id",required=true)Long id){
        try {
            Student student = studentService.findByLoginUser();
            if (Objects.isNull(student) || Objects.isNull(student.getId())) {// 没拿到studentId 说明不是学生登录
                return Result.error(CodeMsg.ADMIN_SEAT_ORDER_DELETE_ERROR);
            }
            long currentDay = DateUtil.day(new Date()).getTime();
            SeatOrder stuIsOrdered = seatService.stuIsOrdered(student.getId(), currentDay);
            if (Objects.isNull(stuIsOrdered)) {
                return Result.error(CodeMsg.ADMIN_SEAT_ORDER_ILLEGALITY_DELETE);
            }
            seatOrderService.delete(id);
        } catch (Exception e) {
            return Result.error(CodeMsg.ADMIN_USE_DELETE_ERROR);
        }
        return Result.success(true);
    }

}
