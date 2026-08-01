package com.school.dao.impl;

import com.school.model.Section;
import java.util.List;

public interface SectionDao {
    boolean insertSection(Section section);
    Section getSectionById(int sectionId);
    List<Section> getSectionsByClassAndYear(int classId, int academicYearId);
    boolean updateSection(Section section);
    boolean deleteSection(int sectionId);
    List<Section> getAllSections();
}
