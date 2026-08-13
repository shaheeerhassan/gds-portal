package com.school.gdsportal.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.Response
import com.google.gson.JsonObject

// Placeholder API Service. You will provide Data Classes later.
import com.school.gdsportal.data.remote.LoginRequest
import com.school.gdsportal.data.remote.LoginResponse
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.data.remote.TeacherAttendance
import com.school.gdsportal.data.remote.NotificationCountResponse
import retrofit2.http.Query
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.data.remote.dto.PaginatedResponse

import com.school.gdsportal.data.remote.ApiResponse

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>

    @GET("api/academic-years/current")
    suspend fun getCurrentAcademicYear(): Response<ApiResponse<AcademicYear>>

    @GET("api/students/count")
    suspend fun getTotalStudentCount(): Response<ApiResponse<Int>>

    @GET("api/attendance/students/present/count")
    suspend fun getPresentStudentCount(): Response<ApiResponse<Int>>

    @GET("api/teachers/count")
    suspend fun getTotalTeacherCount(): Response<ApiResponse<Int>>

    @GET("api/attendance/teachers/date/{date}")
    suspend fun getTeacherAttendanceByDate(@Path("date") date: String): Response<ApiResponse<List<TeacherAttendance>>>

    @GET("api/announcements/global")
    suspend fun getGlobalAnnouncements(): Response<ApiResponse<List<Announcement>>>

    @GET("api/notifications/me/unread-count")
    suspend fun getUnreadNotificationCount(): Response<ApiResponse<Int>>

    @GET("api/classes/count")
    suspend fun getClassesCount(): Response<ApiResponse<Int>>

    @GET("api/sections/count")
    suspend fun getSectionsCount(): Response<ApiResponse<Int>>

    @GET("api/subjects/count")
    suspend fun getSubjectsCount(): Response<ApiResponse<Int>>

    @GET("api/academic-years/")
    suspend fun getAcademicYears(): Response<ApiResponse<List<AcademicYear>>>

    @GET("api/classes/")
    suspend fun getClasses(): Response<ApiResponse<List<com.school.gdsportal.data.remote.SchoolClass>>>

    @GET("api/sections/class/{classId}/{academicYearId}")
    suspend fun getSections(
        @Path("classId") classId: Int,
        @Path("academicYearId") academicYearId: Int
    ): Response<ApiResponse<List<com.school.gdsportal.data.remote.Section>>>

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

    @retrofit2.http.PUT("api/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: Long,
        @Body student: com.school.gdsportal.data.remote.Student
    ): Response<ApiResponse<String>>

    @retrofit2.http.POST("api/students/")
    suspend fun createStudent(
        @Body request: com.school.gdsportal.data.remote.CreateStudentRequest
    ): Response<ApiResponse<com.school.gdsportal.data.remote.Student>>

    @retrofit2.http.POST("api/enrollments/enroll")
    suspend fun enrollStudent(
        @Body request: com.school.gdsportal.data.remote.EnrollStudentRequest
    ): Response<ApiResponse<com.school.gdsportal.data.remote.Enrollment>>
}
