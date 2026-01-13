// DataConverter logic
const DataConverter = {
    months: {
        "Jan": 1, "Feb": 2, "Mar": 3, "Apr": 4, "May": 5, "Jun": 6,
        "Jul": 7, "Aug": 8, "Sep": 9, "Oct": 10, "Nov": 11, "Dec": 12
    },

    convertWeekday: (inputTime) => {
        const map = {
            "M": "MON", "T": "TUE", "W": "WED", "R": "THU", "F": "FRI", "S": "SAT", "U": "SUN"
        };
        return map[inputTime] || "SUN";
    },

    convertLocation: (inputLocation) => {
        const parts = inputLocation.split(" ");
        let result = "Unknown location";
        const building = parts[0].toUpperCase();

        if (building === "YEUNG") result = "AC1";
        else if (building === "BANK") result = "BOC";
        else if (building === "LI") result = "AC2";
        else if (building === "R") result = "CMC";

        if (parts.length > 1) {
            result += "-" + parts[parts.length - 1];
        }
        return result;
    },

    convertDate: (inputDate) => {
        // "Mar 2, 2026" -> "2026/3/2"
        try {
            const parts = inputDate.replace(',', '').split(' ');
            const month = DataConverter.months[parts[0]];
            const day = parts[1];
            const year = parts[2];
            return `${year}/${month}/${day}`;
        } catch (e) {
            console.error("Error converting date:", inputDate, e);
            return inputDate; // Fallback
        }
    },

    convertTime: (inputTime) => {
        // "10:00 PM" -> "22:00"
        try {
            const match = inputTime.match(/(\d+):(\d+)\s?(AM|PM)/i);
            if (!match) return inputTime;

            let hours = parseInt(match[1]);
            const minutes = match[2];
            const ampm = match[3].toUpperCase();

            if (ampm === "PM" && hours < 12) hours += 12;
            if (ampm === "AM" && hours === 12) hours = 0;

            return `${hours.toString().padStart(2, '0')}:${minutes}`;
        } catch (e) {
            console.error("Error converting time:", inputTime, e);
            return inputTime;
        }
    },

    formatToUtc: (dateStr, timeStr) => {
        // Input: "2026/3/2", "14:00"
        // Java code uses system default timezone. We will assume the user's browser TZ 
        // or we could force HK time if this is strictly for CityU (HK).
        // Let's use the browser's current timezone to match "systemDefault()" behavior of the Java app 
        // running on the user's machine.

        const [year, month, day] = dateStr.split('/').map(Number);
        const [hour, minute] = timeStr.split(':').map(Number);

        const date = new Date(year, month - 1, day, hour, minute, 0);

        // Format to YYYYMMDDTHHMMSSZ
        const pad = (n) => n.toString().padStart(2, '0');

        const uYear = date.getUTCFullYear();
        const uMonth = pad(date.getUTCMonth() + 1);
        const uDay = pad(date.getUTCDate());
        const uHour = pad(date.getUTCHours());
        const uMinute = pad(date.getUTCMinutes());
        const uSecond = pad(date.getUTCSeconds());

        return `${uYear}${uMonth}${uDay}T${uHour}${uMinute}${uSecond}Z`;
    },

    nearby: (startDayStr, repeatDay) => {
        // startDayStr: "2026/3/2"
        // repeatDay: "MON", "TUE", etc.
        const [year, month, day] = startDayStr.split('/').map(Number);
        const date = new Date(year, month - 1, day); // Local time

        const targetDayMap = { "SUN": 0, "MON": 1, "TUE": 2, "WED": 3, "THU": 4, "FRI": 5, "SAT": 6 };
        const targetDay = targetDayMap[repeatDay];

        const currentDay = date.getDay();
        let daysToAdd = targetDay - currentDay;
        if (daysToAdd < 0) daysToAdd += 7;

        date.setDate(date.getDate() + daysToAdd);

        return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`;
    }
};

// DataReader Logic
class Timeslot {
    constructor(crn, type, startTime, endTime, startDate, endDate, weekday, location) {
        this.crn = crn;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weekday = weekday;
        this.location = location;
    }
}

class Course {
    constructor(id) {
        this.id = id;
        this.timeslots = [];
    }

    addTimeslot(t) {
        this.timeslots.push(t);
    }
}

const parseInput = (text) => {
    const lines = text.split('\n');
    const courses = [];

    let startIndex = 0;

    // Skip to "Total Credit Hours"? Or just process everything.
    // The Java code skips until "Total Credit Hours" but also seems to rely on "Return to Previous" to stop.
    // Let's implement a robust parser that looks for patterns.

    // Find where the meaningful data starts if possible, but the Java loop `while(true)`
    // reads lines until it hits `Total Credit Hours`.
    for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes("Total Credit Hours")) {
            startIndex = i + 1;
            break;
        }
    }

    let currentCourse = null;
    let currentCrn = "";

    // searchDetail logic adaptation
    // The java code is a bit unstructured in iteration.
    // It calls `searchDetail` which consumes more lines from the Scanner.

    // We can iterate line by line and maintain state.

    for (let i = startIndex; i < lines.length; i++) {
        const line = lines[i].trim();
        if (line.includes("Return to Previous")) break;
        if (!line) continue;

        // Heuristic to detect a course header: "12345 - CS1001 - Intro to CS"
        // The Java code splits by '-'.
        // line 39: String[] courseArr = course.split("-");
        // if length >= 3, it's likely a course header.
        if (line.includes("-") && line.split("-").length >= 3 && !line.includes("Class") && !line.includes("CRN") && !line.includes("Total Credit Hours")) {
            const parts = line.split("-");
            if (parts[1]) {
                const courseId = parts[1].trim().replace(/\s/g, "");

                // Check if course exists
                currentCourse = courses.find(c => c.id === courseId);
                if (!currentCourse) {
                    currentCourse = new Course(courseId);
                    courses.push(currentCourse);
                }
                // Store the type/name if needed? Java passes `courseArr[2]` as generic type or name to Timeslot?
                // Wait, Java: `new Timeslot(crn, courseArr[2], ...)`
                // So `courseArr[2]` is the Course Name / Type part.
                currentCourse.tempName = parts[2].trim();
            }
        }

        if (line.includes("CRN")) {
            // CRN: 12345
            const parts = line.split(":");
            if (parts[1]) currentCrn = parts[1].trim();
        }

        if (line.includes("Class")) {
            // Format: Type \t Time - Time \t Day \t Location \t Date - Date \t ...
            // Java: String temp[] = line.split("\t");
            const temp = line.split("\t");

            if (temp.length >= 5) {
                // Time
                const timeParts = temp[1].split(" - ");
                const startTime = DataConverter.convertTime(timeParts[0]);
                const endTime = DataConverter.convertTime(timeParts[1]);

                // Day
                const weekday = DataConverter.convertWeekday(temp[2]);

                // Location
                const location = DataConverter.convertLocation(temp[3]);

                // Date
                const dateParts = temp[4].split(" - ");
                const startDate = DataConverter.convertDate(dateParts[0]);
                const endDate = DataConverter.convertDate(dateParts[1]);

                // Type is usually temp[0] ("Class" or "Lecture" etc?) 
                // Wait, Java code says: `if (line.contains("Class"))`
                // But `temp[0]` would be the first column. In AIMS it's usually "Class" or "Y".
                // Java code passes `courseArr[2]` to Timeslot constructor as `type`.
                // Ah, line 68: `new Timeslot(crn, courseArr[2], ...)`

                if (currentCourse) {
                    const t = new Timeslot(
                        currentCrn,
                        currentCourse.tempName || "",
                        startTime,
                        endTime,
                        startDate,
                        endDate,
                        weekday,
                        location
                    );
                    currentCourse.addTimeslot(t);
                }
            }
        }
    }

    return courses;
};

// ICS Generation Logic
const generateICS = (courses) => {
    let builder = "";
    builder += "BEGIN:VCALENDAR\r\n";
    builder += "VERSION:2.0\r\n";
    builder += "PRODID:-//CityU Exporter//Web//EN\r\n";
    builder += "METHOD:PUBLISH\r\n";
    builder += "X-WR-CALNAME:CityU Timetable\r\n";

    const byDayMap = { "MON": "MO", "TUE": "TU", "WED": "WE", "THU": "TH", "FRI": "FR", "SAT": "SA", "SUN": "SU" };

    courses.forEach(c => {
        c.timeslots.forEach(t => {
            const title = `${t.location}(${c.id}${t.type ? t.type.charAt(0) : ''})`; // Adjusted similar to Java

            // Calculate start date (nearby)
            const realStartDate = DataConverter.nearby(t.startDate, t.weekday);
            const realEndDate = DataConverter.nearby(t.endDate, t.weekday); // Not sure if this is used for event end or repeats

            // Recurrence Rule
            // UNTIL needs to be the end date + end time of the LAST occurrence.
            const untilStr = DataConverter.formatToUtc(t.endDate, t.endTime);
            const rrule = `FREQ=WEEKLY;BYDAY=${byDayMap[t.weekday]};UNTIL=${untilStr}`;

            // Event Start/End
            const startStr = DataConverter.formatToUtc(realStartDate, t.startTime);
            const endStr = DataConverter.formatToUtc(realStartDate, t.endTime);

            builder += "BEGIN:VEVENT\r\n";
            builder += `UID:${crypto.randomUUID()}\r\n`;
            builder += `DTSTAMP:${new Date().toISOString().replace(/[-:]/g, '').split('.')[0]}Z\r\n`;
            builder += `DTSTART:${startStr}\r\n`;
            builder += `DTEND:${endStr}\r\n`;
            builder += `RRULE:${rrule}\r\n`;
            builder += `SUMMARY:${title}\r\n`;
            builder += "END:VEVENT\r\n";
        });
    });

    builder += "END:VCALENDAR\r\n";
    return builder;
};

// UI Handling
document.addEventListener('DOMContentLoaded', () => {
    const convertBtn = document.getElementById('convertBtn');
    const inputArea = document.getElementById('inputData');
    const fileInput = document.getElementById('fileInput');
    const filenameInput = document.getElementById('filenameInput');

    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                inputArea.value = e.target.result;
            };
            reader.readAsText(file);
        }
    });

    convertBtn.addEventListener('click', () => {
        const text = inputArea.value;
        if (!text.trim()) {
            alert("Please paste data or upload a file first.");
            return;
        }

        try {
            const courses = parseInput(text);
            console.log("Parsed Courses:", courses);
            if (courses.length === 0) {
                alert("No courses found. Please check the input format.");
                return;
            }

            const icsContent = generateICS(courses);

            let filename = filenameInput.value.trim();
            if (!filename) {
                filename = "timetable";
            }
            if (!filename.endsWith(".ics")) {
                filename += ".ics";
            }

            downloadFile(filename, icsContent);
        } catch (e) {
            console.error(e);
            alert("An error occurred during conversion. See console for details.");
        }
    });
});

const downloadFile = (filename, content) => {
    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/calendar;charset=utf-8,' + encodeURIComponent(content));
    element.setAttribute('download', filename);

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
};
