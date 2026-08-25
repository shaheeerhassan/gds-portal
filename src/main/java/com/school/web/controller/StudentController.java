package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Student;
import com.school.service.impl.ParentServiceImpl;
import com.school.service.impl.StudentServiceImpl;
import com.school.service.interfaces.ParentService;
import com.school.service.interfaces.StudentService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.OwnershipGuard;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreateStudentRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/students/*")
public class StudentController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final StudentService studentService;
    private final ParentService parentService;

    public StudentController() {
        this.studentService = new StudentServiceImpl();
        this.parentService = new ParentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/me": {
                long userId = AuthContext.getUserId(req);
                Student student = studentService.getStudentByUserId(userId);
                RoleGuard.requireUserOrRole(req, student.getUserId(), ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, student);
                return;
            }
            case "/":
            case "": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, studentService.getAllStudents());
                return;
            }
            case "/count": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, studentService.getActiveStudentCount());
                return;
            }
            case "/directory": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                String q = req.getParameter("q");
                String academicYearIdStr = req.getParameter("academicYearId");
                String classIdStr = req.getParameter("classId");
                String sectionIdStr = req.getParameter("sectionId");
                String enrolledStr = req.getParameter("enrolled");
                String pageStr = req.getParameter("page");
                String sizeStr = req.getParameter("size");

                Integer academicYearId = (academicYearIdStr != null && !academicYearIdStr.isEmpty()) ? Integer.parseInt(academicYearIdStr) : null;
                Integer classId = (classIdStr != null && !classIdStr.isEmpty()) ? Integer.parseInt(classIdStr) : null;
                Integer sectionId = (sectionIdStr != null && !sectionIdStr.isEmpty()) ? Integer.parseInt(sectionIdStr) : null;
                Boolean enrolled = (enrolledStr != null && !enrolledStr.isEmpty()) ? Boolean.parseBoolean(enrolledStr) : null;
                int page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 0;
                int size = (sizeStr != null && !sizeStr.isEmpty()) ? Integer.parseInt(sizeStr) : 20;

                writeJson(resp, studentService.getStudentDirectory(q, academicYearId, classId, sectionId, enrolled, page, size));
                return;
            }
            default:
                if (path.startsWith("/class/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    String[] parts = path.substring("/class/".length()).split("/");
                    if (parts.length != 2)
                        throw new ValidationException("Expected /class/{classId}/{academicYearId}.");
                    writeJson(resp, studentService.getStudentsByClass(parseInt(parts[0]), parseInt(parts[1])));
                    return;
                }
                if (path.startsWith("/section/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, studentService.getStudentsBySection(parseInt(path.substring("/section/".length()))));
                    return;
                }
                if (path.startsWith("/parent/")) {
                    long parentId = parseLong(path.substring("/parent/".length()));
                    if (AuthContext.hasRole(req, ROLE_PARENT)) {
                        long ownParentId = parentService.getParentByUserId(AuthContext.getUserId(req)).getParentId();
                        if (ownParentId != parentId)
                            throw new UnauthorizedException("You can only view students linked to you.");
                    } else {
                        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    }
                    writeJson(resp, studentService.getStudentsByParentId(parentId));
                    return;
                }
                if (path.startsWith("/search/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, studentService.searchStudentsByName(path.substring("/search/".length())));
                    return;
                }
                if (path.startsWith("/registration/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, studentService.getStudentByRegistrationNumber(path.substring("/registration/".length())));
                    return;
                }
                if (path.startsWith("/user/")) {
                    long userId = parseLong(path.substring("/user/".length()));
                    Student student = studentService.getStudentByUserId(userId);
                    OwnershipGuard.requireSelfOrRolesOrLinkedParent(req, userId, student.getStudentId(),
                            parentService, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, student);
                    return;
                }
                if (path.startsWith("/")) {
                    long studentId = parseLong(path.substring(1));
                    Student student = studentService.getStudentByStudentId(studentId);
                    OwnershipGuard.requireSelfOrRolesOrLinkedParent(req, student.getUserId(), studentId,
                            parentService, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, student);
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreateStudentRequest request = readBody(req, CreateStudentRequest.class);
        if (request == null || request.getStudent() == null)
            throw new ValidationException("Request body with student details is required.");

        Student created = studentService.createStudent(request.toUser(), request.getPassword(), request.getStudent());
        writeJson(resp, created, "Student created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_STUDENT);
        long studentId = parseLong(pathInfo(req).substring(1));
        Student student = readBody(req, Student.class);
        if (student == null)
            throw new ValidationException("Request body is required.");
        student.setStudentId(studentId);
        studentService.updateStudent(student);
        writeStatusMessage(resp, "Student updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long studentId = parseLong(pathInfo(req).substring(1));
        studentService.deactivateStudent(studentId);
        writeStatusMessage(resp, "Student deactivated.");
    }
}
