package com.school.dao.impl;

import com.school.dao.interfaces.TeacherClassDao;
import com.school.exceptions.DaoException;
import com.school.model.Section;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherClassDaoImpl implements TeacherClassDao {

    private static final String INSERT = "INSERT INTO teacher_classes (teacher_id, class_id, section_id, academic_year_id) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_TEACHER = "SELECT DISTINCT s.* FROM sections s JOIN teacher_classes tc ON s.section_id = tc.section_id WHERE tc.teacher_id = ? AND tc.academic_year_id = ? ORDER BY s.section_id";
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
    public List<Section> getTeacherClasses(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Section> sections = new ArrayList<>();
                while (resultSet.next())
                    sections.add(mapSection(resultSet));
                return sections;
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

    private Section mapSection(ResultSet resultSet) throws SQLException {
        Section section = new Section();
        section.setSectionId(resultSet.getInt("section_id"));
        section.setClassId(resultSet.getInt("class_id"));
        section.setAcademicYearId(resultSet.getInt("academic_year_id"));
        section.setSectionName(resultSet.getString("section_name"));
        int capacity = resultSet.getInt("capacity");
        if (!resultSet.wasNull())
            section.setCapacity(capacity);
        section.setRoomNumber(resultSet.getString("room_number"));
        return section;
    }
}