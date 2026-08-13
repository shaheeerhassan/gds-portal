package com.school.dao.impl;

import com.school.dao.interfaces.SectionDao;
import com.school.exceptions.DaoException;
import com.school.model.Section;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDaoImpl implements SectionDao {

    private static final String INSERT = "INSERT INTO sections (class_id, academic_year_id, section_name, capacity, room_number) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM sections WHERE section_id = ?";
    private static final String SELECT_BY_CLASS_AND_YEAR = "SELECT * FROM sections WHERE class_id = ? AND academic_year_id = ? ORDER BY section_id";
    private static final String SELECT_ALL = "SELECT * FROM sections ORDER BY section_id";
    private static final String UPDATE = "UPDATE sections SET class_id = ?, academic_year_id = ?, section_name = ?, capacity = ?, room_number = ? WHERE section_id = ?";
    private static final String DELETE = "DELETE FROM sections WHERE section_id = ?";

    @Override
    public boolean insertSection(Section section) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, section.getClassId());
            ps.setInt(2, section.getAcademicYearId());
            ps.setString(3, section.getSectionName());
            if (section.getCapacity() != null) {
                ps.setInt(4, section.getCapacity());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, section.getRoomNumber());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    section.setSectionId(resultSet.getInt(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting section", e);
        }
    }

    @Override
    public Section getSectionById(int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, sectionId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching section", e);
        }
    }

    @Override
    public List<Section> getSectionsByClassAndYear(int classId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_CLASS_AND_YEAR)) {

            ps.setInt(1, classId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Section> sections = new ArrayList<>();
                while (resultSet.next())
                    sections.add(mapRow(resultSet));
                return sections;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching sections", e);
        }
    }

    @Override
    public boolean updateSection(Section section) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setInt(1, section.getClassId());
            ps.setInt(2, section.getAcademicYearId());
            ps.setString(3, section.getSectionName());
            if (section.getCapacity() != null) {
                ps.setInt(4, section.getCapacity());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, section.getRoomNumber());
            ps.setInt(6, section.getSectionId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating section", e);
        }
    }

    @Override
    public boolean deleteSection(int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setInt(1, sectionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting section", e);
        }
    }

    @Override
    public List<Section> getAllSections() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Section> sections = new ArrayList<>();
            while (resultSet.next())
                sections.add(mapRow(resultSet));
            return sections;
        } catch (SQLException e) {
            throw new DaoException("Error fetching sections", e);
        }
    }

    @Override
    public int getSectionCount() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM sections");
             ResultSet resultSet = ps.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting sections", e);
        }
    }

    private Section mapRow(ResultSet resultSet) throws SQLException {
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