package com.school.service.interfaces;

import com.school.model.Section;

import java.util.List;

public interface TeacherClassService {
    void assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);
    List<Section> getTeacherClasses(long teacherId, int academicYearId);
    void unassignTeacherClass(long teacherClassId);
}
