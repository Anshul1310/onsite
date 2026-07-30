const mongoose = require("mongoose");
const Student = require("./Student.js");

const students = [
    { rollNo: "112125005", name: "Anshul Negi", email: "anshul@test.com", department: "MME", year: 4 }
];

async function seed() {
    await mongoose.connect("mongodb+srv://anshul:anshul@delta.ceopbox.mongodb.net/");
    await Student.deleteMany({});
    const created = await Student.insertMany(students);
    created.forEach(s => console.log(`${s.name} -> ${s._id}`));
    process.exit(0);
}

seed();
