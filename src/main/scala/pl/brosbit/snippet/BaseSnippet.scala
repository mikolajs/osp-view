package pl.brosbit.snippet
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import net.liftweb._
import pl.brosbit.model._
import net.liftweb.util.Helpers._

trait BaseSnippet {
  val user = User.currentUser.openOrThrowException("Uczeń musi być zalogowany")

}

  