package com.school.service.interfaces;

import com.school.dto.TeacherClassDTO;

import java.util.List;

public interface TeacherClassService {
    void assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);
    List<TeacherClassDTO> getTeacherClasses(long teacherId, int academicYearId);
    void unassignTeacherClass(long teacherClassId);
}
