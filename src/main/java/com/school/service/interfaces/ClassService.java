package com.school.service.interfaces;

import com.school.model.Class;

import java.util.List;

public interface ClassService {
    Class createClass(Class c);
    Class getClassById(int classId);
    Class getClassByNumericLevel(int numericLevel);
    List<Class> getAllClasses();
    void updateClass(Class c);
    void deleteClass(int classId);
}
