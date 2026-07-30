package com.example.myapplication.network

data class LoginRequest(
    val rollNo: String,
    val name: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val message: String? = null,
    val student: Student? = null
)

data class MarkRequest(
    val token: String
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
