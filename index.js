const express = require("express");
const mongoose = require("mongoose");
const fs = require("fs");
const crypto = require("crypto");
const QRCode = require("qrcode");
const path = require("path");
const jwt = require("jsonwebtoken");
const axios = require("axios");

const app = express();
const JWT_SECRET = "supersecretkey";

app.use(express.static(path.join(process.cwd(), "public")));
app.use(express.json());

const studentSchema = new mongoose.Schema(
    {
        rollNo: { type: String, required: true, unique: true },
        name: { type: String, required: true },
        email: { type: String, required: true, unique: true },
        department: String,
        year: Number
    },
    { timestamps: true }
);

const qrSessionSchema = new mongoose.Schema(
    {
        token: { type: String, required: true, unique: true },
        isActive: { type: Boolean, default: true },
        expiresAt: { type: Date, required: true }
    },
    { timestamps: true }
);

const attendanceSchema = new mongoose.Schema(
    {
        student: { type: mongoose.Schema.Types.ObjectId, ref: "Student", required: true },
        qrSession: { type: mongoose.Schema.Types.ObjectId, ref: "QrSession", required: true }
    },
    { timestamps: true }
);

const Student = mongoose.models.Student || mongoose.model("Student", studentSchema);
const QrSession = mongoose.models.QrSession || mongoose.model("QrSession", qrSessionSchema);
const Attendance = mongoose.models.Attendance || mongoose.model("Attendance", attendanceSchema);

const connectDB = async () => {
    try {
        await mongoose.connect("mongodb+srv://anshul:anshul@delta.ceopbox.mongodb.net/");
        console.log("MongoDB Connected");
    } catch (err) {
        console.error(err.message);
        process.exit(1);
    }
};

const authMiddleware = (req, res, next) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({ success: false, message: "No token provided" });
    }
    const token = authHeader.split(" ")[1];
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.studentId = decoded.studentId;
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(401).json({ success: false, message: "Invalid or expired token" });
    }
};

app.get("/health", (req, res) => {
    res.send("Hello World!");
});

app.post("/dauth", async (req, res) => {
    try {
        const { code, rollNo, name, email, department, year } = req.body;
        let student;
        if (code) {
            try {
                const tokenRes = await axios.post("https://auth.delta.nitt.edu/api/oauth/token", {
                    client_id: process.env.DAUTH_CLIENT_ID || "client_id",
                    client_secret: process.env.DAUTH_CLIENT_SECRET || "client_secret",
                    grant_type: "authorization_code",
                    code: code,
                    redirect_uri: process.env.DAUTH_REDIRECT_URI || "http://localhost:4000/callback"
                });
                const userRes = await axios.post("https://auth.delta.nitt.edu/api/resources/user", {}, {
                    headers: { Authorization: `Bearer ${tokenRes.data.access_token}` }
                });
                const userData = userRes.data;
                student = await Student.findOne({ email: userData.email });
                if (!student) {
                    student = await Student.create({
                        rollNo: userData.email ? userData.email.split("@")[0] : String(userData.id),
                        name: userData.name || "Student",
                        email: userData.email,
                        department: userData.department || "CSE",
                        year: userData.year || 1
                    });
                }
            } catch (dauthErr) {
                if (rollNo) {
                    student = await Student.findOne({ rollNo });
                    if (!student) {
                        student = await Student.create({ rollNo, name: name || rollNo, email: email || `${rollNo}@nitt.edu`, department: department || "N/A", year: year || 1 });
                    }
                } else {
                    return res.status(400).json({ success: false, message: dauthErr.message });
                }
            }
        } else if (rollNo) {
            student = await Student.findOne({ rollNo });
            if (!student) {
                student = await Student.create({ rollNo, name: name || rollNo, email: email || `${rollNo}@nitt.edu`, department: department || "CSE", year: year || 1 });
            }
        } else {
            return res.status(400).json({ success: false, message: "Code or Roll Number required" });
        }
        const jwtToken = jwt.sign({ studentId: student._id, rollNo: student.rollNo }, JWT_SECRET, { expiresIn: "7d" });
        return res.json({ success: true, token: jwtToken, student });
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message });
    }
});

app.get("/attendance/count/:rollNo", async (req, res) => {
    try {
        const { rollNo } = req.params;
        const student = await Student.findOne({ rollNo });
        if (!student) {
            return res.status(404).json({ success: false, message: "Student not found." });
        }
        const count = await Attendance.countDocuments({ student: student._id });
        res.json({ success: true, student, count });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
});

app.get("/attendance/:date", async (req, res) => {
    try {
        const { date } = req.params;
        const start = new Date(date);
        const end = new Date(date);
        end.setDate(end.getDate() + 1);

        const attendance = await Attendance.find({
            createdAt: {
                $gte: start,
                $lt: end
            }
        })
            .populate("student")
            .populate("qrSession");

        res.json({ success: true, attendance });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
});

app.post("/generate", async (req, res) => {
    try {
        const { expirationTime } = req.body;
        if (!expirationTime || expirationTime <= 0) {
            return res.status(400).json({ success: false, message: "Valid expirationTime is required." });
        }
        await QrSession.updateMany({}, { $set: { isActive: false } });
        const token = crypto.randomUUID();
        const expiresAt = new Date(Date.now() + expirationTime * 60 * 1000);
        const qrSession = await QrSession.create({ token, isActive: true, expiresAt });
        const qrFolder = path.join(process.cwd(), "public", "qr");
        if (!fs.existsSync(qrFolder)) {
            fs.mkdirSync(qrFolder, { recursive: true });
        }
        const fileName = `${qrSession._id}.png`;
        const filePath = path.join(qrFolder, fileName);
        await QRCode.toFile(filePath, token);
        const qrUrl = `${req.protocol}://${req.get("host")}/qr/${fileName}`;
        return res.status(201).json({ success: true, qrSessionId: qrSession._id, token, qrUrl, expiresAt });
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message });
    }
});

app.post("/mark", authMiddleware, async (req, res) => {
    try {
        const { token } = req.body;
        const studentId = req.studentId;
        if (!token) {
            return res.status(400).json({ success: false, message: "Token is required." });
        }
        const qrSession = await QrSession.findOne({ token, isActive: true });
        if (!qrSession) {
            return res.status(404).json({ success: false, message: "Invalid or inactive QR." });
        }
        if (qrSession.expiresAt < new Date()) {
            return res.status(400).json({ success: false, message: "QR Code has expired." });
        }
        const student = await Student.findById(studentId);
        if (!student) {
            return res.status(404).json({ success: false, message: "Student not found." });
        }
        const alreadyMarked = await Attendance.findOne({ student: studentId, qrSession: qrSession._id });
        if (alreadyMarked) {
            return res.status(409).json({ success: false, message: "Attendance already marked." });
        }
        const attendance = await Attendance.create({ student: studentId, qrSession: qrSession._id });
        return res.status(201).json({ success: true, message: "Attendance marked successfully.", attendance });
    } catch (err) {
        return res.status(500).json({ success: false, message: err.message });
    }
});

app.listen(4000, () => {
    connectDB();
    console.log("server is running on port 4000");
});