    package com.school.service.impl;

    import com.school.dao.impl.ExaminationDaoImpl;
    import com.school.dao.interfaces.ExaminationDao;
    import com.school.exceptions.ResourceNotFoundException;
    import com.school.exceptions.ValidationException;
    import com.school.model.Examination;
    import com.school.service.interfaces.ExaminationService;

    import java.util.List;

    import static com.school.validations.ValidatorUtil.*;

    public class ExaminationServiceImpl implements ExaminationService {

        private final ExaminationDao examinationDao;

        public ExaminationServiceImpl() {
            examinationDao = new ExaminationDaoImpl();
        }

        @Override
        public Examination createExamination(Examination examination) {
            validateExamination(examination);

            if (!examinationDao.insertExamination(examination))
                throw new IllegalStateException("Failed to create examination.");

            return examination;
        }

        @Override
        public Examination getExaminationById(long examinationId) {
            validateId(examinationId);
            Examination examination = examinationDao.getExaminationById(examinationId);
            if (examination == null)
                throw new ResourceNotFoundException("Examination not found.");
            return examination;
        }

        @Override
        public List<Examination> getExaminationsBySection(int sectionId, int academicYearId) {
            validateId(sectionId);
            validateId(academicYearId);
            return examinationDao.getExaminationsBySection(sectionId, academicYearId);
        }

        @Override
        public List<Examination> getSpecificExaminationsBySection(int sectionId, String examName, int academicYearId) {
            validateId(sectionId);
            validateId(academicYearId);
            examName = validateRequired(examName, "Exam name");
            return examinationDao.getSpecificExaminationsBySection(sectionId, examName, academicYearId);
        }

        @Override
        public List<Examination> getExaminationsByTeacher(long teacherId, int academicYearId) {
            validateId(teacherId);
            validateId(academicYearId);
            return examinationDao.getExaminationsByTeacher(teacherId, academicYearId);
        }

        @Override
        public List<Examination> getSpecificExaminationsByTeacher(long teacherId, String examName, int academicYearId) {
            validateId(teacherId);
            validateId(academicYearId);
            examName = validateRequired(examName, "Exam name");
            return examinationDao.getSpecificExaminationsByTeacher(teacherId, examName, academicYearId);
        }

        @Override
        public List<Examination> getExaminationsByTeacherAndSection(long teacherId, int sectionId, int academicYearId) {
            validateId(teacherId);
            validateId(sectionId);
            validateId(academicYearId);
            return examinationDao.getExaminationsByTeacherAndSection(teacherId, sectionId, academicYearId);
        }

        @Override
        public List<Examination> getSpecificExaminationsByTeacherAndSection(long teacherId, int sectionId, String examName, int academicYearId) {
            validateId(teacherId);
            validateId(sectionId);
            validateId(academicYearId);
            examName = validateRequired(examName, "Exam name");
            return examinationDao.getSpecificExaminationsByTeacherAndSection(teacherId, sectionId, examName, academicYearId);
        }

        @Override
        public void updateExamination(Examination examination) {
            validateId(examination.getExaminationId());
            validateExamination(examination);

            if (!examinationDao.updateExamination(examination))
                throw new ResourceNotFoundException("Examination not found.");
        }

        @Override
        public void updateExaminationStatus(long examinationId, Examination.Status status) {
            validateId(examinationId);
            if (status == null)
                throw new ValidationException("Examination status is required.");

            if (!examinationDao.updateExaminationStatus(examinationId, status))
                throw new ResourceNotFoundException("Examination not found.");
        }

        private void validateExamination(Examination examination) {
            examination.setExamName(validateRequired(examination.getExamName(), "Exam name"));
            validateId(examination.getSubjectId());
            validateId(examination.getSectionId());
            validateId(examination.getAcademicYearId());
            if (examination.getMaxMarks() <= 0)
                throw new ValidationException("Max marks must be a positive number.");
            if (examination.getPassingMarks() != null && examination.getPassingMarks() > examination.getMaxMarks())
                throw new ValidationException("Passing marks cannot exceed max marks.");
            if (examination.getStatus() == null)
                throw new ValidationException("Examination status is required.");
        }
    }
