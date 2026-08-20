package com.school.gdsportal.ui.principal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.school.gdsportal.di.AppContainer
import com.school.gdsportal.ui.admin.AcademicsLandingScreen
import com.school.gdsportal.ui.admin.academics.academicyears.*
import com.school.gdsportal.ui.admin.academics.classes.*
import com.school.gdsportal.ui.admin.academics.sections.*
import com.school.gdsportal.ui.admin.academics.subjects.*
import com.school.gdsportal.ui.admin.academics.periods.*
import com.school.gdsportal.ui.admin.teaching.classteacher.*
import com.school.gdsportal.ui.admin.teaching.teacherclasses.*
import com.school.gdsportal.ui.admin.teaching.teachersubjects.*
import com.school.gdsportal.ui.admin.teaching.timetable.*
import com.school.gdsportal.ui.admin.assessment.examinations.*
import com.school.gdsportal.ui.admin.assessment.assignments.*
import com.school.gdsportal.ui.admin.assessment.submissions.*
import com.school.gdsportal.ui.admin.assessment.marks.*
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardScreen
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardViewModel
import com.school.gdsportal.ui.admin.PeopleLandingScreen
import com.school.gdsportal.ui.admin.administrators.*
import com.school.gdsportal.ui.admin.principals.*
import com.school.gdsportal.ui.admin.profile.ChangePasswordScreen
import com.school.gdsportal.ui.admin.profile.ChangePasswordViewModel
import com.school.gdsportal.ui.admin.profile.ProfileScreen
import com.school.gdsportal.ui.admin.profile.ProfileViewModel
import com.school.gdsportal.ui.admin.*
import kotlinx.coroutines.launch

