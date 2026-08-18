package com.school.gdsportal.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Response
import com.google.gson.JsonObject

// Placeholder API Service. You will provide Data Classes later.
import com.school.gdsportal.data.remote.LoginRequest
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.data.remote.TeacherAttendance
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.data.remote.NotificationCountResponse
import retrofit2.http.Query
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.data.remote.dto.PaginatedResponse
import com.school.gdsportal.data.remote.ClassTeacherAssignment

import com.school.gdsportal.data.remote.ApiResponse
import com.school.gdsportal.data.remote.LoginData

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: com.school.gdsportal.data.remote.ChangePasswordRequest): Response<ApiResponse<Void>>
    
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Long,
        @Body user: User
    ): Response<ApiResponse<Void>>

    @GET("api/academic-years/current")
    suspend fun getCurrentAcademicYear(): Response<ApiResponse<AcademicYear>>

    @GET("api/students/count")
    suspend fun getTotalStudentCount(): Response<ApiResponse<Int>>

    @GET("api/attendance/students/present/count")
    suspend fun getPresentStudentCount(): Response<ApiResponse<Int>>

    @GET("api/teachers/count")
    suspend fun getTotalTeacherCount(): Response<ApiResponse<Int>>

    @GET("api/teachers/")
    suspend fun getAllTeachers(): Response<ApiResponse<List<Teacher>>>

    @GET("api/teachers/{teacherId}")
    suspend fun getTeacherById(@Path("teacherId") teacherId: Long): Response<ApiResponse<Teacher>>

    @POST("api/teachers")
    suspend fun createTeacher(@Body request: com.school.gdsportal.data.remote.TeacherCreateRequest): Response<ApiResponse<Teacher>>

    @PUT("api/teachers/{teacherId}")
    suspend fun updateTeacher(
        @Path("teacherId") teacherId: Long,
        @Body teacher: com.school.gdsportal.data.remote.TeacherProfileFields
    ): Response<ApiResponse<Void>>

    @DELETE("api/teachers/{teacherId}")
    suspend fun deactivateTeacher(@Path("teacherId") teacherId: Long): Response<ApiResponse<Void>>

    // Principals
    @GET("api/principals")
    suspend fun getPrincipals(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Principal>>>

    @GET("api/principals/{principalId}")
    suspend fun getPrincipalById(@Path("principalId") principalId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Principal>>

    @POST("api/principals")
    suspend fun createPrincipal(@Body request: com.school.gdsportal.data.remote.CreatePrincipalRequest): Response<ApiResponse<com.school.gdsportal.data.remote.Principal>>

    @PUT("api/principals/{principalId}")
    suspend fun updatePrincipal(
        @Path("principalId") principalId: Long,
        @Body principal: com.school.gdsportal.data.remote.Principal
    ): Response<ApiResponse<Void>>

    @DELETE("api/principals/{principalId}")
    suspend fun deactivatePrincipal(@Path("principalId") principalId: Long): Response<ApiResponse<Void>>

    // Administrators
    @GET("api/administrators")
    suspend fun getAdministrators(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Administrator>>>

    @GET("api/administrators/{adminId}")
    suspend fun getAdministratorById(@Path("adminId") adminId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Administrator>>

    @POST("api/administrators")
    suspend fun createAdministrator(@Body request: com.school.gdsportal.data.remote.CreateAdministratorRequest): Response<ApiResponse<com.school.gdsportal.data.remote.Administrator>>

    @PUT("api/administrators/{adminId}")
    suspend fun updateAdministrator(
        @Path("adminId") adminId: Long,
        @Body administrator: com.school.gdsportal.data.remote.Administrator
    ): Response<ApiResponse<Void>>

    // Academic Years
    @GET("api/academic-years")
    suspend fun getAcademicYears(): Response<ApiResponse<List<com.school.gdsportal.data.remote.AcademicYear>>>


    @GET("api/academic-years/{academicYearId}")
    suspend fun getAcademicYearById(@Path("academicYearId") id: Int): Response<ApiResponse<com.school.gdsportal.data.remote.AcademicYear>>

    @POST("api/academic-years")
    suspend fun createAcademicYear(@Body academicYear: com.school.gdsportal.data.remote.AcademicYear): Response<ApiResponse<com.school.gdsportal.data.remote.AcademicYear>>

    @PUT("api/academic-years/{academicYearId}")
    suspend fun updateAcademicYear(
        @Path("academicYearId") id: Int,
        @Body academicYear: com.school.gdsportal.data.remote.AcademicYear
    ): Response<ApiResponse<Void>>

    @PUT("api/academic-years/set-current/{academicYearId}")
    suspend fun setCurrentAcademicYear(@Path("academicYearId") id: Int): Response<ApiResponse<Void>>

    @GET("api/class-teachers/teacher/{teacherId}/history")
    suspend fun getClassTeacherHistory(@Path("teacherId") teacherId: Long): Response<ApiResponse<List<ClassTeacherAssignment>>>

    @POST("api/class-teachers")
    suspend fun assignClassTeacher(@Body assignment: ClassTeacherAssignment): Response<ApiResponse<Void>>

    @GET("api/class-teachers/section/{sectionId}")
    suspend fun getClassTeacherBySection(@Path("sectionId") sectionId: Int): Response<ApiResponse<ClassTeacherAssignment>>

    @DELETE("api/class-teachers/section/{sectionId}/year/{academicYearId}")
    suspend fun removeClassTeacher(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<Void>>

    @GET("api/attendance/teachers/teacher/{teacherId}")
    suspend fun getTeacherAttendance(
        @Path("teacherId") teacherId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ApiResponse<List<TeacherAttendance>>>

    @retrofit2.http.POST("api/attendance/teachers/")
    suspend fun markTeacherAttendance(
        @Body request: com.school.gdsportal.data.remote.MarkTeacherAttendanceRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.PUT("api/attendance/teachers/status/{attendanceId}")
    suspend fun updateTeacherAttendanceStatus(
        @Path("attendanceId") attendanceId: Long,
        @Body request: com.school.gdsportal.data.remote.TeacherAttendanceStatusRequest
    ): Response<ApiResponse<String>>

    @GET("api/attendance/teachers/date/{date}")
    suspend fun getTeacherAttendanceByDate(@Path("date") date: String): Response<ApiResponse<List<TeacherAttendance>>>

    @GET("api/teachers/search/{term}")
    suspend fun searchTeachers(@Path("term") term: String): Response<ApiResponse<List<Teacher>>>

    @GET("api/students/search/{term}")
    suspend fun searchStudents(@Path("term") term: String): Response<ApiResponse<List<com.school.gdsportal.data.remote.Student>>>

    @GET("api/announcements/global")
    suspend fun getGlobalAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("api/announcements/")
    suspend fun getAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("api/announcements/id/{id}")
    suspend fun getAnnouncementById(@Path("id") id: Long): Response<ApiResponse<Announcement>>

    @POST("api/announcements/")
    suspend fun createAnnouncement(
        @Body announcement: Announcement,
        @Query("notify") notify: Boolean = false
    ): Response<ApiResponse<Announcement>>

    @PUT("api/announcements/{id}")
    suspend fun updateAnnouncement(@Path("id") id: Long, @Body announcement: Announcement): Response<ApiResponse<String>>

    @PUT("api/announcements/disable/{id}")
    suspend fun disableAnnouncement(@Path("id") id: Long): Response<ApiResponse<String>>

    // Notifications
    @GET("api/notifications/me/unread-count")
    suspend fun getUnreadNotificationCount(): Response<ApiResponse<Int>>

    @GET("api/notifications/me/unread")
    suspend fun getUnreadNotifications(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Notification>>>

    @GET("api/notifications/me")
    suspend fun getMyNotifications(
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Notification>>>

    @GET("api/notifications/id/{notificationId}")
    suspend fun getNotificationById(@Path("notificationId") notificationId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Notification>>

    @POST("api/notifications/")
    suspend fun createNotification(@Body notification: com.school.gdsportal.data.remote.Notification): Response<ApiResponse<com.school.gdsportal.data.remote.Notification>>

    @POST("api/notifications/bulk")
    suspend fun createBulkNotifications(@Body request: com.school.gdsportal.data.remote.BulkNotificationsRequest): Response<ApiResponse<Void>>

    @POST("api/notifications/broadcast")
    suspend fun broadcastNotification(@Body request: com.school.gdsportal.data.remote.BroadcastNotificationRequest): Response<ApiResponse<Void>>

    @PUT("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse<Void>>

    @PUT("api/notifications/read/{notificationId}")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Long): Response<ApiResponse<Void>>

    @DELETE("api/notifications/me/all")
    suspend fun deleteAllMyNotifications(): Response<ApiResponse<Void>>

    @DELETE("api/notifications/{notificationId}")
    suspend fun deleteNotificationById(@Path("notificationId") notificationId: Long): Response<ApiResponse<Void>>

    // Users
    @GET("api/users/")
    suspend fun getAllUsers(): Response<ApiResponse<List<User>>>

    @GET("api/users/role/{roleId}")
    suspend fun getUsersByRole(@Path("roleId") roleId: Int): Response<ApiResponse<List<User>>>

    @GET("api/classes/count")
    suspend fun getClassesCount(): Response<ApiResponse<Int>>

    @GET("api/sections/count")
    suspend fun getSectionsCount(): Response<ApiResponse<Int>>

    @GET("api/subjects/count")
    suspend fun getSubjectsCount(): Response<ApiResponse<Int>>


    @GET("api/classes/")
    suspend fun getClasses(): Response<ApiResponse<List<com.school.gdsportal.data.remote.SchoolClass>>>

    @GET("api/classes/{classId}")
    suspend fun getClassById(@Path("classId") classId: Int): Response<ApiResponse<com.school.gdsportal.data.remote.SchoolClass>>

    @POST("api/classes/")
    suspend fun createClass(@Body schoolClass: com.school.gdsportal.data.remote.SchoolClass): Response<ApiResponse<com.school.gdsportal.data.remote.SchoolClass>>

    @PUT("api/classes/{classId}")
    suspend fun updateClass(@Path("classId") classId: Int, @Body schoolClass: com.school.gdsportal.data.remote.SchoolClass): Response<ApiResponse<com.school.gdsportal.data.remote.SchoolClass>>

    @DELETE("api/classes/{classId}")
    suspend fun deleteClass(@Path("classId") classId: Int): Response<Void>

    @GET("api/sections/class/{classId}/{academicYearId}")
    suspend fun getSections(
        @Path("classId") classId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Section>>>

    @GET("api/sections/")
    suspend fun getAllSections(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Section>>>

    @GET("api/sections/{sectionId}")
    suspend fun getSectionById(@Path("sectionId") sectionId: Int): Response<ApiResponse<com.school.gdsportal.data.remote.Section>>

    @POST("api/sections/")
    suspend fun createSection(@Body section: com.school.gdsportal.data.remote.Section): Response<ApiResponse<com.school.gdsportal.data.remote.Section>>

    @PUT("api/sections/{sectionId}")
    suspend fun updateSection(@Path("sectionId") sectionId: Int, @Body section: com.school.gdsportal.data.remote.Section): Response<ApiResponse<com.school.gdsportal.data.remote.Section>>

    @DELETE("api/sections/{sectionId}")
    suspend fun deleteSection(@Path("sectionId") sectionId: Int): Response<Void>

    @GET("api/students/directory")
    suspend fun getStudentsDirectory(
        @Query("q") query: String?,
        @Query("academicYearId") academicYearId: Int?,
        @Query("classId") classId: Int?,
        @Query("sectionId") sectionId: Int?,
        @Query("enrolled") enrolled: Boolean?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PaginatedResponse<StudentDirectoryDTO>>>

    @GET("api/students/{studentId}")
    suspend fun getStudent(@Path("studentId") studentId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Student>>

    @GET("api/enrollments/current/{studentId}")
    suspend fun getCurrentEnrollment(@Path("studentId") studentId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Enrollment>>

    @GET("api/enrollments/student/{studentId}")
    suspend fun getEnrollmentHistory(@Path("studentId") studentId: Long): Response<ApiResponse<List<com.school.gdsportal.data.remote.Enrollment>>>

    @GET("api/parents/student/{studentId}")
    suspend fun getParentsByStudentId(@Path("studentId") studentId: Long): Response<ApiResponse<List<com.school.gdsportal.data.remote.Parent>>>

    @GET("api/parents/{parentId}")
    suspend fun getParent(@Path("parentId") parentId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Parent>>

    @GET("api/students/parent/{parentId}")
    suspend fun getStudentsByParentId(@Path("parentId") parentId: Long): Response<ApiResponse<List<com.school.gdsportal.data.remote.Student>>>

    @GET("api/parents/directory")
    suspend fun getParentsDirectory(
        @Query("q") query: String?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<PaginatedResponse<com.school.gdsportal.data.remote.ParentDirectoryDTO>>>

    @retrofit2.http.POST("api/parents/")
    suspend fun createParent(
        @Body request: com.school.gdsportal.data.remote.CreateParentRequest
    ): Response<ApiResponse<com.school.gdsportal.data.remote.Parent>>

    @retrofit2.http.POST("api/parents/link")
    suspend fun linkParent(
        @Body request: com.school.gdsportal.data.remote.LinkParentRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.DELETE("api/parents/link/{parentId}/{studentId}")
    suspend fun unlinkParent(
        @Path("parentId") parentId: Long,
        @Path("studentId") studentId: Long
    ): Response<ApiResponse<String>>

    @retrofit2.http.GET("api/users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: Long
    ): Response<ApiResponse<com.school.gdsportal.data.remote.User>>
    
    @retrofit2.http.PUT("api/users/status")
    suspend fun updateUserStatus(
        @Body request: com.school.gdsportal.data.remote.UpdateUserStatusRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.PUT("api/parents/{parentId}")
    suspend fun updateParent(
        @Path("parentId") parentId: Long,
        @Body parent: com.school.gdsportal.data.remote.Parent
    ): Response<ApiResponse<String>>

    @retrofit2.http.DELETE("api/parents/{parentId}")
    suspend fun deleteParent(
        @Path("parentId") parentId: Long
    ): Response<ApiResponse<String>>

    @retrofit2.http.PUT("api/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: Long,
        @Body student: com.school.gdsportal.data.remote.Student
    ): Response<ApiResponse<String>>

    @retrofit2.http.POST("api/students/")
    suspend fun createStudent(
        @Body request: com.school.gdsportal.data.remote.CreateStudentRequest
    ): Response<ApiResponse<com.school.gdsportal.data.remote.CreateStudentData>>

    @retrofit2.http.DELETE("api/students/{studentId}")
    suspend fun deleteStudent(
        @Path("studentId") studentId: Long
    ): Response<ApiResponse<String>>

    @retrofit2.http.POST("api/enrollments/enroll")
    suspend fun enrollStudent(
        @Body request: com.school.gdsportal.data.remote.EnrollStudentRequest
    ): Response<ApiResponse<com.school.gdsportal.data.remote.Enrollment>>

    @retrofit2.http.POST("api/enrollments/transfer")
    suspend fun transferStudent(
        @Body request: com.school.gdsportal.data.remote.TransferStudentRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.PUT("api/enrollments/end/{studentId}/{academicYearId}")
    suspend fun endEnrollment(
        @Path("studentId") studentId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<String>>

    @retrofit2.http.GET("api/teacher-classes/teacher/{teacherId}/{academicYearId}")
    suspend fun getTeacherClasses(
        @Path("teacherId") teacherId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.TeacherClassDTO>>>

    @retrofit2.http.POST("api/teacher-classes/")
    suspend fun assignTeacherClass(
        @Body request: com.school.gdsportal.data.remote.TeacherClassAssignRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.DELETE("api/teacher-classes/{teacherClassId}")
    suspend fun unassignTeacherClass(
        @Path("teacherClassId") teacherClassId: Long
    ): Response<ApiResponse<String>>

    @retrofit2.http.GET("api/teacher-subjects/teacher/{teacherId}/{academicYearId}")
    suspend fun getTeacherSubjects(
        @Path("teacherId") teacherId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.TeacherSubjectDTO>>>

    @retrofit2.http.POST("api/teacher-subjects/")
    suspend fun assignTeacherSubject(
        @Body request: com.school.gdsportal.data.remote.TeacherSubjectAssignRequest
    ): Response<ApiResponse<String>>

    @retrofit2.http.DELETE("api/teacher-subjects/{teacherSubjectId}")
    suspend fun unassignTeacherSubject(
        @Path("teacherSubjectId") teacherSubjectId: Long
    ): Response<ApiResponse<String>>

    @GET("api/subjects/")
    suspend fun getSubjects(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Subject>>>

    @GET("api/subjects/{subjectId}")
    suspend fun getSubjectById(@Path("subjectId") subjectId: Int): Response<ApiResponse<com.school.gdsportal.data.remote.Subject>>

    @GET("api/subjects/section/{sectionId}/{academicYearId}")
    suspend fun getSubjectsBySection(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Subject>>>

    @POST("api/subjects/")
    suspend fun createSubject(@Body subject: com.school.gdsportal.data.remote.Subject): Response<ApiResponse<com.school.gdsportal.data.remote.Subject>>

    @PUT("api/subjects/{subjectId}")
    suspend fun updateSubject(@Path("subjectId") subjectId: Int, @Body subject: com.school.gdsportal.data.remote.Subject): Response<ApiResponse<com.school.gdsportal.data.remote.Subject>>

    @DELETE("api/subjects/{subjectId}")
    suspend fun deleteSubject(@Path("subjectId") subjectId: Int): Response<Void>

    @GET("api/periods/")
    suspend fun getAllPeriods(): Response<ApiResponse<List<com.school.gdsportal.data.remote.Period>>>

    @GET("api/periods/{periodId}")
    suspend fun getPeriodById(@Path("periodId") periodId: Int): Response<ApiResponse<com.school.gdsportal.data.remote.Period>>

    @POST("api/periods/")
    suspend fun createPeriod(@Body period: com.school.gdsportal.data.remote.Period): Response<ApiResponse<com.school.gdsportal.data.remote.Period>>

    @PUT("api/periods/{periodId}")
    suspend fun updatePeriod(@Path("periodId") periodId: Int, @Body period: com.school.gdsportal.data.remote.Period): Response<ApiResponse<com.school.gdsportal.data.remote.Period>>

    @DELETE("api/periods/{periodId}")
    suspend fun deletePeriod(@Path("periodId") periodId: Int): Response<Void>

    // TIMETABLE
    @GET("api/timetable/section/{sectionId}/{academicYearId}")
    suspend fun getTimetableBySection(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Timetable>>>

    @GET("api/timetable/teacher/{teacherId}/{academicYearId}")
    suspend fun getTimetableByTeacher(
        @Path("teacherId") teacherId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Timetable>>>

    @GET("api/timetable/day/{sectionId}/{day}")
    suspend fun getTimetableByDay(
        @Path("sectionId") sectionId: Int,
        @Path("day") day: String
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Timetable>>>

    @POST("api/timetable/")
    suspend fun createTimetable(@Body timetable: com.school.gdsportal.data.remote.Timetable): Response<ApiResponse<com.school.gdsportal.data.remote.Timetable>>

    @PUT("api/timetable/{timetableId}")
    suspend fun updateTimetable(
        @Path("timetableId") timetableId: Long,
        @Body timetable: com.school.gdsportal.data.remote.Timetable
    ): Response<ApiResponse<Any>>

    @DELETE("api/timetable/{timetableId}")
    suspend fun deleteTimetable(@Path("timetableId") timetableId: Long): Response<ApiResponse<Any>>

    // EXAMINATIONS
    @GET("api/examinations/id/{examinationId}")
    suspend fun getExaminationById(@Path("examinationId") examinationId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Examination>>

    @GET("api/examinations/section/{sectionId}/year/{academicYearId}")
    suspend fun getExaminationsBySection(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Examination>>>

    @POST("api/examinations/")
    suspend fun createExamination(@Body examination: com.school.gdsportal.data.remote.Examination): Response<ApiResponse<com.school.gdsportal.data.remote.Examination>>

    @PUT("api/examinations/{examinationId}")
    suspend fun updateExamination(
        @Path("examinationId") examinationId: Long,
        @Body examination: com.school.gdsportal.data.remote.Examination
    ): Response<ApiResponse<Any>>

    @PUT("api/examinations/status/{examinationId}")
    suspend fun updateExaminationStatus(
        @Path("examinationId") examinationId: Long,
        @Body request: com.school.gdsportal.data.remote.StatusRequest
    ): Response<ApiResponse<Any>>

    @GET("api/assignments/section/{sectionId}/{academicYearId}")
    suspend fun getAssignmentsBySection(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Assignment>>>

    @GET("api/assignments/id/{assignmentId}")
    suspend fun getAssignmentById(@Path("assignmentId") assignmentId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Assignment>>

    @GET("api/submissions/assignment/{assignmentId}")
    suspend fun getSubmissionsByAssignment(@Path("assignmentId") assignmentId: Long): Response<ApiResponse<List<com.school.gdsportal.data.remote.Submission>>>

    @GET("api/submissions/id/{submissionId}")
    suspend fun getSubmissionById(@Path("submissionId") submissionId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Submission>>

    @GET("api/marks/examination/{examinationId}")
    suspend fun getMarksByExamination(@Path("examinationId") examinationId: Long): Response<ApiResponse<List<com.school.gdsportal.data.remote.Mark>>>

    @GET("api/marks/id/{markId}")
    suspend fun getMarkById(@Path("markId") markId: Long): Response<ApiResponse<com.school.gdsportal.data.remote.Mark>>

    @GET("api/attendance/students/section/{sectionId}/date/{date}")
    suspend fun getStudentAttendanceBySectionAndDate(
        @Path("sectionId") sectionId: Int,
        @Path("date") date: String
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.StudentAttendance>>>

    @GET("api/enrollments/section/{sectionId}/{academicYearId}")
    suspend fun getEnrollmentsBySection(
        @Path("sectionId") sectionId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Enrollment>>>

    @GET("api/reports/student-performance/{studentId}/{academicYearId}")
    suspend fun getStudentPerformanceReport(
        @Path("studentId") studentId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<com.google.gson.JsonObject>>

    @GET("api/reports/teacher-attendance/{teacherId}/{month}/{year}")
    suspend fun getTeacherAttendanceReport(
        @Path("teacherId") teacherId: Long,
        @Path("month") month: Int,
        @Path("year") year: Int
    ): Response<ApiResponse<com.school.gdsportal.data.remote.dto.TeacherAttendanceReportData>>

    @GET("api/reports/class-attendance/{sectionId}/{month}/{year}")
    suspend fun getClassAttendanceReport(
        @Path("sectionId") sectionId: Int,
        @Path("month") month: Int,
        @Path("year") year: Int
    ): Response<ApiResponse<com.school.gdsportal.data.remote.dto.ClassAttendanceReportData>>

    @GET("api/reports/teacher-performance/{teacherId}/{academicYearId}")
    suspend fun getTeacherPerformanceReport(
        @Path("teacherId") teacherId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<com.school.gdsportal.data.remote.dto.TeacherPerformanceReportData>>

    @GET("api/reports/examination/{examinationId}")
    suspend fun getExaminationReport(
        @Path("examinationId") examinationId: Long
    ): Response<ApiResponse<com.school.gdsportal.data.remote.dto.ExaminationReportData>>

    @GET("api/reports/student-attendance-summary/{studentId}/{academicYearId}")
    suspend fun getStudentAttendanceSummary(
        @Path("studentId") studentId: Long,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<com.school.gdsportal.data.remote.dto.StudentAttendanceSummaryData>>
}
