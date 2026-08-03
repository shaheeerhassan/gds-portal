package com.school.dao.interfaces;

import com.school.model.Section;

import java.util.List;

public interface TeacherClassDao {
    boolean assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);
    List<Section> getTeacherClasses(long teacherId, int academicYearId);
    boolean unassignTeacherClass(long teacherClassId);
}
