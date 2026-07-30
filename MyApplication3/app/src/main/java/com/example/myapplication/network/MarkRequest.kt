package com.example.myapplication.network

data class MarkRequest(
    val token: String
)

data class DAuthRequest(
    val code: String? = null,
    val rollNo: String? = null,
    val name: String? = null,
    val email: String? = null,
    val department: String? = null,
    val year: Int? = null
)

data class DAuthResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val student: Student? = null
)

data class Student(
    val _id: String,
    val name: String,
    val rollNo: String,
    val email: String,
    val department: String,
    val year: Int
)

data class Attendance(
    val _id: String,
    val student: Student,
    val createdAt: String
)

data class AttendanceResponse(
    val success: Boolean,
    val attendance: List<Attendance>
)

data class AttendanceCountResponse(
    val success: Boolean,
    val student: Student,
    val count: Int
)
