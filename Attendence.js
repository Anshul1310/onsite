import mongoose from "mongoose";

const studentSchema = new mongoose.Schema(
    {
        rollNo: {
            type: String,
            required: true,
            unique: true,
        },
        name: {
            type: String,
            required: true,
        },
        email: {
            type: String,
            required: true,
            unique: true,
        },
        department: String,
        year: Number,
    },
    { timestamps: true }
);

export default mongoose.model("Student", studentSchema);