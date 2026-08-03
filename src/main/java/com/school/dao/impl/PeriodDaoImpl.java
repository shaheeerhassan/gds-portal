package com.school.dao.impl;

import com.school.dao.interfaces.PeriodDao;
import com.school.exception.DaoException;
import com.school.model.Period;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeriodDaoImpl implements PeriodDao {

    private static final String INSERT = "INSERT INTO periods (period_number, start_time, end_time) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM periods WHERE period_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM periods ORDER BY period_number";
    private static final String UPDATE = "UPDATE periods SET period_number = ?, start_time = ?, end_time = ? WHERE period_id = ?";
    private static final String DELETE = "DELETE FROM periods WHERE period_id = ?";

    @Override
    public int insertPeriod(Period period) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, period.getPeriodNumber());
            if (period.getStartTime() != null) {
                ps.setTime(2, Time.valueOf(period.getStartTime()));
            } else {
                ps.setNull(2, Types.TIME);
            }
            if (period.getEndTime() != null) {
                ps.setTime(3, Time.valueOf(period.getEndTime()));
            } else {
                ps.setNull(3, Types.TIME);
            }

            int success = ps.executeUpdate();
            if (success == 1) {
                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        int periodId = resultSet.getInt(1);
                        period.setPeriodId(periodId);
                        return periodId;
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error inserting period", e);
        }
    }

    @Override
    public Period getPeriodById(int periodId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, periodId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching period", e);
        }
    }

    @Override
    public List<Period> getAllPeriods() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Period> periods = new ArrayList<>();
            while (resultSet.next())
                periods.add(mapRow(resultSet));
            return periods;
        } catch (SQLException e) {
            throw new DaoException("Error fetching periods", e);
        }
    }

    @Override
    public boolean updatePeriod(Period period) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setInt(1, period.getPeriodNumber());
            if (period.getStartTime() != null) {
                ps.setTime(2, Time.valueOf(period.getStartTime()));
            } else {
                ps.setNull(2, Types.TIME);
            }
            if (period.getEndTime() != null) {
                ps.setTime(3, Time.valueOf(period.getEndTime()));
            } else {
                ps.setNull(3, Types.TIME);
            }
            ps.setInt(4, period.getPeriodId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating period", e);
        }
    }

    @Override
    public boolean deletePeriod(int periodId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setInt(1, periodId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting period", e);
        }
    }

    private Period mapRow(ResultSet resultSet) throws SQLException {
        Period period = new Period();
        period.setPeriodId(resultSet.getInt("period_id"));
        period.setPeriodNumber(resultSet.getInt("period_number"));
        Time startTime = resultSet.getTime("start_time");
        if (startTime != null)
            period.setStartTime(startTime.toLocalTime());
        Time endTime = resultSet.getTime("end_time");
        if (endTime != null)
            period.setEndTime(endTime.toLocalTime());
        return period;
    }
}