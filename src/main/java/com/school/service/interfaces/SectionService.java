package com.school.service.interfaces;

import com.school.model.Section;

import java.util.List;

public interface SectionService {
    Section createSection(Section section);
    Section getSectionById(int sectionId);
    List<Section> getSectionsByClassAndYear(int classId, int academicYearId);
    List<Section> getAllSections();
    void updateSection(Section section);
    void deleteSection(int sectionId);
}
