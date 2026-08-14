package com.school.dao.impl;

import com.school.dao.interfaces.TeacherClassDao;
import com.school.exceptions.DaoException;
import com.school.dto.TeacherClassDTO;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherClassDaoImpl implements TeacherClassDao {

    private static final String INSERT = "INSERT INTO teacher_classes (teacher_id, class_id, section_id, academic_year_id) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_TEACHER = 
        "SELECT tc.teacher_class_id, tc.teacher_id, tc.class_id, c.class_name, tc.section_id, s.section_name, tc.academic_year_id " +
        "FROM teacher_classes tc " +
        "JOIN classes c ON tc.class_id = c.class_id " +
        "JOIN sections s ON tc.section_id = s.section_id " +
        "WHERE tc.teacher_id = ? AND tc.academic_year_id = ? " +
        "ORDER BY c.numeric_level, s.section_name";
    private static final String DELETE = "DELETE FROM teacher_classes WHERE teacher_class_id = ?";
    private static final String COUNT_ASSIGNED = "SELECT COUNT(*) FROM teacher_classes WHERE teacher_id = ? AND class_id = ? AND section_id = ? AND academic_year_id = ?";

    @Override
    public boolean assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, classId);
            ps.setInt(3, sectionId);
            ps.setInt(4, academicYearId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error assigning teacher class", e);
        }
    }

    @Override
    public boolean isAssigned(long teacherId, int classId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ASSIGNED)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, classId);
            ps.setInt(3, sectionId);
            ps.setInt(4, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking teacher class assignment", e);
        }
    }

    @Override
    public List<TeacherClassDTO> getTeacherClasses(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<TeacherClassDTO> assignments = new ArrayList<>();
                while (resultSet.next()) {
                    TeacherClassDTO dto = new TeacherClassDTO();
                    dto.setTeacherClassId(resultSet.getLong("teacher_class_id"));
                    dto.setTeacherId(resultSet.getLong("teacher_id"));
                    dto.setClassId(resultSet.getInt("class_id"));
                    dto.setClassName(resultSet.getString("class_name"));
                    dto.setSectionId(resultSet.getInt("section_id"));
                    dto.setSectionName(resultSet.getString("section_name"));
                    dto.setAcademicYearId(resultSet.getInt("academic_year_id"));
                    assignments.add(dto);
                }
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher classes", e);
        }
    }

    @Override
    public boolean unassignTeacherClass(long teacherClassId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, teacherClassId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error unassigning teacher class", e);
        }
    }
}