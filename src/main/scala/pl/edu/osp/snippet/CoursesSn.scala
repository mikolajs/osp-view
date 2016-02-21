package pl.edu.osp.snippet

import pl.edu.osp.model._
import net.liftweb.util.Helpers._


class CoursesSn {

  def showAllowedCourses() = {
    ".courseItem" #> getCourses().map(course => {
      "h2" #> <h2>
        {course.title}
      </h2> &
        "h3" #> <h3>{course.subjectName}</h3> &
        ".courseInfo *" #> course.descript &
        ".courseLink [href]" #> ("/course/" + course._id.toString)
    })
  }
  private def getCourses() = {
    val pub = PublicCourse.findAll.map(p => p.course)
    val all = Course.findAll
    all.filter(c =>  pub.exists(p => p == c._id))

  }
}