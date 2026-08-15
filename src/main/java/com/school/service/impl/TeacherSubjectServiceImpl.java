package com.school.service.impl;

import com.school.dao.impl.TeacherSubjectDaoImpl;
import com.school.dao.interfaces.TeacherSubjectDao;
import com.school.dao.impl.SectionDaoImpl;
import com.school.dao.impl.TeacherClassDaoImpl;
import com.school.dao.interfaces.SectionDao;
import com.school.dao.interfaces.TeacherClassDao;
import com.school.dto.TeacherSubjectDTO;
import com.school.model.Section;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.service.interfaces.TeacherSubjectService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class TeacherSubjectServiceImpl implements TeacherSubjectService {

    private final TeacherSubjectDao teacherSubjectDao;
    private final TeacherClassDao teacherClassDao;
    private final SectionDao sectionDao;

    public TeacherSubjectServiceImpl() {
        teacherSubjectDao = new TeacherSubjectDaoImpl();
        teacherClassDao = new TeacherClassDaoImpl();
        sectionDao = new SectionDaoImpl();
    }

    @Override
    public void assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId) {
        validateId(teacherId);
        validateId(subjectId);
        validateId(sectionId);
        validateId(academicYearId);



        if (teacherSubjectDao.isSubjectAssignedInSection(subjectId, sectionId, academicYearId))
            throw new DuplicateResourceException("Another teacher is already assigned to teach this subject in this section.");

        if (!teacherSubjectDao.assignTeacherSubject(teacherId, subjectId, sectionId, academicYearId))
            throw new IllegalStateException("Failed to assign subject to teacher.");

        // Also add record to teacher_classes
        Section section = sectionDao.getSectionById(sectionId);
        if (section != null) {
            int classId = section.getClassId();
            if (!teacherClassDao.isAssigned(teacherId, classId, sectionId, academicYearId)) {
                teacherClassDao.assignTeacherClass(teacherId, classId, sectionId, academicYearId);
            }
        }
    }

    @Override
    public List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return teacherSubjectDao.getTeacherSubjects(teacherId, academicYearId);
    }

    @Override
    public void unassignTeacherSubject(long teacherSubjectId) {
        validateId(teacherSubjectId);

        TeacherSubjectDTO dto = teacherSubjectDao.getTeacherSubjectById(teacherSubjectId);
        if (dto == null) {
            throw new ResourceNotFoundException("Teacher-subject assignment not found.");
        }

        if (!teacherSubjectDao.unassignTeacherSubject(teacherSubjectId)) {
            throw new ResourceNotFoundException("Teacher-subject assignment not found.");
        }

        // If the teacher has no other subjects in this section, remove the teacher_classes record too
        if (!teacherSubjectDao.hasOtherSubjectsInSection(dto.getTeacherId(), dto.getSectionId(), dto.getAcademicYearId(), teacherSubjectId)) {
            teacherClassDao.unassignTeacherClass(dto.getTeacherId(), dto.getClassId(), dto.getSectionId(), dto.getAcademicYearId());
        }
    }
}
