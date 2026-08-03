package com.school.dao.interfaces;

import com.school.model.Class;
import java.util.List;

public interface ClassDao {
    boolean insertClass(Class c);
    Class getClassById(int classId);
    Class getClassByNumericLevel(int numericLevel);
    List<Class> getAllClasses();
    boolean updateClass(Class c);
    boolean deleteClass(int classId);
}
