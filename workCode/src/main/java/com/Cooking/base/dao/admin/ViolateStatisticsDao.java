package com.Cooking.base.dao.admin;


import com.Cooking.base.entity.admin.LogIntegral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.Id;
import java.util.List;

public interface ViolateStatisticsDao extends JpaRepository<LogIntegral,Long> {


    /**
     * Query data on the number of offenders this year
     * @return
     */
    @Query(value = "SELECT\n" +
            "\tMONTH(create_time),\n" +
            "\tcount(grade) count \n" +
            "FROM\n" +
            "\tylrc_log_integral \n" +
            "\tWHERE\n" +
            "\t\tyear(create_time) = YEAR(now())\n" +
            "GROUP BY\n" +
            "\tMONTH(create_time)"   ,nativeQuery = true)
    List<Object> logIntegralCountByDate();







}
