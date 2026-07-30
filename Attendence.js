const mongoose = require("mongoose");

const attendanceSchema = new mongoose.Schema(
    {
        student: { type: mongoose.Schema.Types.ObjectId, ref: "Student", required: true },
        qrSession: { type: mongoose.Schema.Types.ObjectId, ref: "QrSession", required: true }
    },
    { timestamps: true }
);

module.exports = mongoose.models.Attendance || mongoose.model("Attendance", attendanceSchema);