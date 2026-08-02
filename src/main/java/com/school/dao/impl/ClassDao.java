package com.school.dao.impl;

import com.school.model.Class;
import java.util.List;

public interface ClassDao {
    boolean insertClass(Class c);
    Class getClassById(int classId);
    List<Class> getAllClasses();
    boolean updateClass(Class c);
    boolean deleteClass(int classId);
}
