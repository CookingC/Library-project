package com.Cooking.base.dao.admin;

import com.Cooking.base.entity.admin.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherDao extends JpaRepository<Teacher, Long> {
}
