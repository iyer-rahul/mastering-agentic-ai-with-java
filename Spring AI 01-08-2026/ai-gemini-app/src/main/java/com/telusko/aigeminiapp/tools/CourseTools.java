package com.telusko.aigeminiapp.tools;

import com.telusko.aigeminiapp.web.dto.Course;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CourseTools
{

    private final Map<String, Course> catalog = new ConcurrentHashMap<>();

    public CourseTools() {
        put(new Course("java-101", "Java Fundamentals", "Beginner", 40, 999, 12));
        put(new Course("spring-201", "Spring Boot in Depth", "Intermediate", 55, 1499, 5));
        put(new Course("ai-301", "Building AI Apps with Spring AI", "Advanced", 30, 1999, 3));
        put(new Course("py-101", "Python for Absolute Beginners", "Beginner", 35, 899, 20));
        put(new Course("ds-210", "Data Structures with Java", "Intermediate", 45, 1299, 0));
    }

    @Tool(description = """
            List every course in the Telusko catalog, along with its level, duration,
            price in INR and how many seats are still free.""")
    public List<Course> listCourses() {
        return List.copyOf(catalog.values());
    }

    @Tool(description = """
            Search the catalog by keyword. Matches the course title, id or level,
            for example "spring", "python", "beginner". Returns an empty list when
            nothing matches - in that case tell the user we do not teach it yet.""")
    public List<Course> findCourses(
            @ToolParam(description = "A single topic or keyword to search for, e.g. 'spring'")
            String topic) {

        String needle = topic == null ? "" : topic.toLowerCase(Locale.ROOT).trim();

        return catalog.values().stream()
                .filter(course -> course.title().toLowerCase(Locale.ROOT).contains(needle)
                        || course.id().toLowerCase(Locale.ROOT).contains(needle)
                        || course.level().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    @Tool(description = """
            Enroll one student into a course and hold a seat for them.
            Only call this once you know both the exact course id and the student's name.
            Returns a plain confirmation sentence, or an error sentence if the id is
            unknown or the batch is already full.""")
    public String enrollStudent(
            @ToolParam(description = "Exact course id from the catalog, e.g. 'spring-201'")
            String courseId,
            @ToolParam(description = "Full name of the student being enrolled")
            String studentName) {

        Course course = catalog.get(courseId == null ? "" : courseId.toLowerCase(Locale.ROOT).trim());

        if (course == null) {
            return "No course with id '" + courseId + "'. Ask the student to pick one from the catalog.";
        }
        if (course.seatsLeft() <= 0) {
            return "'" + course.title() + "' is full. Suggest the next batch or a similar course.";
        }

        Course updated = course.withSeats(course.seatsLeft() - 1);
        catalog.put(updated.id(), updated);

        return "%s is enrolled in '%s'. Amount payable: INR %d. Seats left: %d."
                .formatted(studentName, updated.title(), updated.priceInr(), updated.seatsLeft());
    }

    private void put(Course course) {
        catalog.put(course.id(), course);
    }
}

