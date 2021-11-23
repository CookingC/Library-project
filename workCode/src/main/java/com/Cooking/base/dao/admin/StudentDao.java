package com.Cooking.base.dao.admin;

import com.Cooking.base.entity.admin.Student;
import com.Cooking.base.entity.admin.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDao extends JpaRepository<Student, Long> {

    Student findByUser_Id(Long userId);

}
