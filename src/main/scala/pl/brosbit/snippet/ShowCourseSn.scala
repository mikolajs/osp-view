package pl.brosbit.snippet

import java.util.{Date}
import _root_.net.liftweb._
import net.liftweb.http.{S, SHtml}
import net.liftweb.util._
import pl.brosbit.model._
import net.liftweb.util.Helpers._
import net.liftweb.mapper.By
import net.liftweb.common.Full
import net.liftweb.common.StringOrNodeSeq.nsTo

class ShowCourseSn extends BaseShowCourseSn {

//controla czy można oglądać kurs
  def show() = {

    if (course.title != "") {
      "#subjectListLinks a" #> createLessonList &
        "#courseInfo" #> <div class="alert alert-success">
          <h2>
            {course.title}
          </h2> <p>
            {course.descript}
          </p>
        </div> &
        ".content *" #> this.showAsDocument(currentLesson, false)
    } else ".main *" #> <h1>Nie ma takiego kursu lub brak lekcji</h1>
  }



}