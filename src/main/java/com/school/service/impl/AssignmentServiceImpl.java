package com.school.service.impl;

import com.school.dao.impl.AssignmentDaoImpl;
import com.school.dao.interfaces.AssignmentDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Assignment;
import com.school.service.interfaces.AssignmentService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentDao assignmentDao;

    public AssignmentServiceImpl() {
        assignmentDao = new AssignmentDaoImpl();
    }

    @Override
    public Assignment createAssignment(Assignment assignment) {
        validateAssignment(assignment);

        if (!assignmentDao.insertAssignment(assignment))
            throw new IllegalStateException("Failed to create assignment.");

        return assignment;
    }

    @Override
    public Assignment getAssignmentById(long assignmentId) {
        validateId(assignmentId);
        Assignment assignment = assignmentDao.getAssignmentById(assignmentId);
        if (assignment == null)
            throw new ResourceNotFoundException("Assignment not found.");
        return assignment;
    }

    @Override
    public List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId) {
        validateId(sectionId);
        validateId(academicYearId);
        return assignmentDao.getAssignmentsBySection(sectionId, academicYearId);
    }

    @Override
    public List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return assignmentDao.getAssignmentsByTeacher(teacherId, academicYearId);
    }

    @Override
    public void updateAssignment(Assignment assignment) {
        validateId(assignment.getAssignmentId());
        validateAssignment(assignment);

        if (!assignmentDao.updateAssignment(assignment))
            throw new ResourceNotFoundException("Assignment not found.");
    }

    @Override
    public void publishAssignment(long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        assignment.setStatus(Assignment.Status.PUBLISHED);
        if (!assignmentDao.updateAssignment(assignment))
            throw new IllegalStateException("Failed to publish assignment.");
    }

    @Override
    public void deleteAssignment(long assignmentId) {
        validateId(assignmentId);
        if (!assignmentDao.deleteAssignment(assignmentId))
            throw new ResourceNotFoundException("Assignment not found.");
    }

    private void validateAssignment(Assignment assignment) {
        validateId(assignment.getTeacherId());
        validateId(assignment.getSubjectId());
        validateId(assignment.getSectionId());
        assignment.setTitle(validateRequired(assignment.getTitle(), "Title"));
        if (assignment.getMaxMarks() <= 0)
            throw new ValidationException("Max marks must be a positive number.");
        if (assignment.getDeadline() == null)
            throw new ValidationException("Deadline is required.");
    }
}
