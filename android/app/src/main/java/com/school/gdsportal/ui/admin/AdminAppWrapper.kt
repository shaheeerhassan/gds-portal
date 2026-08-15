package com.school.gdsportal.ui.admin

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
import com.school.gdsportal.ui.admin.components.AdminDrawerContent
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
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardScreen
import com.school.gdsportal.ui.admin.dashboard.AdminDashboardViewModel
import com.school.gdsportal.ui.admin.PeopleLandingScreen
import com.school.gdsportal.ui.admin.administrators.*
import com.school.gdsportal.ui.admin.principals.*
import kotlinx.coroutines.launch

@Composable
fun AdminAppWrapper(
    appContainer: AppContainer,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "admin_dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) {
        val openDrawer: () -> Unit = {
            coroutineScope.launch { drawerState.open() }
        }

        NavHost(navController = navController, startDestination = "admin_dashboard") {
            composable("admin_dashboard") {
                val dashboardViewModel: AdminDashboardViewModel = viewModel(
                    factory = AdminDashboardViewModel.provideFactory(
                        appContainer.apiService,
                        appContainer.tokenManager
                    )
                )
                AdminDashboardScreen(
                    viewModel = dashboardViewModel,
                    onMenuClick = openDrawer
                )
            }
            
            // CLASSES
            composable("classes") {
                val viewModel: ClassesDirectoryViewModel = viewModel(
                    factory = ClassesDirectoryViewModel.Factory(appContainer.apiService)
                )
                ClassesDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("admin_academics_landing") { popUpTo("admin_academics_landing") } },
                    onAddClick = { navController.navigate("classes/create") },
                    onClassClick = { id -> navController.navigate("classes/$id") }
                )
            }

            composable("classes/create") {
                val viewModel: ClassCreateViewModel = viewModel(
                    factory = ClassCreateViewModel.provideFactory(appContainer.apiService)
                )
                ClassCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onClassCreated = { id ->
                        if (id != null) {
                            navController.navigate("classes/$id") {
                                popUpTo("classes")
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }
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
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("classes") {
                            popUpTo("admin_academics_landing")
                        }
                    },
                    onEditClick = { navController.navigate("classes/$classId/edit") }
                )
            }

            composable(
                route = "classes/{classId}/edit",
                arguments = listOf(navArgument("classId") { type = NavType.IntType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getInt("classId") ?: return@composable
                val viewModel: ClassEditViewModel = viewModel(
                    factory = ClassEditViewModel.Factory(classId, appContainer.apiService)
                )
                ClassEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onClassEdited = { navController.navigateUp() }
                )
            }
            
            // SECTIONS
            composable("sections") {
                val viewModel: SectionsDirectoryViewModel = viewModel(
                    factory = SectionsDirectoryViewModel.Factory(appContainer.apiService)
                )
                SectionsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("admin_academics_landing") { popUpTo("admin_academics_landing") } },
                    onAddClick = { navController.navigate("sections/create") },
                    onSectionClick = { id -> navController.navigate("sections/$id") }
                )
            }

            composable("sections/create") {
                val viewModel: SectionCreateViewModel = viewModel(
                    factory = SectionCreateViewModel.provideFactory(appContainer.apiService)
                )
                SectionCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSectionCreated = { id ->
                        if (id != null) {
                            navController.navigate("sections/$id") {
                                popUpTo("sections")
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }
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
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("sections") {
                            popUpTo("admin_academics_landing")
                        }
                    },
                    onEditClick = { navController.navigate("sections/$sectionId/edit") }
                )
            }

            composable(
                route = "sections/{sectionId}/edit",
                arguments = listOf(navArgument("sectionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getInt("sectionId") ?: return@composable
                val viewModel: SectionEditViewModel = viewModel(
                    factory = SectionEditViewModel.Factory(sectionId, appContainer.apiService)
                )
                SectionEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSectionEdited = { navController.navigateUp() }
                )
            }

            // SUBJECTS
            composable("subjects") {
                val viewModel: SubjectsDirectoryViewModel = viewModel(
                    factory = SubjectsDirectoryViewModel.Factory(appContainer.apiService)
                )
                SubjectsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("admin_academics_landing") { popUpTo("admin_academics_landing") } },
                    onAddClick = { navController.navigate("subjects/create") },
                    onSubjectClick = { id -> navController.navigate("subjects/$id") }
                )
            }

            composable("subjects/create") {
                val viewModel: SubjectCreateViewModel = viewModel(
                    factory = SubjectCreateViewModel.provideFactory(appContainer.apiService)
                )
                SubjectCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubjectCreated = { id ->
                        if (id != null) {
                            navController.navigate("subjects/$id") {
                                popUpTo("subjects")
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }
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
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("subjects") {
                            popUpTo("admin_academics_landing")
                        }
                    },
                    onEditClick = { navController.navigate("subjects/$subjectId/edit") }
                )
            }

            composable(
                route = "subjects/{subjectId}/edit",
                arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: return@composable
                val viewModel: SubjectEditViewModel = viewModel(
                    factory = SubjectEditViewModel.Factory(subjectId, appContainer.apiService)
                )
                SubjectEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubjectEdited = { navController.navigateUp() }
                )
            }

            // PERIODS
            composable("periods") {
                val viewModel: PeriodsDirectoryViewModel = viewModel(
                    factory = PeriodsDirectoryViewModel.Factory(appContainer.apiService)
                )
                PeriodsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("admin_academics_landing") { popUpTo("admin_academics_landing") } },
                    onAddClick = { navController.navigate("periods/create") },
                    onPeriodClick = { id -> navController.navigate("periods/$id") }
                )
            }

            composable("periods/create") {
                val viewModel: PeriodCreateViewModel = viewModel(
                    factory = PeriodCreateViewModel.provideFactory(appContainer.apiService)
                )
                PeriodCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onPeriodCreated = { id ->
                        if (id != null) {
                            navController.navigate("periods/$id") {
                                popUpTo("periods")
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }
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
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("periods") {
                            popUpTo("admin_academics_landing")
                        }
                    },
                    onEditClick = { navController.navigate("periods/$periodId/edit") }
                )
            }

            composable(
                route = "periods/{periodId}/edit",
                arguments = listOf(navArgument("periodId") { type = NavType.IntType })
            ) { backStackEntry ->
                val periodId = backStackEntry.arguments?.getInt("periodId") ?: return@composable
                val viewModel: PeriodEditViewModel = viewModel(
                    factory = PeriodEditViewModel.Factory(periodId, appContainer.apiService)
                )
                PeriodEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onPeriodEdited = { navController.navigateUp() }
                )
            }

            // -------------------------------------------------------------
            // TEACHING SECTION
            // -------------------------------------------------------------
            composable("admin_teaching_landing") {
                TeachingLandingScreen(
                    onBack = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // CLASS TEACHERS
            composable("class-teachers") {
                val viewModel: ClassTeachersDirectoryViewModel = viewModel(
                    factory = ClassTeachersDirectoryViewModel.Factory(appContainer.apiService)
                )
                ClassTeachersDirectoryScreen(
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { navController.navigate("class-teachers/create") },
                    onAssignmentClick = { sectionId, yearId ->
                        navController.navigate("class-teachers/$sectionId/$yearId")
                    }
                )
            }

            composable("class-teachers/create") {
                val viewModel: ClassTeacherAssignmentViewModel = viewModel(
                    factory = ClassTeacherAssignmentViewModel.Factory(null, null, appContainer.apiService)
                )
                ClassTeacherAssignmentScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignmentSaved = { navController.navigateUp() }
                )
            }

            composable(
                route = "class-teachers/{sectionId}/{academicYearId}",
                arguments = listOf(
                    navArgument("sectionId") { type = NavType.IntType },
                    navArgument("academicYearId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val sectionId = backStackEntry.arguments?.getInt("sectionId") ?: return@composable
                val yearId = backStackEntry.arguments?.getInt("academicYearId") ?: return@composable
                
                val viewModel: ClassTeacherAssignmentViewModel = viewModel(
                    factory = ClassTeacherAssignmentViewModel.Factory(sectionId, yearId, appContainer.apiService)
                )
                ClassTeacherAssignmentScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignmentSaved = { navController.navigateUp() }
                )
            }

            // TEACHER CLASSES
            composable("teacher-classes") {
                val viewModel: TeacherClassesViewModel = viewModel(
                    factory = TeacherClassesViewModel.Factory(appContainer.apiService)
                )
                TeacherClassesScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onTeacherClick = { teacherId ->
                        navController.navigate("teachers/$teacherId")
                    }
                )
            }

            // TEACHER SUBJECTS
            composable("teacher-subjects") {
                val viewModel: TeacherSubjectsViewModel = viewModel(
                    factory = TeacherSubjectsViewModel.Factory(appContainer.apiService)
                )
                TeacherSubjectsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onTeacherClick = { teacherId ->
                        navController.navigate("teachers/$teacherId")
                    }
                )
            }
            
            // TIMETABLE
            composable("timetable") {
                val viewModel: TimetableDirectoryViewModel = viewModel(
                    factory = TimetableDirectoryViewModel.Factory(appContainer.apiService)
                )
                TimetableDirectoryScreen(
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { navController.navigate("timetable/create") },
                    onTimetableClick = { entry ->
                        val json = URLEncoder.encode(Gson().toJson(entry), StandardCharsets.UTF_8.toString())
                        navController.navigate("timetable/detail/$json")
                    }
                )
            }

            composable("timetable/create") {
                val viewModel: TimetableFormViewModel = viewModel(
                    factory = TimetableFormViewModel.Factory(appContainer.apiService)
                )
                // Try to get preselected context from previous screen if we want (optional)
                LaunchedEffect(Unit) {
                    viewModel.loadInitialData(null, null, null, null)
                }
                TimetableFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubmitSuccess = { navController.navigateUp() }
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
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { e ->
                        val eJson = URLEncoder.encode(Gson().toJson(e), StandardCharsets.UTF_8.toString())
                        navController.navigate("timetable/edit/$eJson")
                    },
                    onDeleteSuccess = { navController.navigateUp() }
                )
            }

            composable("timetable/edit/{entryJson}") { backStackEntry ->
                val json = backStackEntry.arguments?.getString("entryJson") ?: ""
                val decodedJson = URLDecoder.decode(json, StandardCharsets.UTF_8.toString())
                val entry = Gson().fromJson(decodedJson, TimetableDisplay::class.java)

                val viewModel: TimetableFormViewModel = viewModel(
                    factory = TimetableFormViewModel.Factory(appContainer.apiService)
                )
                LaunchedEffect(entry) {
                    if (entry != null) {
                        viewModel.loadInitialData(null, null, null, entry)
                    }
                }
                TimetableFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubmitSuccess = { navController.popBackStack("timetable", false) }
                )
            }

            // EXAMINATIONS
            composable("examinations") {
                val viewModel: ExaminationsDirectoryViewModel = viewModel(
                    factory = ExaminationsDirectoryViewModel.Factory(appContainer.apiService)
                )
                ExaminationsDirectoryScreen(
                    viewModel = viewModel,
                    onMenuClick = { navController.navigateUp() },
                    onAddClick = { navController.navigate("examinations/create") },
                    onExaminationClick = { exam ->
                        navController.navigate("examinations/detail/${exam.examinationId}")
                    }
                )
            }

            composable("examinations/create") {
                val viewModel: ExaminationFormViewModel = viewModel(
                    factory = ExaminationFormViewModel.Factory(appContainer.apiService)
                )
                LaunchedEffect(Unit) {
                    viewModel.loadInitialData(null)
                }
                ExaminationFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubmitSuccess = { navController.navigateUp() }
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
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { id ->
                        navController.navigate("examinations/edit/$id")
                    }
                )
            }

            composable("examinations/edit/{examinationId}") { backStackEntry ->
                val examinationId = backStackEntry.arguments?.getString("examinationId")?.toLongOrNull() ?: 0L
                val viewModel: ExaminationFormViewModel = viewModel(
                    factory = ExaminationFormViewModel.Factory(appContainer.apiService)
                )
                LaunchedEffect(examinationId) {
                    viewModel.loadInitialData(examinationId)
                }
                ExaminationFormScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onSubmitSuccess = { navController.popBackStack("examinations", false) }
                )
            }

            // ASSIGNMENTS
            composable("assignments") {
                val viewModel: AssignmentsDirectoryViewModel = viewModel(
                    factory = AssignmentsDirectoryViewModel.Factory(appContainer.apiService)
                )
                AssignmentsDirectoryScreen(
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
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // -------------------------------------------------------------
            // ACADEMICS SECTION
            // -------------------------------------------------------------
            composable("admin_academics_landing") {
                AcademicsLandingScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    onBack = { navController.navigateUp() }
                )
            }
            
            composable("academic-years") {
                val viewModel: AcademicYearsDirectoryViewModel = viewModel(
                    factory = AcademicYearsDirectoryViewModel.Factory(appContainer.apiService)
                )
                AcademicYearsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("admin_academics_landing") { popUpTo("admin_academics_landing") } },
                    onAddClick = { navController.navigate("academic-years/create") },
                    onYearClick = { id -> navController.navigate("academic-years/$id") }
                )
            }
            
            composable("academic-years/create") {
                val viewModel: AcademicYearCreateViewModel = viewModel(
                    factory = AcademicYearCreateViewModel.provideFactory(appContainer.apiService)
                )
                AcademicYearCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onAcademicYearCreated = { id ->
                        if (id != null) {
                            navController.navigate("academic-years/$id") {
                                popUpTo("academic-years")
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }
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
                    viewModel = viewModel,
                    onBackClick = { navController.navigate("academic-years") },
                    onEditClick = { navController.navigate("academic-years/$id/edit") }
                )
            }
            
            composable(
                route = "academic-years/{academicYearId}/edit",
                arguments = listOf(navArgument("academicYearId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("academicYearId") ?: 0
                val viewModel: AcademicYearEditViewModel = viewModel(
                    factory = AcademicYearEditViewModel.provideFactory(id, appContainer.apiService)
                )
                AcademicYearEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("admin_people_landing") {
                PeopleLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }


            // Student Module Routes
            composable("students") {
                val studentsViewModel: com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel = viewModel(
                    factory = com.school.gdsportal.ui.admin.students.StudentsDirectoryViewModel.provideFactory(
                        appContainer.apiService
                    )
                )
                com.school.gdsportal.ui.admin.students.StudentsDirectoryScreen(
                    viewModel = studentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddStudentClick = { navController.navigate("students/create") },
                    onStudentClick = { studentId -> navController.navigate("students/$studentId") }
                )
            }
            composable("students/create") {
                val factory = com.school.gdsportal.ui.admin.students.create.StudentCreateViewModel.provideFactory(
                    apiService = appContainer.apiService
                )
                val createViewModel: com.school.gdsportal.ui.admin.students.create.StudentCreateViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.create.StudentCreateScreen(
                    viewModel = createViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val profileViewModel: com.school.gdsportal.ui.admin.students.profile.StudentProfileViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.profile.StudentProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate("students/$studentId/edit") },
                    onPersonalClick = { navController.navigate("students/$studentId/personal") },
                    onEnrollmentClick = { navController.navigate("students/$studentId/enrollment") },
                    onParentsClick = { navController.navigate("students/$studentId/parents") },
                    onEnrollClick = { navController.navigate("students/$studentId/enroll") },
                    onTransferClick = { navController.navigate("students/$studentId/transfer") }
                )
            }
            composable("students/{studentId}/personal") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val personalViewModel: com.school.gdsportal.ui.admin.students.personal.StudentPersonalViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.personal.StudentPersonalScreen(
                    viewModel = personalViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/edit") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.edit.StudentEditViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val editViewModel: com.school.gdsportal.ui.admin.students.edit.StudentEditViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.edit.StudentEditScreen(
                    viewModel = editViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/enrollment") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val enrollmentViewModel: com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.enrollment.StudentEnrollmentScreen(
                    viewModel = enrollmentViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/enroll") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.enroll.StudentEnrollViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val enrollViewModel: com.school.gdsportal.ui.admin.students.enroll.StudentEnrollViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.enroll.StudentEnrollScreen(
                    viewModel = enrollViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/transfer") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.transfer.StudentTransferViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val transferViewModel: com.school.gdsportal.ui.admin.students.transfer.StudentTransferViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.transfer.StudentTransferScreen(
                    viewModel = transferViewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/promote") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Promote Student ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/end-enrollment") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "End Enrollment ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/roll-number") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Change Roll Number ($studentId)", onBack = { navController.navigateUp() })
            }
            composable("students/{studentId}/parents") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val parentsViewModel: com.school.gdsportal.ui.admin.students.parents.StudentParentsViewModel = viewModel(factory = factory)
                
                com.school.gdsportal.ui.admin.students.parents.StudentParentsScreen(
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddParentClick = { navController.navigate("students/$studentId/parents/add") }
                )
            }
            composable("students/{studentId}/parents/add") { backStackEntry ->
                val studentIdStr = backStackEntry.arguments?.getString("studentId")
                val studentId = studentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.students.parents.AddParentViewModel.Companion.provideFactory(
                    studentId = studentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.students.parents.AddParentViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.students.parents.AddParentScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("students/{studentId}/parents/link") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                StudentPlaceholderScreen(title = "Link Parent ($studentId)", onBack = { navController.navigateUp() })
            }

            // Principals Module Routes
            composable("principals") {
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryViewModel.Factory(appContainer.apiService)
                val principalsViewModel: com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalsDirectoryScreen(
                    viewModel = principalsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddPrincipalClick = { navController.navigate("principals/create") },
                    onPrincipalClick = { principalId ->
                        navController.navigate("principals/$principalId")
                    }
                )
            }
            composable("principals/create") {
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalCreateViewModel.provideFactory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.principals.PrincipalCreateViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onPrincipalCreated = { principalId ->
                        navController.popBackStack()
                        navController.navigate("principals/$principalId")
                    }
                )
            }
            composable("principals/{principalId}") { backStackEntry ->
                val principalIdStr = backStackEntry.arguments?.getString("principalId")
                val principalId = principalIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalProfileViewModel.Factory(
                    principalId = principalId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.principals.PrincipalProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditPrincipalClick = { navController.navigate("principals/$principalId/edit") }
                )
            }
            composable("principals/{principalId}/edit") { backStackEntry ->
                val principalIdStr = backStackEntry.arguments?.getString("principalId")
                val principalId = principalIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.principals.PrincipalEditViewModel.provideFactory(
                    principalId = principalId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.principals.PrincipalEditViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.principals.PrincipalEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            
            // Administrators Module Routes
            composable("administrators") {
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryViewModel.Factory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorsDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddAdministratorClick = { navController.navigate("administrators/create") },
                    onAdministratorClick = { adminId -> navController.navigate("administrators/$adminId") }
                )
            }
            composable("administrators/create") {
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorCreateViewModel.provideFactory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorCreateViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAdministratorCreated = { adminId ->
                        navController.popBackStack()
                        if (adminId != null) {
                            navController.navigate("administrators/$adminId")
                        } else {
                            navController.navigate("administrators")
                        }
                    }
                )
            }
            composable("administrators/{adminId}") { backStackEntry ->
                val adminIdStr = backStackEntry.arguments?.getString("adminId")
                val adminId = adminIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorProfileViewModel.Factory(
                    adminId = adminId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditAdministratorClick = { navController.navigate("administrators/$adminId/edit") }
                )
            }
            composable("administrators/{adminId}/edit") { backStackEntry ->
                val adminIdStr = backStackEntry.arguments?.getString("adminId")
                val adminId = adminIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.administrators.AdministratorEditViewModel.provideFactory(
                    adminId = adminId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.administrators.AdministratorEditViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.administrators.AdministratorEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Parent Module Routes
            composable("parents") {
                val factory = com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModelFactory(appContainer.apiService)
                val parentsViewModel: com.school.gdsportal.ui.admin.parents.ParentsDirectoryViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentsDirectoryScreen(
                    viewModel = parentsViewModel,
                    onBackClick = { navController.navigateUp() },
                    onParentClick = { parentId ->
                        navController.navigate("parents/$parentId")
                    }
                )
            }
            composable("parents/{parentId}") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onViewAllStudentsClick = { navController.navigate("parents/$parentId/students") },
                    onParentInformationClick = { navController.navigate("parents/$parentId/information") },
                    onEditParentClick = { navController.navigate("parents/$parentId/edit") }
                )
            }
            composable("parents/{parentId}/information") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentInformationScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("parents/{parentId}/edit") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentEditViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentEditViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }
            composable("parents/{parentId}/students") { backStackEntry ->
                val parentIdStr = backStackEntry.arguments?.getString("parentId")
                val parentId = parentIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.parents.ParentProfileViewModel.Companion.provideFactory(
                    parentId = parentId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.parents.ParentProfileViewModel = viewModel(factory = factory)
                com.school.gdsportal.ui.admin.parents.ParentStudentsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Teacher Module Routes
            composable("teachers") {
                val factory = com.school.gdsportal.ui.admin.teachers.TeachersDirectoryViewModel.Factory(appContainer.apiService)
                val viewModel: com.school.gdsportal.ui.admin.teachers.TeachersDirectoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.TeachersDirectoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAddTeacherClick = { navController.navigate("teachers/create") },
                    onTeacherClick = { teacherId -> navController.navigate("teachers/$teacherId") }
                )
            }
            composable("teachers/create") {
                val factory = com.school.gdsportal.ui.admin.teachers.create.TeacherCreateViewModel.Companion.provideFactory(
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.create.TeacherCreateViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.create.TeacherCreateScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onNavigateToTeacher = { createdTeacherId ->
                        navController.navigate("teachers/$createdTeacherId") {
                            popUpTo("teachers") { inclusive = false }
                        }
                    }
                )
            }
            composable("teachers/{teacherId}") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.TeacherProfileViewModel.Factory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.TeacherProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.TeacherProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditTeacherClick = { navController.navigate("teachers/$teacherId/edit") },
                    onPersonalInformationClick = { navController.navigate("teachers/$teacherId/information") },
                    onTeachingAssignmentsClick = { navController.navigate("teachers/$teacherId/subjects") },
                    onClassTeacherClick = { navController.navigate("teachers/$teacherId/class-teacher") }
                )
            }
            composable("teachers/{teacherId}/information") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.information.TeacherPersonalScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate("teachers/$teacherId/edit") }
                )
            }
            composable("teachers/{teacherId}/edit") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.edit.TeacherEditViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.edit.TeacherEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.edit.TeacherEditScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable("teachers/{teacherId}/subjects") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherSubjectsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignClick = { navController.navigate("teachers/$teacherId/subjects/assign") }
                )
            }
            composable("teachers/{teacherId}/subjects/assign") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherAssignSubjectViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherAssignSubjectViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.subjects.TeacherAssignSubjectScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignSuccess = { navController.navigateUp() }
                )
            }
            composable("teachers/{teacherId}/class-teacher") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignClick = { navController.navigate("teachers/$teacherId/class-teacher/assign") }
                )
            }
            composable("teachers/{teacherId}/class-teacher/assign") { backStackEntry ->
                val teacherIdStr = backStackEntry.arguments?.getString("teacherId")
                val teacherId = teacherIdStr?.toLongOrNull() ?: 0L
                // We could potentially share the ViewModel, but getting it from the nav graph requires hilt/navGraphViewModels.
                // Recreating it here is fine since it's lightweight, or we can share via remember.
                // The instructions say "prefer a dedicated form route". Re-instantiating the VM will load data again, which is perfectly acceptable.
                val factory = com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel.Companion.provideFactory(
                    teacherId = teacherId,
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.teaching.class_teacher.ClassTeacherAssignScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() },
                    onAssignSuccess = { navController.navigateUp() }
                )
            }
            composable("admin_teacher_attendance") {
                val factory = com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceViewModel.Companion.provideFactory(
                    apiService = appContainer.apiService
                )
                val viewModel: com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                com.school.gdsportal.ui.admin.teachers.attendance.TeacherAttendanceScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.navigateUp() }
                )
            }


            


            composable("admin_assessment_landing") {
                AssessmentLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("admin_attendance_landing") {
                AttendanceLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable("admin_communication_landing") {
                CommunicationLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_reports_landing") {
                ReportsLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ }
                )
            }

            composable("admin_profile_landing") {
                ProfileLandingScreen(
                    onBack = { navController.navigateUp() },
                    onNavigate = { /* TODO Phase 2 */ },
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun StudentPlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon")
    }
}

@Composable
private fun ParentPlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon")
    }
}

@Composable
private fun TeacherPlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon")
    }
}
