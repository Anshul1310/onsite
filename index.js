const express = require("express")
const mongoose = require("mongoose")
const app = express()
const fs = require("fs");
const crypto = require("crypto");
const QRCode = require("qrcode");
const path = require("path")
const Student = require("./Student.js")
const QrSession = require("./QrSession.js")
const Attendance = require("./Attendence.js")
app.use(express.static(path.join(process.cwd(), "public")));

const connectDB = async () => {
    try {
        await mongoose.connect("mongodb+srv://anshul:anshul@delta.ceopbox.mongodb.net/");
        console.log("MongoDB Connected");
    } catch (err) {
        console.error(err.message);
        process.exit(1);
    }
};



app.get("/health", (req, res) => {
    res.send("Hello World!")
})
app.use(express.json())

app.post("/generate", async (req, res) => {
    try {
        const { expirationTime } = req.body; // in minutes

        if (!expirationTime || expirationTime <= 0) {
            return res.status(400).json({
                success: false,
                message: "Valid expirationTime is required."
            });
        }

        await QrSession.updateMany(
            {},
            {
                $set: {
                    isActive: false
                }
            }
        );

        const token = crypto.randomUUID();

        const expiresAt = new Date(Date.now() + expirationTime * 60 * 1000);

        const qrSession = await QrSession.create({
            token,
            isActive: true,
            expiresAt
        });

        const qrFolder = path.join(process.cwd(), "public", "qr");
        if (!fs.existsSync(qrFolder)) {
            fs.mkdirSync(qrFolder, { recursive: true });
        }

        const fileName = `${qrSession._id}.png`;
        const filePath = path.join(qrFolder, fileName);

        await QRCode.toFile(filePath, token);

        const qrUrl = `${req.protocol}://${req.get("host")}/qr/${fileName}`;

        return res.status(201).json({
            success: true,
            qrSessionId: qrSession._id,
            token,
            qrUrl,
            expiresAt
        });

    } catch (err) {
        console.error(err);

        return res.status(500).json({
            success: false,
            message: err.message
        });
    }
});


app.post("/mark", async (req, res) => {
    try {
        const { studentId, token } = req.body;

        if (!studentId || !token) {
            return res.status(400).json({
                success: false,
                message: "studentId and token are required."
            });
        }

        const qrSession = await QrSession.findOne({
            token,
            isActive: true
        });

        if (!qrSession) {
            return res.status(404).json({
                success: false,
                message: "Invalid or inactive QR."
            });
        }

        if (qrSession.expiresAt < new Date()) {
            return res.status(400).json({
                success: false,
                message: "QR Code has expired."
            });
        }

        const student = await Student.findById(studentId);

        if (!student) {
            return res.status(404).json({
                success: false,
                message: "Student not found."
            });
        }

        const alreadyMarked = await Attendance.findOne({
            student: studentId,
            qrSession: qrSession._id
        });

        if (alreadyMarked) {
            return res.status(409).json({
                success: false,
                message: "Attendance already marked."
            });
        }

        // Mark attendance
        const attendance = await Attendance.create({
            student: studentId,
            qrSession: qrSession._id
        });

        return res.status(201).json({
            success: true,
            message: "Attendance marked successfully.",
            attendance
        });

    } catch (err) {
        console.error(err);

        return res.status(500).json({
            success: false,
            message: err.message
        });
    }
});

app.listen(4000, () => {
    connectDB();
    console.log("server is running on port 4000")
})