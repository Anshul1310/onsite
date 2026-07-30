const mongoose = require("mongoose")

const qrSessionSchema = new mongoose.Schema(
    {
        token: {
            type: String,
            required: true,
            unique: true,
        },
        isActive: {
            type: Boolean,
            default: true,
        },
        expiresAt: {
            type: Date,
            required: true,
        }
    },
    { timestamps: true }
);

module.exports = mongoose.model("QrSession", qrSessionSchema);