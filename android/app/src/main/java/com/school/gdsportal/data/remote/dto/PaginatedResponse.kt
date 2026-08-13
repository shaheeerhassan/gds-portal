package com.school.gdsportal.data.remote.dto

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean
)
