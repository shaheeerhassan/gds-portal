package com.school.dao.impl;

import com.school.dao.interfaces.SubjectDao;
import com.school.exceptions.DaoException;
import com.school.model.Subject;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDaoImpl implements SubjectDao {

    private static final String INSERT = "INSERT INTO subjects (subject_name, subject_code, description) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM subjects WHERE subject_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM subjects ORDER BY subject_id";
    private static final String UPDATE = "UPDATE subjects SET subject_name = ?, subject_code = ?, description = ? WHERE subject_id = ?";
    private static final String DELETE = "DELETE FROM subjects WHERE subject_id = ?";

    @Override
    public boolean insertSubject(Subject subject) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getSubjectCode());
            ps.setString(3, subject.getDescription());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    subject.setSubjectId(resultSet.getInt(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting subject", e);
        }
    }

    @Override
    public Subject getSubjectById(int subjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, subjectId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching subject", e);
        }
    }

    @Override
    public List<Subject> getAllSubjects() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Subject> subjects = new ArrayList<>();
            while (resultSet.next())
                subjects.add(mapRow(resultSet));
            return subjects;
        } catch (SQLException e) {
            throw new DaoException("Error fetching subjects", e);
        }
    }

    @Override
    public boolean updateSubject(Subject subject) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getSubjectCode());
            ps.setString(3, subject.getDescription());
            ps.setInt(4, subject.getSubjectId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating subject", e);
        }
    }

    @Override
    public boolean deleteSubject(int subjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setInt(1, subjectId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting subject", e);
        }
    }

    @Override
    public int getSubjectCount() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM subjects");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting subjects", e);
        }
    }

    private Subject mapRow(ResultSet resultSet) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(resultSet.getInt("subject_id"));
        subject.setSubjectName(resultSet.getString("subject_name"));
        subject.setSubjectCode(resultSet.getString("subject_code"));
        subject.setDescription(resultSet.getString("description"));
        return subject;
    }
}