package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Period;
import com.school.service.impl.PeriodServiceImpl;
import com.school.service.interfaces.PeriodService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/periods/*")
public class PeriodController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final PeriodService periodService;

    public PeriodController() {
        this.periodService = new PeriodServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, periodService.getAllPeriods());
                return;
            default:
                if (path.startsWith("/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, periodService.getPeriodById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        Period period = readBody(req, Period.class);
        if (period == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, periodService.createPeriod(period), "Period created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int periodId = parseInt(pathInfo(req).substring(1));
        Period period = readBody(req, Period.class);
        if (period == null)
            throw new ValidationException("Request body is required.");
        period.setPeriodId(periodId);
        periodService.updatePeriod(period);
        writeStatusMessage(resp, "Period updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int periodId = parseInt(pathInfo(req).substring(1));
        periodService.deletePeriod(periodId);
        writeStatusMessage(resp, "Period deleted.");
    }
}
