package pl.brosbit.snippet
import _root_.net.liftweb._
import net.liftweb.common._
import net.liftweb.util._
import pl.brosbit.model._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._


//only for resources
class CoursesSn {


  ////dodać wybór tylko  upublicznionych kursów;
  def showAllowedCourses() = {
    //poprawić na wyszukiwanie dla konkretnego ucznia
    ".courseItem" #> Course.findAll.map(course => {
      "h2" #> <h2>
        {course.title}
      </h2> &
        "h3" #> <h3>
          {course.subjectName}<span class="text-muted classInfo">, Klasy:
            {course.classInfo}
          </span>
        </h3> &
        ".courseInfo *" #> course.descript &
        ".courseLink [href]" #> ("/view/course/" + course._id.toString)
    })
  }
}