@Composable
fun PrincipalAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "principal_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PrincipalDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        val openDrawer: () -> Unit = {
            coroutineScope.launch { drawerState.open() }
        }

        NavHost(navController = navController, startDestination = "principal_dashboard") {

            // -------------------------------------------------------------
            // DASHBOARD
            // -------------------------------------------------------------
            composable("principal_dashboard") {
                val dashboardViewModel: AdminDashboardViewModel = viewModel(
                    factory = AdminDashboardViewModel.provideFactory(
                        appContainer.apiService,
                        appContainer.tokenManager
                    )
                )
                // Reusing the upgraded Admin Dashboard view, but the Principal has lesser
                // authority than an Administrator: isReadOnly = true hides every creation
                // action (Add Student, Add Teacher, Broadcast, Notify) since those routes
                // aren't even registered below and would otherwise crash on tap.
                AdminDashboardScreen(
                    isReadOnly = true,
                    viewModel = dashboardViewModel,
                    onMenuClick = openDrawer,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // -------------------------------------------------------------
            // ACADEMICS SECTION
            // -------------------------------------------------------------
            composable("admin_academics_landing") {
                AcademicsLandingScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { openDrawer() }
                )
            }

            composable("academic-years") {
                val viewModel: AcademicYearsDirectoryViewModel = viewModel(
                    factory = AcademicYearsDirectoryViewModel.Factory(appContainer.apiService)
                )
                AcademicYearsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddClick = { }, // Disabled
                    onYearClick = { id -> navController.navigate("academic-years/$id") }
                )
            }

            composable(
                route = "academic-years/{academicYearId}",
                arguments = listOf(navArgument("academicYearId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("academicYearId") ?: 0
                val viewModel: AcademicYearDetailViewModel = viewModel(
                    factory = AcademicYearDetailViewModel.Factory(id, appContainer.apiService)
                )
                AcademicYearDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { } // Disabled
                )
            }

            composable("classes") {
                val viewModel: ClassesDirectoryViewModel = viewModel(
                    factory = ClassesDirectoryViewModel.Factory(appContainer.apiService)
                )
                ClassesDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddClick = { },
                    onClassClick = { id -> navController.navigate("classes/$id") }
                )
            }

            composable(
                route = "classes/{classId}",
                arguments = listOf(navArgument("classId") { type = NavType.IntType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getInt("classId") ?: return@composable
                val viewModel: ClassDetailViewModel = viewModel(
                    factory = ClassDetailViewModel.Factory(classId, appContainer.apiService)
                )
                ClassDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            composable("sections") {
                val viewModel: SectionsDirectoryViewModel = viewModel(
                    factory = SectionsDirectoryViewModel.Factory(appContainer.apiService)
                )
                SectionsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddClick = { },
                    onSectionClick = { id -> navController.navigate("sections/$id") }
                )
            }

            composable(
                route = "sections/{sectionId}",
                arguments = listOf(navArgument("sectionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getInt("sectionId") ?: return@composable
                val viewModel: SectionDetailViewModel = viewModel(
                    factory = SectionDetailViewModel.Factory(sectionId, appContainer.apiService)
                )
                SectionDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            composable("subjects") {
                val viewModel: SubjectsDirectoryViewModel = viewModel(
                    factory = SubjectsDirectoryViewModel.Factory(appContainer.apiService)
                )
                SubjectsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddClick = { },
                    onSubjectClick = { id -> navController.navigate("subjects/$id") }
                )
            }

            composable(
                route = "subjects/{subjectId}",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: return@composable
                val viewModel: SubjectDetailViewModel = viewModel(
                    factory = SubjectDetailViewModel.Factory(subjectId, appContainer.apiService)
                )
                SubjectDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            composable("periods") {
                val viewModel: PeriodsDirectoryViewModel = viewModel(
                    factory = PeriodsDirectoryViewModel.Factory(appContainer.apiService)
                )
                PeriodsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddClick = { },
                    onPeriodClick = { id -> navController.navigate("periods/$id") }
                )
            }

            composable(
                route = "periods/{periodId}",
                arguments = listOf(navArgument("periodId") { type = NavType.IntType })
            ) { backStackEntry ->
                val periodId = backStackEntry.arguments?.getInt("periodId") ?: return@composable
                val viewModel: PeriodDetailViewModel = viewModel(
                    factory = PeriodDetailViewModel.Factory(periodId, appContainer.apiService)
                )
                PeriodDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            // -------------------------------------------------------------
            // TEACHING SECTION
            // -------------------------------------------------------------
            composable("admin_teaching_landing") {
                TeachingLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("class-teachers") {
                val viewModel: ClassTeachersDirectoryViewModel = viewModel(
                    factory = ClassTeachersDirectoryViewModel.Factory(appContainer.apiService)
                )
                ClassTeachersDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { },
                    onAssignmentClick = { sectionId, yearId ->
                        // Route removed for assignment modification
                    }
                )
            }

            composable("teacher-classes") {
                val viewModel: TeacherClassesViewModel = viewModel(
                    factory = TeacherClassesViewModel.Factory(appContainer.apiService)
                )
                TeacherClassesScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onTeacherClick = { teacherId ->
                        navController.navigate("teachers/$teacherId")
                    }
                )
            }

            composable("teacher-subjects") {
                val viewModel: TeacherSubjectsViewModel = viewModel(
                    factory = TeacherSubjectsViewModel.Factory(appContainer.apiService)
                )
                TeacherSubjectsScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onTeacherClick = { teacherId ->
                        navController.navigate("teachers/$teacherId")
                    }
                )
            }

            composable("timetable") {
                val viewModel: TimetableDirectoryViewModel = viewModel(
                    factory = TimetableDirectoryViewModel.Factory(appContainer.apiService)
                )
                TimetableDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { },
                    onTimetableClick = { entry ->
                        // Pass JSON but detail screen will block edits
                        val json = java.net.URLEncoder.encode(Gson().toJson(entry), StandardCharsets.UTF_8.toString())
                        navController.navigate("timetable/detail/$json")
                    }
                )
            }

            composable("timetable/detail/{entryJson}") { backStackEntry ->
                val json = backStackEntry.arguments?.getString("entryJson") ?: ""
                val decodedJson = URLDecoder.decode(json, StandardCharsets.UTF_8.toString())
                val entry = Gson().fromJson(decodedJson, TimetableDisplay::class.java)

                val viewModel: TimetableDetailViewModel = viewModel(
                    factory = TimetableDetailViewModel.Factory(appContainer.apiService)
                )
                LaunchedEffect(entry) {
                    if (entry != null) {
                        viewModel.setEntry(entry)
                    }
                }
                TimetableDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { },
                    onDeleteSuccess = { }
                )
            }


            // -------------------------------------------------------------
            // ASSESSMENT SECTION
            // -------------------------------------------------------------
            composable("admin_assessment_landing") {
                AssessmentLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("examinations") {
                val viewModel: ExaminationsDirectoryViewModel = viewModel(
                    factory = ExaminationsDirectoryViewModel.Factory(appContainer.apiService)
                )
                ExaminationsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { },
                    onExaminationClick = { exam ->
                        navController.navigate("examinations/detail/${exam.examinationId}")
                    }
                )
            }

            composable("examinations/detail/{examinationId}") { backStackEntry ->
                val examinationId = backStackEntry.arguments?.getString("examinationId")?.toLongOrNull() ?: 0L
                val viewModel: ExaminationDetailViewModel = viewModel(
                    factory = ExaminationDetailViewModel.Factory(appContainer.apiService)
                )
                LaunchedEffect(examinationId) {
                    viewModel.loadExamination(examinationId)
                }
                ExaminationDetailScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            composable("assignments") {
                val viewModel: AssignmentsDirectoryViewModel = viewModel(
                    factory = AssignmentsDirectoryViewModel.Factory(appContainer.apiService)
                )
                AssignmentsDirectoryScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAssignmentClick = { assignment ->
                        navController.navigate("assignments/${assignment.assignmentId}")
                    }
                )
            }

            composable("assignments/{assignmentId}") { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getString("assignmentId")?.toLongOrNull() ?: 0L
                val viewModel: AssignmentDetailViewModel = viewModel(
                    factory = AssignmentDetailViewModel.Factory(appContainer.apiService, assignmentId)
                )
                AssignmentDetailScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("submissions") {
                val viewModel: SubmissionsDirectoryViewModel = viewModel(
                    factory = SubmissionsDirectoryViewModel.Factory(appContainer.apiService)
                )
                SubmissionsDirectoryScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onSubmissionClick = { submission ->
                        navController.navigate("submissions/${submission.submissionId}")
                    }
                )
            }

            composable("submissions/{submissionId}") { backStackEntry ->
                val submissionId = backStackEntry.arguments?.getString("submissionId")?.toLongOrNull() ?: 0L
                val viewModel: SubmissionDetailViewModel = viewModel(
                    factory = SubmissionDetailViewModel.Factory(appContainer.apiService, submissionId)
                )
                SubmissionDetailScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("marks") {
                val viewModel: MarksDirectoryViewModel = viewModel(
                    factory = MarksDirectoryViewModel.Factory(appContainer.apiService)
                )
                MarksDirectoryScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onMarkClick = { mark ->
                        navController.navigate("marks/${mark.markId}")
                    }
                )
            }

            composable("marks/{markId}") { backStackEntry ->
                val markId = backStackEntry.arguments?.getString("markId")?.toLongOrNull() ?: 0L
                val viewModel: MarkDetailViewModel = viewModel(
                    factory = MarkDetailViewModel.Factory(appContainer.apiService, markId)
                )
                MarkDetailScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // -------------------------------------------------------------
            // PEOPLE SECTION
            // -------------------------------------------------------------
            composable("admin_people_landing") {
                PeopleLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("students") {
                val studentsViewModel: com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel = viewModel(
                    factory = com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel.provideFactory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.students.StudentsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = studentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddStudentClick = { },
                    onStudentClick = { studentId -> navController.navigate("students/$studentId") }
                )
            }

            composable("students/{studentId}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel.provideFactory(studentId, appContainer.apiService)
                val profileViewModel: com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel = viewModel(factory = factory)

                com.school.gdsportal.ui.admin.students.profile.StudentProfileScreen(
                    isReadOnly = true,
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { },
                    onPersonalClick = { navController.navigate("students/$studentId/personal") },
                    onEnrollmentClick = { navController.navigate("students/$studentId/enrollment") },
                    onParentsClick = { navController.navigate("students/$studentId/parents") },
                    onEnrollClick = { },
                    onTransferClick = { }
                )
            }

            composable("students/{studentId}/personal") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel.provideFactory(studentId, appContainer.apiService)
                val personalViewModel: com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel = viewModel(factory = factory)

                com.school.gdsportal.ui.admin.students.personal.StudentPersonalScreen(
//                    isReadOnly = true,
                    viewModel = personalViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("students/{studentId}/enrollment") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel.provideFactory(studentId, appContainer.apiService)
                val enrollmentViewModel: com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel = viewModel(factory = factory)

                com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentScreen(
//                    isReadOnly = true,
                    viewModel = enrollmentViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("students/{studentId}/parents") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel.provideFactory(studentId, appContainer.apiService)
                val parentsViewModel: com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel = viewModel(factory = factory)

                com.school.gdsportal.ui.admin.students.parents.StudentParentsScreen(
                    isReadOnly = true,
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddParentClick = { }
                )
            }

            composable("principals") {
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryViewModel.Factory(appContainer.apiService)
                val principalsViewModel: com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = principalsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddPrincipalClick = { },
                    onPrincipalClick = { principalId -> navController.navigate("principals/$principalId") }
                )
            }

            composable("principals/{principalId}") { backStackEntry ->
                val principalIdStr = backStackEntry.arguments?.getString("principalId")
                val principalId = principalIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalProfileViewModel.Factory(principalId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.principals.PrincipalProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalProfileScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditPrincipalClick = { }
                )
            }

            composable("administrators") {
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryViewModel.Factory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddAdministratorClick = { },
                    onAdministratorClick = { adminId -> navController.navigate("administrators/$adminId") }
                )
            }

            composable("administrators/{adminId}") { backStackEntry ->
                val adminIdStr = backStackEntry.arguments?.getString("adminId")
                val adminId = adminIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorProfileViewModel.Factory(adminId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorProfileScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditAdministratorClick = { }
                )
            }

            composable("parents") {
                val factory = com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModelFactory(appContainer.apiService)
                val parentsViewModel: com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentsDirectoryScreen(
                    isReadOnly = true,
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onParentClick = { parentId -> navController.navigate("parents/$parentId") }
                )
            }

            composable("parents/{parentId}") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(parentId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentProfileScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onViewAllStudentsClick = { navController.navigate("parents/$parentId/students") },
                    onParentInformationClick = { navController.navigate("parents/$parentId/information") },
                    onEditParentClick = { }
                )
            }

            composable("parents/{parentId}/information") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(parentId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentInformationScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("parents/{parentId}/students") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(parentId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentStudentsScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("teachers") {
                val factory = com.school.gdsportal.ui.admin.teachers.TeachersDirectoryViewModel.Factory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.TeachersDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.TeachersDirectoryScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddTeacherClick = { },
                    onTeacherClick = { teacherId -> navController.navigate("teachers/$teacherId") }
                )
            }

            composable("teachers/{teacherId}") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.TeacherProfileViewModel.Factory(teacherId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.TeacherProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.TeacherProfileScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditTeacherClick = { },
                    onPersonalInformationClick = { navController.navigate("teachers/$teacherId/information") },
                    onTeachingAssignmentsClick = { navController.navigate("teachers/$teacherId/subjects") },
                    onClassTeacherClick = { navController.navigate("teachers/$teacherId/class-teacher") }
                )
            }

            composable("teachers/{teacherId}/information") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalViewModel.Companion.provideFactory(teacherId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { }
                )
            }

            composable("teachers/{teacherId}/subjects") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsViewModel.Companion.provideFactory(teacherId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignClick = { }
                )
            }

            composable("teachers/{teacherId}/class-teacher") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel.Companion.provideFactory(teacherId, appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherScreen(
                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignClick = { }
                )
            }


            // -------------------------------------------------------------
            // ATTENDANCE & COMMUNICATION SECTION
            // -------------------------------------------------------------
            composable("admin_attendance_landing") {
                AttendanceLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("student-attendance") {
                val viewModel: com.school.gdsportal.ui.admin.attendance.student.StudentAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.attendance.student.StudentAttendanceViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.attendance.student.StudentAttendanceScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("admin_teacher_attendance") {
                val factory = com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceViewModel.Companion.provideFactory(
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceScreen(
//                    isReadOnly = true,
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("admin_communication_landing") {
                CommunicationLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("announcements") {
                val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDirectoryViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onCreateClick = { navController.navigate("announcements/create") },
                    onAnnouncementClick = { id -> navController.navigate("announcements/$id") }
                )
            }

            composable("announcements/create") {
                val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditViewModel.Factory(appContainer.apiService, null)
                )
                com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSuccess = { id ->
                        navController.popBackStack()
                        if (id != null) navController.navigate("announcements/$id")
                    }
                )
            }

            composable(
                route = "announcements/{announcementId}",
                arguments = listOf(navArgument("announcementId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("announcementId") ?: return@composable
                val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailViewModel.Factory(appContainer.apiService, id)
                )
                com.school.gdsportal.ui.admin.communication.announcements.AnnouncementDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate("announcements/$id/edit") }
                )
            }

            composable(
                route = "announcements/{announcementId}/edit",
                arguments = listOf(navArgument("announcementId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("announcementId") ?: return@composable
                val viewModel: com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditViewModel.Factory(appContainer.apiService, id)
                )
                com.school.gdsportal.ui.admin.communication.announcements.AnnouncementCreateEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSuccess = { navController.navigateUp() }
                )
            }

            composable("notifications") {
                val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.communication.notifications.NotificationsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onCreateClick = { navController.navigate("notifications/create") },
                    onNotificationClick = { id -> navController.navigate("notifications/$id") }
                )
            }

            composable("notifications/create") {
                val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationCreateViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationCreateViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.communication.notifications.NotificationCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSuccess = { navController.navigateUp() }
                )
            }

            composable(
                route = "notifications/{notificationId}",
                arguments = listOf(navArgument("notificationId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("notificationId") ?: return@composable
                val viewModel: com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailViewModel.Factory(appContainer.apiService, id)
                )
                com.school.gdsportal.ui.admin.communication.notifications.NotificationDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // -------------------------------------------------------------
            // REPORTS SECTION
            // -------------------------------------------------------------
            // All reports are strictly Read-Only, no parameter needed unless they mutate data.
            composable("admin_reports_landing") {
                ReportsLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("reports/student-performance") {
                val viewModel: com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceSelectorViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceSelectorScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReport = { studentId, academicYearId ->
                        navController.navigate("reports/student-performance/$studentId/$academicYearId")
                    }
                )
            }

            composable("reports/student-performance/{studentId}/{academicYearId}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val academicYearId = backStackEntry.arguments?.getString("academicYearId")?.toIntOrNull() ?: 0
                val viewModel: com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceReportViewModel.Factory(
                        appContainer.apiService, studentId, academicYearId
                    )
                )
                com.school.gdsportal.ui.admin.reports.studentperformance.StudentPerformanceReportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("reports/teacher-attendance") {
                val viewModel: com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportSelectorViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportSelectorScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReportClick = { teacherId, month, year ->
                        navController.navigate("reports/teacher-attendance/$teacherId/$month/$year")
                    }
                )
            }

            composable("reports/teacher-attendance/{teacherId}/{month}/{year}") { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getString("teacherId")?.toLongOrNull() ?: 0L
                val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 0
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: 0
                val viewModel: com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportViewModel.Factory(
                        appContainer.apiService, teacherId, month, year
                    )
                )
                com.school.gdsportal.ui.admin.reports.teacherattendance.TeacherAttendanceReportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("reports/class-attendance") {
                val viewModel: com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportSelectorViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportSelectorScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReportClick = { sectionId, month, year, classId, academicYearId ->
                        navController.navigate("reports/class-attendance/$sectionId/$month/$year/$classId/$academicYearId")
                    }
                )
            }

            composable("reports/class-attendance/{sectionId}/{month}/{year}/{classId}/{academicYearId}") { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getString("sectionId")?.toIntOrNull() ?: 0
                val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 0
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: 0
                val classId = backStackEntry.arguments?.getString("classId")?.toIntOrNull() ?: 0
                val academicYearId = backStackEntry.arguments?.getString("academicYearId")?.toIntOrNull() ?: 0
                val viewModel: com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportViewModel.Factory(
                        appContainer.apiService, sectionId, month, year, classId, academicYearId
                    )
                )
                com.school.gdsportal.ui.admin.reports.classattendance.ClassAttendanceReportScreen(
                    viewModel = viewModel,
                    month = month,
                    year = year,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("reports/teacher-performance") {
                val viewModel: com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportSelectorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportSelectorViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportSelectorScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReportClick = { teacherId, academicYearId ->
                        navController.navigate("reports/teacher-performance/$teacherId/$academicYearId")
                    }
                )
            }

            composable("reports/teacher-performance/{teacherId}/{academicYearId}") { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getString("teacherId")?.toLongOrNull() ?: 0L
                val academicYearId = backStackEntry.arguments?.getString("academicYearId")?.toIntOrNull() ?: 0
                val viewModel: com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportViewModel.Factory(
                        appContainer.apiService, teacherId, academicYearId
                    )
                )
                com.school.gdsportal.ui.admin.reports.teacherperformance.TeacherPerformanceReportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("reports/examination") {
                val viewModel: com.school.gdsportal.ui.admin.reports.examination.ExaminationReportSelectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.examination.ExaminationReportSelectionViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.examination.ExaminationReportSelectionScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReportClick = { examinationId ->
                        navController.navigate("reports/examination/$examinationId")
                    }
                )
            }

            composable("reports/examination/{examinationId}") { backStackEntry ->
                val examinationId = backStackEntry.arguments?.getString("examinationId")?.toLongOrNull() ?: 0L
                val viewModel: com.school.gdsportal.ui.admin.reports.examination.ExaminationReportViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.examination.ExaminationReportViewModel.Factory(
                        appContainer.apiService, examinationId
                    )
                )
                com.school.gdsportal.ui.admin.reports.examination.ExaminationReportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("reports/student-attendance-summary") {
                val viewModel: com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummarySelectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummarySelectionViewModel.Factory(appContainer.apiService)
                )
                com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummarySelectionScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onGenerateReportClick = { studentId, academicYearId, studentName, regNum ->
                        navController.navigate("reports/student-attendance-summary/$studentId/$academicYearId/${android.net.Uri.encode(studentName)}/${android.net.Uri.encode(regNum)}")
                    }
                )
            }

            composable("reports/student-attendance-summary/{studentId}/{academicYearId}/{studentName}/{regNum}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val academicYearId = backStackEntry.arguments?.getString("academicYearId")?.toIntOrNull() ?: 0
                val studentName = android.net.Uri.decode(backStackEntry.arguments?.getString("studentName") ?: "")
                val regNum = android.net.Uri.decode(backStackEntry.arguments?.getString("regNum") ?: "")
                val viewModel: com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummaryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummaryViewModel.Factory(
                        appContainer.apiService, studentId, academicYearId
                    )
                )
                com.school.gdsportal.ui.admin.reports.studentattendancesummary.StudentAttendanceSummaryScreen(
                    viewModel = viewModel,
                    studentName = studentName,
                    regNum = regNum,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // -------------------------------------------------------------
            // PROFILE SECTION
            // -------------------------------------------------------------
            composable("admin_profile_landing") {
                ProfileLandingScreen(
                    onBack = { openDrawer() },
                    onNavigate = { route ->
                        when (route) {
                            "admin_my_profile" -> navController.navigate("admin_my_profile")
                            "admin_change_password" -> navController.navigate("admin_change_password")
                            "logout_action" -> onLogout()
                        }
                    },
                    onLogout = onLogout
                )
            }

            composable("admin_my_profile") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.provideFactory(appContainer.apiService)
                )
                ProfileScreen(
//                    isReadOnly = true,
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("admin_change_password") {
                val changePasswordViewModel: ChangePasswordViewModel = viewModel(
                    factory = ChangePasswordViewModel.provideFactory(appContainer.apiService)
                )
                ChangePasswordScreen(
                    viewModel = changePasswordViewModel,
                    onBackClick = { navController.navigateUp()}
                )
            }
        }
    }
}