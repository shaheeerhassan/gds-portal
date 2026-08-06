package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Teacher;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.impl.TeacherSubjectServiceImpl;
import com.school.service.interfaces.TeacherService;
import com.school.service.interfaces.TeacherSubjectService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/teacher-subjects/*")
public class TeacherSubjectController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final TeacherSubjectService teacherSubjectService;
    private final TeacherService teacherService;

    public TeacherSubjectController() {
        this.teacherSubjectService = new TeacherSubjectServiceImpl();
        this.teacherService = new TeacherServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/teacher/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/teacher/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /teacher/{teacherId}/{academicYearId}.");
            long teacherId = parseLong(parts[0]);
            requireTeacherSelfAccess(req, teacherId);
            writeJson(resp, teacherSubjectService.getTeacherSubjects(teacherId, parseInt(parts[1])));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        AssignRequest request = readBody(req, AssignRequest.class);
        if (request == null || request.getTeacherId() == 0 || request.getSubjectId() == 0)
            throw new ValidationException("teacherId and subjectId are required.");
        teacherSubjectService.assignTeacherSubject(
                request.getTeacherId(), request.getSubjectId(), request.getSectionId(), request.getAcademicYearId());
        writeStatusMessage(resp, "Teacher subject assigned.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        long teacherSubjectId = parseLong(pathInfo(req).substring(1));
        teacherSubjectService.unassignTeacherSubject(teacherSubjectId);
        writeStatusMessage(resp, "Teacher subject unassigned.");
    }

    private void requireTeacherSelfAccess(HttpServletRequest req, long teacherId) {
        if (AuthContext.hasRole(req, ROLE_TEACHER)) {
            Teacher target = teacherService.getTeacherById(teacherId);
            if (target.getUserId() != AuthContext.getUserId(req))
                throw new UnauthorizedException("You can only access your own data.");
        }
    }

    public static class AssignRequest {
        private long teacherId;
        private int subjectId;
        private int sectionId;
        private int academicYearId;

        public long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(long teacherId) {
            this.teacherId = teacherId;
        }

        public int getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(int subjectId) {
            this.subjectId = subjectId;
        }

        public int getSectionId() {
            return sectionId;
        }

        public void setSectionId(int sectionId) {
            this.sectionId = sectionId;
        }

        public int getAcademicYearId() {
            return academicYearId;
        }

        public void setAcademicYearId(int academicYearId) {
            this.academicYearId = academicYearId;
        }
    }
}
