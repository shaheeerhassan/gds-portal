package com.school.service.impl;

import com.school.dao.impl.ClassDaoImpl;
import com.school.dao.interfaces.ClassDao;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Class;
import com.school.service.interfaces.ClassService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class ClassServiceImpl implements ClassService {

    private final ClassDao classDao;

    public ClassServiceImpl() {
        classDao = new ClassDaoImpl();
    }

    @Override
    public Class createClass(Class c) {
        c.setClassName(validateRequired(c.getClassName(), "Class name"));
        validateId(c.getNumericLevel());

        if (classDao.getClassByNumericLevel(c.getNumericLevel()) != null)
            throw new DuplicateResourceException("A class at this level already exists.");

        if (!classDao.insertClass(c))
            throw new IllegalStateException("Failed to create class.");

        return c;
    }

    @Override
    public Class getClassById(int classId) {
        validateId(classId);
        Class c = classDao.getClassById(classId);
        if (c == null)
            throw new ResourceNotFoundException("Class not found.");
        return c;
    }

    @Override
    public Class getClassByNumericLevel(int numericLevel) {
        validateId(numericLevel);
        Class c = classDao.getClassByNumericLevel(numericLevel);
        if (c == null)
            throw new ResourceNotFoundException("Class not found.");
        return c;
    }

    @Override
    public List<Class> getAllClasses() {
        return classDao.getAllClasses();
    }

    @Override
    public void updateClass(Class c) {
        validateId(c.getClassId());
        c.setClassName(validateRequired(c.getClassName(), "Class name"));
        validateId(c.getNumericLevel());

        Class existingLevel = classDao.getClassByNumericLevel(c.getNumericLevel());
        if (existingLevel != null && existingLevel.getClassId() != c.getClassId())
            throw new DuplicateResourceException("A class at this level already exists.");

        if (!classDao.updateClass(c))
            throw new ResourceNotFoundException("Class not found.");
    }

    @Override
    public void deleteClass(int classId) {
        validateId(classId);
        if (!classDao.deleteClass(classId))
            throw new ResourceNotFoundException("Class not found.");
    }
}
