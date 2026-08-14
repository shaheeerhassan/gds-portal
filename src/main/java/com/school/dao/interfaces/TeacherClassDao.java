package com.school.dao.interfaces;

import com.school.dto.TeacherClassDTO;
import java.util.List;

public interface TeacherClassDao {
    boolean assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);
    boolean isAssigned(long teacherId, int classId, int sectionId, int academicYearId);
    boolean unassignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);
    List<TeacherClassDTO> getTeacherClasses(long teacherId, int academicYearId);
    boolean unassignTeacherClass(long teacherClassId);
}
