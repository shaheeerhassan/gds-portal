package com.school.gdsportal.data.remote

data class User(
    val userId: Long,
    val username: String?,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val roleId: Int? = null,
    val active: Boolean = true
)

data class UpdateUserStatusRequest(
    val userId: Long,
    val active: Boolean
)

data class Role(
    val id: Int,
    val name: String
)

data class AcademicYear(
    val academicYearId: Int,
    val yearName: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean
)

data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: String,
    val targetRole: String? = null,
    val active: Boolean = true
)

data class TeacherAttendance(
    val id: Int,
    val teacherId: Int,
    val date: String,
    val status: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null
)

data class NotificationCountResponse(
    val count: Int
)

data class SchoolClass(
    val classId: Int,
    val className: String,
    val numericLevel: Int,
    val description: String?
)

data class Section(
    val sectionId: Int,
    val classId: Int,
    val academicYearId: Int,
    val sectionName: String,
    val capacity: Int?,
    val roomNumber: String?
)

data class Subject(
    val subjectId: Int,
    val subjectName: String,
    val subjectCode: String,
    val description: String?
)

data class Student(
    val studentId: Long,
    val userId: Long,
    val registrationNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String?,
    val gender: String?,
    val admissionDate: String?,
    val active: Boolean
)

data class Enrollment(
    val studentClassId: Long,
    val studentId: Long,
    val classId: Int,
    val sectionId: Int,
    val academicYearId: Int,
    val rollNumber: String?,
    val enrollmentDate: String?,
    val active: Boolean
)

data class Parent(
    val parentId: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val occupation: String?,
    val active: Boolean
)

data class CreateStudentRequest(
    val email: String,
    val username: String?,
    val password: String,
    val student: CreateStudentData
)

data class CreateStudentData(
    val firstName: String,
    val lastName: String,
    val registrationNumber: String,
    val admissionDate: String,
    val gender: String,
    val dateOfBirth: String?
)

data class EnrollStudentRequest(
    val studentId: Long,
    val classId: Int,
    val sectionId: Int,
    val academicYearId: Int,
    val rollNumber: String?
)

data class TransferStudentRequest(
    val studentId: Long,
    val academicYearId: Int,
    val newSectionId: Int,
    val rollNumber: String?
)

data class LinkParentRequest(
    val studentId: Long,
    val parentId: Long,
    val relationshipType: String,
    val primaryContact: Boolean
)


data class ParentDirectoryDTO(
    val parentId: Long,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val occupation: String?
)

data class Teacher(
    val teacherId: Long,
    val userId: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val gender: String,
    val dateOfBirth: String?,
    val hireDate: String,
    val qualification: String?,
    val active: Boolean
)

data class TeacherClassDTO(
    val teacherClassId: Long,
    val teacherId: Long,
    val classId: Int,
    val className: String,
    val sectionId: Int,
    val sectionName: String,
    val academicYearId: Int
)

data class TeacherSubjectDTO(
    val teacherSubjectId: Long,
    val teacherId: Long,
    val subjectId: Int,
    val subjectName: String,
    val classId: Int,
    val className: String,
    val sectionId: Int,
    val sectionName: String,
    val academicYearId: Int
)

data class TeacherClassAssignRequest(
    val teacherId: Long,
    val classId: Int,
    val sectionId: Int,
    val academicYearId: Int
)

data class TeacherSubjectAssignRequest(
    val teacherId: Long,
    val subjectId: Int,
    val sectionId: Int,
    val academicYearId: Int
)
