package com.school.service.impl;

import com.school.dao.impl.SectionDaoImpl;
import com.school.dao.interfaces.SectionDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Section;
import com.school.service.interfaces.SectionService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class SectionServiceImpl implements SectionService {

    private final SectionDao sectionDao;

    public SectionServiceImpl() {
        sectionDao = new SectionDaoImpl();
    }

    @Override
    public Section createSection(Section section) {
        validateId(section.getClassId());
        validateId(section.getAcademicYearId());
        section.setSectionName(validateRequired(section.getSectionName(), "Section name"));
        if (section.getCapacity() != null && section.getCapacity() <= 0)
            throw new ValidationException("Capacity must be a positive number.");

        if (!sectionDao.insertSection(section))
            throw new IllegalStateException("Failed to create section.");

        return section;
    }

    @Override
    public Section getSectionById(int sectionId) {
        validateId(sectionId);
        Section section = sectionDao.getSectionById(sectionId);
        if (section == null)
            throw new ResourceNotFoundException("Section not found.");
        return section;
    }

    @Override
    public List<Section> getSectionsByClassAndYear(int classId, int academicYearId) {
        validateId(classId);
        validateId(academicYearId);
        return sectionDao.getSectionsByClassAndYear(classId, academicYearId);
    }

    @Override
    public List<Section> getAllSections() {
        return sectionDao.getAllSections();
    }

    @Override
    public void updateSection(Section section) {
        validateId(section.getSectionId());
        section.setSectionName(validateRequired(section.getSectionName(), "Section name"));
        if (section.getCapacity() != null && section.getCapacity() <= 0)
            throw new ValidationException("Capacity must be a positive number.");

        if (!sectionDao.updateSection(section))
            throw new ResourceNotFoundException("Section not found.");
    }

    @Override
    public void deleteSection(int sectionId) {
        validateId(sectionId);
        if (!sectionDao.deleteSection(sectionId))
            throw new ResourceNotFoundException("Section not found.");
    }
}
