package com.school.gdsportal.data.remote

data class CreateParentRequest(
    val email: String,
    val username: String,
    val password: String,
    val parent: ParentDetails
)

data class ParentDetails(
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val occupation: String?
)
