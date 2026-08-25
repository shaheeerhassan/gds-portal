package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Teacher;
import com.school.model.Timetable;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.impl.TimetableServiceImpl;
import com.school.service.interfaces.TeacherService;
import com.school.service.interfaces.TimetableService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/timetable/*")
public class TimetableController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";

    private final TimetableService timetableService;
    private final TeacherService teacherService;

    public TimetableController() {
        this.timetableService = new TimetableServiceImpl();
        this.teacherService = new TeacherServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/section/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT);
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /section/{sectionId}/{academicYearId}.");
            writeJson(resp, timetableService.getTimetableBySectionAndYear(parseInt(parts[0]), parseInt(parts[1])));
            return;
        }
        if (path.startsWith("/teacher/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/teacher/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /teacher/{teacherId}/{academicYearId}.");
            long teacherId = parseLong(parts[0]);
            requireTeacherSelfAccess(req, teacherId);
            writeJson(resp, timetableService.getTimetableByTeacher(teacherId, parseInt(parts[1])));
            return;
        }
        if (path.startsWith("/day/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/day/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /day/{sectionId}/{day}.");
            writeJson(resp, timetableService.getTimetableByDay(parseInt(parts[0]), parseDay(parts[1])));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        Timetable timetable = readBody(req, Timetable.class);
        if (timetable == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, timetableService.createTimetableEntry(timetable), "Timetable entry created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long timetableId = parseLong(pathInfo(req).substring(1));
        Timetable timetable = readBody(req, Timetable.class);
        if (timetable == null)
            throw new ValidationException("Request body is required.");
        timetable.setTimetableId(timetableId);
        timetableService.updateTimetableEntry(timetable);
        writeStatusMessage(resp, "Timetable entry updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long timetableId = parseLong(pathInfo(req).substring(1));
        timetableService.deleteTimetableEntry(timetableId);
        writeStatusMessage(resp, "Timetable entry deleted.");
    }

    private void requireTeacherSelfAccess(HttpServletRequest req, long teacherId) {
        if (AuthContext.hasRole(req, ROLE_TEACHER)) {
            Teacher target = teacherService.getTeacherById(teacherId);
            if (target.getUserId() != AuthContext.getUserId(req))
                throw new UnauthorizedException("You can only access your own data.");
        }
    }

    private Timetable.DayOfWeek parseDay(String value) {
        try {
            return Timetable.DayOfWeek.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid day: " + value);
        }
    }
}
