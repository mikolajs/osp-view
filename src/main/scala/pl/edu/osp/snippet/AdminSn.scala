package pl.edu.osp.snippet
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.pl.edu.osp.model._
import net.liftweb.http.{S, SHtml}
import _root_.net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.util.Helpers._
import _root_.net.liftweb.json.JsonDSL._
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import org.bson.types.ObjectId

class AdminSn {
  val admin_? = User.currentUser match {
    case Full(u) => if(u.role.get == "a") true else false
    case Empty => false
  }
  println("================ User in admin path admin?  " + admin_?.toString)


  def show() = {
    val id = S.param("id").openOr("0")
    if(id != "0") {
      PublicCourse.find(id) match {
        case Some(c) => c.delete
        case None => {
          val c = PublicCourse.create
          c.course = new ObjectId(id)
          c.save
        }
      }
    }
    "tr" #> getCourses().map(c => {
     <tr> <td>{c._1.title}</td> <td>{c._1.descript}</td><td>
       {if(c._2) <a href={"/admin?id="+c._1._id} class="btn btn-success">Wyłącz</a>
        else
       <a href={"/admin?id="+c._1._id} class="btn btn-danger">Włącz</a>}
     </td></tr>
    })
  }

  private def getCourses() = {
    val pub = PublicCourse.findAll.map(p => p.course)
    val all = Course.findAll
    all.map(c => (c,  pub.exists(p => p == c._id)))

  }

}