const express = require("express")
const mongoose = require("mongoose")
const app = express()

const connectDB = async () => {
    try {
        await mongoose.connect("mongodb+srv://anshul:anshul@delta.ceopbox.mongodb.net/");
        console.log("MongoDB Connected");
    } catch (err) {
        console.error(err.message);
        process.exit(1);
    }
};

app.listen(4000, () => {
    connectDB();
    console.log("server is running on port 4000")
})

app.get("/", (req, res) => {
    res.send("Hello World!")
})
app.use(express.json())

app.post("/generate", (req, res) => {
    const { n } = req.body

})
