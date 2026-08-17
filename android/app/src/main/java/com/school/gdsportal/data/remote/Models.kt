package com.school.gdsportal.data.remote

import com.google.gson.annotations.SerializedName

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
    @SerializedName(value = "isCurrent", alternate = ["current"])
    val isCurrent: Boolean
)

data class Announcement(
    val announcementId: Long = 0,
    val title: String,
    val content: String,
    val createdBy: Long = 0,
    val targetRoleId: Int? = null,
    val classId: Int? = null,
    val sectionId: Int? = null,
    @SerializedName(value = "isActive", alternate = ["active"])
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TeacherAttendance(
    val attendanceId: Long,
    val teacherId: Long,
    val attendanceDate: String,
    val status: String,
    val markedAt: String? = null
)

data class MarkTeacherAttendanceRequest(
    val records: List<TeacherAttendanceRecord>
)

data class TeacherAttendanceRecord(
    val teacherId: Long,
    val attendanceDate: String,
    val status: String
)

data class TeacherAttendanceStatusRequest(
    val status: String
)

data class NotificationCountResponse(
    val count: Int
)

data class Notification(
    val notificationId: Long = 0,
    val userId: Long = 0,
    val notificationType: String = "NEW_ANNOUNCEMENT",
    val title: String = "",
    val message: String = "",
    val referenceTable: String? = null,
    val referenceId: Long? = null,
    @SerializedName(value = "isRead", alternate = ["read"])
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val readAt: String? = null
)

data class BulkNotificationsRequest(
    val notifications: List<Notification>
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

data class Period(
    val periodId: Int,
    val periodNumber: Int,
    val startTime: String,
    val endTime: String
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
    val classId: Int?,
    val className: String?,
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

data class TeacherCreateRequest(
    val email: String,
    val username: String?,
    val password: String,
    val teacher: TeacherProfileFields
)

data class TeacherProfileFields(
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val hireDate: String,
    val dateOfBirth: String?,
    val gender: String?,
    val qualification: String?
)

data class ClassTeacherAssignment(
    val assignmentId: Long,
    val teacherId: Long,
    val sectionId: Int,
    val academicYearId: Int,
    val assignedDate: String?,
    val removedDate: String?,
    val isActive: Boolean
)

data class Principal(
    val principalId: Long,
    val userId: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val isActive: Boolean
)

data class CreatePrincipalRequest(
    val email: String,
    val username: String,
    val password: String,
    val principal: Principal
)

data class Administrator(
    val adminId: Long,
    val userId: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val phone: String
)

data class CreateAdministratorRequest(
    val email: String,
    val username: String,
    val password: String,
    val administrator: Administrator
)


data class Timetable(
    val timetableId: Long = 0,
    val sectionId: Int,
    val subjectId: Int,
    val teacherId: Long,
    val periodId: Int,
    val dayOfWeek: String,
    val academicYearId: Int
)

enum class ExaminationStatus {
    SCHEDULED, ONGOING, COMPLETED, PUBLISHED
}

data class Examination(
    val examinationId: Long = 0,
    val examName: String,
    val subjectId: Int,
    val sectionId: Int,
    val academicYearId: Int,
    val examDate: String, // format YYYY-MM-DD
    val startTime: String, // format HH:MM
    val endTime: String,
    val maxMarks: Double,
    val passingMarks: Double?,
    val status: ExaminationStatus,
    val createdBy: Long = 0,
    val createdAt: String? = null
)

data class StatusRequest(
    val status: String
)


enum class AssignmentStatus {
    CREATED, PUBLISHED
}

data class Assignment(
    val assignmentId: Long = 0,
    val teacherId: Long,
    val subjectId: Int,
    val sectionId: Int,
    val title: String,
    val description: String?,
    val maxMarks: Double,
    val deadline: String?,
    val status: AssignmentStatus,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AssignmentDisplay(
    val assignmentId: Long,
    val title: String,
    val subjectName: String,
    val sectionName: String,
    val teacherName: String,
    val deadline: String,
    val status: AssignmentStatus,
    val maxMarks: String,
    val description: String
)

enum class SubmissionStatus {
    SUBMITTED, LATE, GRADED
}

data class Submission(
    val submissionId: Long = 0,
    val assignmentId: Long,
    val studentId: Long,
    val submittedAt: String?,
    val fileUrl: String?,
    val status: SubmissionStatus,
    val marksAwarded: Double?,
    val feedback: String?,
    val gradedBy: Long?,
    val gradedAt: String?
)

data class SubmissionDisplay(
    val submissionId: Long,
    val studentName: String,
    val registrationNumber: String,
    val assignmentTitle: String,
    val submittedAt: String,
    val status: SubmissionStatus,
    val marksAwarded: String,
    val feedback: String,
    val fileUrl: String
)

data class Mark(
    val markId: Long = 0,
    val examinationId: Long,
    val studentId: Long,
    val marksObtained: Double?,
    val grade: String?,
    val remarks: String?,
    val enteredBy: Long?,
    val enteredAt: String?
)

data class MarkDisplay(
    val markId: Long,
    val studentName: String,
    val registrationNumber: String,
    val examinationName: String,
    val marksObtained: String,
    val grade: String,
    val remarks: String
)

enum class StudentAttendanceStatus {
    PRESENT, ABSENT, LATE, LEAVE
}

data class StudentAttendance(
    val attendanceId: Long = 0,
    val studentClassId: Long,
    val attendanceDate: String,
    val status: StudentAttendanceStatus,
    val periodId: Int,
    val markedBy: Long,
    val markedAt: String?,
    @SerializedName(value = "isLocked", alternate = ["locked"])
    val isLocked: Boolean,
    val remarks: String?
)

data class StudentAttendanceDisplay(
    val studentId: Long,
    val studentName: String,
    val registrationNumber: String,
    val status: StudentAttendanceStatus?,
    val remarks: String
)
