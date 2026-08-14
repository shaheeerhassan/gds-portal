package com.school.dao.interfaces;

import com.school.dto.TeacherSubjectDTO;
import java.util.List;

public interface TeacherSubjectDao {
    boolean assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean isAssigned(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean isSubjectAssignedInSection(int subjectId, int sectionId, int academicYearId);
    boolean hasOtherSubjectsInSection(long teacherId, int sectionId, int academicYearId, long excludeTeacherSubjectId);
    List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId);
    TeacherSubjectDTO getTeacherSubjectById(long teacherSubjectId);
    boolean unassignTeacherSubject(long teacherSubjectId);
}
