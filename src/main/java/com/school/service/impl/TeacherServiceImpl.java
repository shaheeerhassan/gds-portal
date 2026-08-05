package com.school.service.impl;

import com.school.dao.impl.TeacherDaoImpl;
import com.school.dao.interfaces.TeacherDao;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Section;
import com.school.model.Teacher;
import com.school.model.User;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.TeacherService;
import com.school.service.interfaces.UserService;

import java.util.List;
import java.util.Map;

import static com.school.validations.ValidatorUtil.*;

public class TeacherServiceImpl implements TeacherService {

    private final TeacherDao teacherDao;
    private final UserService userService;
    private final RoleService roleService;

    public TeacherServiceImpl() {
        teacherDao = new TeacherDaoImpl();
        userService = new UserServiceImpl();
        roleService = new RoleServiceImpl();
    }

    @Override
    public Teacher createTeacher(User user, String password, Teacher teacher) {
        teacher.setFirstName(validateName(teacher.getFirstName(), "First name"));
        teacher.setLastName(validateName(teacher.getLastName(), "Last name"));
        teacher.setPhone(validatePhone(teacher.getPhone()));
        teacher.setEmployeeId(validateRequired(teacher.getEmployeeId(), "Employee ID"));

        if (teacherDao.getTeacherByEmployeeId(teacher.getEmployeeId()) != null)
            throw new DuplicateResourceException("A teacher with this employee ID already exists.");

        user.setRoleId(roleService.getRoleByName("TEACHER").getRoleId());
        user = userService.createUser(user, password);

        teacher.setUserId(user.getUserId());
        teacher.setActive(true);

        try {
            if (!teacherDao.insertTeacher(teacher)) {
                throw new IllegalStateException("Failed to create teacher.");
            }
        } catch (Exception e) {
            userService.deleteUser(user.getUserId());
            throw e;
        }

        return teacher;
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherDao.getAllTeachers();
    }

    @Override
    public List<Teacher> searchTeachersByName(String name) {
        name = validateRequired(name, "Name");
        return teacherDao.searchTeachersByName(name);
    }

    @Override
    public List<Teacher> getTeachersBySubject(int subjectId) {
        validateId(subjectId);
        return teacherDao.getAllTeachersBySubject(subjectId);
    }

    @Override
    public List<Teacher> getTeachersBySection(int sectionId, int academicYearId) {
        validateId(sectionId, academicYearId);
        return teacherDao.getTeachersBySection(sectionId, academicYearId);
    }

    @Override
    public Map<Long, List<Section>> getAllClassTeachers() {
        return teacherDao.getAllClassTeachers();
    }

    @Override
    public Teacher getTeacherByUserId(long userId) {
        validateId(userId);
        Teacher teacher = teacherDao.getTeacherByUserId(userId);
        if (teacher == null)
            throw new ResourceNotFoundException("Teacher not found.");
        return teacher;
    }

    @Override
    public Teacher getTeacherById(long teacherId) {
        validateId(teacherId);
        Teacher teacher = teacherDao.getTeacherById(teacherId);
        if (teacher == null)
            throw new ResourceNotFoundException("Teacher not found.");
        return teacher;
    }

    @Override
    public Teacher getTeacherByEmployeeId(String employeeId) {
        employeeId = validateRequired(employeeId, "Employee ID");
        Teacher teacher = teacherDao.getTeacherByEmployeeId(employeeId);
        if (teacher == null)
            throw new ResourceNotFoundException("Teacher not found.");
        return teacher;
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        email = validateEmail(email);
        Teacher teacher = teacherDao.getTeacherByEmail(email);
        if (teacher == null)
            throw new ResourceNotFoundException("Teacher not found.");
        return teacher;
    }

    @Override
    public int getTeacherCount() {
        return teacherDao.getTeacherCount();
    }

    @Override
    public void updateTeacher(Teacher teacher) {
        validateId(teacher.getTeacherId());

        Teacher existing = teacherDao.getTeacherById(teacher.getTeacherId());


        if (existing == null)
            throw new ResourceNotFoundException("Teacher not found.");

        if (teacher.getEmployeeId() == null)
            teacher.setEmployeeId(existing.getEmployeeId());

        if (teacher.getFirstName() == null)
            teacher.setFirstName(existing.getFirstName());

        if (teacher.getLastName() == null)
            teacher.setLastName(existing.getLastName());

        if (teacher.getPhone() == null)
            teacher.setPhone(existing.getPhone());

        if (teacher.getDateOfBirth() == null)
            teacher.setDateOfBirth(existing.getDateOfBirth());

        if (teacher.getHireDate() == null)
            teacher.setHireDate(existing.getHireDate());

        if (teacher.getQualification() == null)
            teacher.setQualification(existing.getQualification());

        teacher.setFirstName(validateName(teacher.getFirstName(), "First name"));
        teacher.setLastName(validateName(teacher.getLastName(), "Last name"));
        teacher.setPhone(validatePhone(teacher.getPhone()));

        if (!teacherDao.updateTeacherDetails(teacher))
            throw new ResourceNotFoundException("Teacher not found.");
    }

    @Override
    public void deactivateTeacher(long teacherId) {
        validateId(teacherId);
        if (!teacherDao.deleteTeacher(teacherId))
            throw new ResourceNotFoundException("Teacher not found.");
    }
}
