package com.Cooking.base.controller.admin;

import com.Cooking.base.bean.Result;
import com.Cooking.base.dao.admin.SeatStatisticsDao;
import com.Cooking.base.entity.admin.Statistics;
import com.Cooking.base.service.admin.SeatStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/statistic")
public class SeatStatisticsController {

    @Autowired
    private SeatStatisticsService seatStatisticsService;

    /**
     * 进入占座统计页面
     * @return
     */
    @RequestMapping(value = "/occupied")
    public String list() {
        return "admin/statistics/occupied_statistics";
    }


    /**
     * 按照月份统计各个类型中的占座人数
     *
     * @return
     */
    @RequestMapping("/statisticsMap")
    @ResponseBody
    public Result<Map<String, List<Integer>>> statisticsList() {
        Map<String, List<Integer>> stringListMap = seatStatisticsService.seatStatisticsList();


        return Result.success(stringListMap);
    }
}