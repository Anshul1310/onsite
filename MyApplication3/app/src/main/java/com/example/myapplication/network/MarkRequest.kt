package com.example.myapplication.network

data class MarkRequest(
    val studentId: String,
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
