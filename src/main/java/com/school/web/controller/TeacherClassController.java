package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Teacher;
import com.school.service.impl.TeacherClassServiceImpl;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.interfaces.TeacherClassService;
import com.school.service.interfaces.TeacherService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/teacher-classes/*")
public class TeacherClassController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final TeacherClassService teacherClassService;
    private final TeacherService teacherService;

    public TeacherClassController() {
        this.teacherClassService = new TeacherClassServiceImpl();
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
            writeJson(resp, teacherClassService.getTeacherClasses(teacherId, parseInt(parts[1])));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        AssignRequest request = readBody(req, AssignRequest.class);
        if (request == null || request.getTeacherId() == 0 || request.getClassId() == 0)
            throw new ValidationException("teacherId and classId are required.");
        teacherClassService.assignTeacherClass(
                request.getTeacherId(), request.getClassId(), request.getSectionId(), request.getAcademicYearId());
        writeStatusMessage(resp, "Teacher class assigned.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        long teacherClassId = parseLong(pathInfo(req).substring(1));
        teacherClassService.unassignTeacherClass(teacherClassId);
        writeStatusMessage(resp, "Teacher class unassigned.");
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
        private int classId;
        private int sectionId;
        private int academicYearId;

        public long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(long teacherId) {
            this.teacherId = teacherId;
        }

        public int getClassId() {
            return classId;
        }

        public void setClassId(int classId) {
            this.classId = classId;
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
