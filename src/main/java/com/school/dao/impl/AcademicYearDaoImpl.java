package com.school.dao.impl;

import com.school.dao.interfaces.AcademicYearDao;
import com.school.exception.DaoException;
import com.school.model.AcademicYear;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicYearDaoImpl implements AcademicYearDao {

    private static final String INSERT = "INSERT INTO academic_years (year_name, start_date, end_date, is_current) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM academic_years WHERE academic_year_id = ?";
    private static final String SELECT_CURRENT = "SELECT * FROM academic_years WHERE is_current = TRUE";
    private static final String SELECT_ALL = "SELECT * FROM academic_years ORDER BY academic_year_id";
    private static final String UPDATE = "UPDATE academic_years SET year_name = ?, start_date = ?, end_date = ?, is_current = ? WHERE academic_year_id = ?";
    private static final String CLEAR_CURRENT = "UPDATE academic_years SET is_current = FALSE WHERE is_current = TRUE";
    private static final String SET_CURRENT = "UPDATE academic_years SET is_current = TRUE WHERE academic_year_id = ?";

    @Override
    public boolean insertAcademicYear(AcademicYear academicYear) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, academicYear.getYearName());
            if (academicYear.getStartDate() != null) {
                ps.setDate(2, Date.valueOf(academicYear.getStartDate()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            if (academicYear.getEndDate() != null) {
                ps.setDate(3, Date.valueOf(academicYear.getEndDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setBoolean(4, academicYear.isCurrent());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    academicYear.setAcademicYearId(resultSet.getInt(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting academic year", e);
        }
    }

    @Override
    public AcademicYear getCurrentAcademicYear() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_CURRENT);
             ResultSet resultSet = ps.executeQuery()) {

            if (resultSet.next())
                return mapRow(resultSet);
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching current academic year", e);
        }
    }

    @Override
    public AcademicYear getAcademicYearById(int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching academic year", e);
        }
    }

    @Override
    public List<AcademicYear> getAllAcademicYears() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<AcademicYear> academicYears = new ArrayList<>();
            while (resultSet.next())
                academicYears.add(mapRow(resultSet));
            return academicYears;
        } catch (SQLException e) {
            throw new DaoException("Error fetching academic years", e);
        }
    }

    @Override
    public boolean updateAcademicYear(AcademicYear academicYear) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, academicYear.getYearName());
            if (academicYear.getStartDate() != null) {
                ps.setDate(2, Date.valueOf(academicYear.getStartDate()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            if (academicYear.getEndDate() != null) {
                ps.setDate(3, Date.valueOf(academicYear.getEndDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setBoolean(4, academicYear.isCurrent());
            ps.setInt(5, academicYear.getAcademicYearId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating academic year", e);
        }
    }

    @Override
    public boolean setCurrentAcademicYear(int academicYearId) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(CLEAR_CURRENT)) {
                ps.executeUpdate();
            }
            boolean updated;
            try (PreparedStatement ps = cn.prepareStatement(SET_CURRENT)) {
                ps.setInt(1, academicYearId);
                updated = ps.executeUpdate() > 0;
            }

            cn.commit();
            return updated;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error setting current academic year", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    private AcademicYear mapRow(ResultSet resultSet) throws SQLException {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setAcademicYearId(resultSet.getInt("academic_year_id"));
        academicYear.setYearName(resultSet.getString("year_name"));
        Date startDate = resultSet.getDate("start_date");
        if (startDate != null)
            academicYear.setStartDate(startDate.toLocalDate());
        Date endDate = resultSet.getDate("end_date");
        if (endDate != null)
            academicYear.setEndDate(endDate.toLocalDate());
        academicYear.setCurrent(resultSet.getBoolean("is_current"));
        return academicYear;
    }
}