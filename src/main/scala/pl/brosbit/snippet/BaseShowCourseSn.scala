package pl.brosbit.snippet

import scala.xml.{Text,  Unparsed}
import _root_.net.liftweb._
import http.S
import common._
import util._
import pl.brosbit.model._
import json.JsonDSL._
import Helpers._

class BaseShowCourseSn  {

  val basePath = "/view/course/"

  val user = User.currentUser match {
    case Full(user) => user
    case _ => S.redirectTo("/login")
  }

  val courseId = S.param("id").openOr("0")
  var lessonId = S.param("l").openOr("0")
  val course = Course.find(courseId) match {
    case Some(course) => course
    case _ => Course.create
  }
  val lessons = LessonCourse.findAll(("courseId" -> course._id.toString), ("nr" -> 1));
  var currentLesson = lessons.find(les => les._id.toString == lessonId) match {
    case Some(foundLesson) => foundLesson
    case _ => if (!lessons.isEmpty) lessons.head else LessonCourse.create
  }

  protected def findChapterName(id: Int) = if (course.chapters.contains(id)) course.chapters(id) else "Brak nawy!"

  protected def showAsDocument(lesson: LessonCourse, admin: Boolean) = {
    val infoError = "section" #> <section>
      <h1>Błąd - nie znaleziono materiału</h1>
    </section>

    val (headWords, restOf) = lesson.contents.partition(x => x.what == "w")
    val (quests, restOf2) = restOf.partition(x => x.what == "q")
    val (notes, restOf3) = restOf2.partition(x => x.what == "n")
    val (docum, videos) = restOf3.partition(x => x.what == "d")

    val listHWid = headWords.map(hw => hw.id.drop(1))
    val listQid = quests.map(q => q.id.drop(1))
    val listVideos = videos.map(v => v.id.drop(1))
    val listDocs = docum.map(d => d.id.drop(1))

    val vs = Video.findAll(("_id" -> ("$in" -> listVideos)))
    val hws = HeadWord.findAll(("_id" -> ("$in" -> listHWid)))
    val qts = QuizQuestion.findAll(("_id" -> ("$in" -> listQid)))
    val docs = Document.findAll(("_id" -> ("$in" -> listDocs)))

    val content = lesson.contents.map(item => item.what match {
      case "w" => {
        val headW = hws.find(i => i._id.toString == item.id.drop(1)).
          getOrElse(HeadWord.create)
        "<section class=\"headword\">" + headW.content + "</section>"
      }
      case "q" => {
        createQuest(qts.find(q => q._id.toString == item.id.drop(1)).
          getOrElse(QuizQuestion.create))
      }
      case "v" => {
        vs.find(v => v._id.toString() == item.id.drop(1)) match {
          case Some(video) => {
            val vidId = "vid" + video._id.toString()
            <section class="video">
              <h3>
                {video.title}
              </h3>
              <p>
                <small>
                  {video.descript}
                </small>
              </p>{if (video.onServer) <div id={vidId}>Ładowanie filmu</div> ++
              <script type="text/javascript">
                {Unparsed( """var _0x9b95="\x68\x74\x74\x70\x3A\x2F\x2F\x76\x69\x64\x65\x6F\x2E\x65\x70\x6F\x64\x72\x65\x63\x7A\x6E\x69\x6B\x2E\x65\x64\x75\x2E\x70\x6C\x2F";
                      jwplayer("%s").setup({
                    		file: (_0x9b95 + "%s"),
                    		width: 853,
                    		height:  480,
                        image: "/style/images/grafika_pod_video.png"
                    	});""".format(vidId,
                  video._id.toString + "." + video.link.split('.').last))}
              </script>
            else
              <iframe width="853" height="480" src={"//www.youtube.com/embed/" + video.link} frameborder="0" allowfullscreen=" "></iframe>}
            </section>
          }
          case _ => <h4>Błąd - nie ma takiego filmu</h4>
        }
      }
      case "d" => {
        val docModel = docs.find(i => i._id.toString == item.id.drop(1)).getOrElse(Document.create)
        "<section class=\"document\"> <h1>" + docModel.title + "</h1>\n " + docModel.content + "</section>"
      }
      case "n" => {
        <section class="notice">{Unparsed(item.descript)}</section>
      }
      case _ => <h4>Błąd - nie ma takiego typu zawartości</h4>
    }).mkString("\n")

    val editLinkAdd = if (lesson.courseId == course._id)
      <a href={"/educontent/editlesson/" + lesson._id.toString} class="btn btn-info">
        <span class="glyphicon glyphicon-pencil"></span>
        Edytuj
      </a>
    else <span></span>


    val chapterInfo = Text("Rozdział: " + lesson.chapter)


    val baseCSS =
      "#run-as-slides [href]" #> ("/lesson-slides/" + lesson._id.toString()) &
        ".page-header *" #> Text("Temat: " + lesson.title) &
        ".chapterInfo *" #> chapterInfo &
        "#sections" #> Unparsed(content)

    if (admin) {
      baseCSS &
        "#editLink" #> editLinkAdd
    } else baseCSS

  }

  protected def createQuest(quest: QuizQuestion) = {

    if (quest.fake.length == 0) {
      if (quest.answers.length == 0) createPlainQuest(quest)
      else createInputQuest(quest)
    }
    else {
      if (quest.answers.length == 1) createSingleAnswerQuest(quest)
      else createMultiAnswerQuest(quest)
    }
  }

  protected def createPlainQuest(quest: QuizQuestion) = {
    mkQuestHTML('p', quest.question, "", scala.xml.NodeSeq.Empty)
  }

  protected def createSingleAnswerQuest(quest: QuizQuestion) = {
    val all = (quest.fake ++ quest.answers).sortWith(_ > _)
      .map(s => <li>
      <input type="radio" value={s} name={quest._id.toString}/> <label>
        {s}
      </label>
    </li>)
    val correctString = quest.answers.mkString(";;;")
    mkQuestHTML('s', quest.question, correctString, <ul>
      {all}
    </ul>)
  }

  protected def createInputQuest(quest: QuizQuestion) = {
    val all = <div>
      <label>Odpowiedź:</label> <input type="text" name={quest._id.toString}/>
    </div>
    val correctString = quest.answers.mkString(";;;")
    mkQuestHTML('i', quest.question, correctString, all)
  }

  protected def createMultiAnswerQuest(quest: QuizQuestion) = {
    val all = (quest.fake ++ quest.answers).sortWith(_ > _)
      .map(s => <li>
      <input type="checkbox" value={s} name={quest._id.toString}/> <label>
        {s}
      </label>
    </li>)
    val correctString = quest.answers.mkString(";;;")
    mkQuestHTML('m', quest.question, correctString, <ul>
      {all}
    </ul>)
  }

  protected def mkQuestHTML(questType: Char, question: String, correct: String, answers: scala.xml.NodeSeq) = {
    <section class="question">
      <div class="panel panel-info">
        <div class="panel-heading questionMark">
          <span class="glyphicon glyphicon-question-sign"></span>
          Zadanie </div>
        <div class="panel-body">
          <input type="hidden" class="questType" value={questType.toString}/>
          <input type="hidden" class="correct" value={correct}/>
          <div class="questionText">
            {Unparsed(question)}
          </div>{answers}{if (questType != 'p') <button onclick="showCourse.checkAnswer(this)">Sprawdź</button>
        else <span></span>}<p class="alertWell"></p>
        </div>
      </div>
    </section>
  }


  //protected def insert

  protected def createLessonList() = {
    var chapterFiltred: List[LessonCourse] = lessons

    println("[AppINFO:::::: ]" + lessons.length.toString)
    val toReturn = course.chapters.map(ch => {
      val (lessonChapter, restChap) = chapterFiltred.partition(l => l.chapter == ch)
      chapterFiltred = restChap
      <span class="list-group-item chapter">
        {ch}
      </span> ++
        lessonChapter.map(less => {
          if (less._id.toString() == currentLesson._id.toString())
            <a href={"#"} class={"list-group-item active"}>
              <span class="badge">
                {less.nr.toString}
              </span>
              <span class="lesson-title">
                {less.title}
              </span>
            </a>
          else
            <a href={basePath + course._id.toString + "?l=" + less._id.toString} class="list-group-item">
              <span class="badge">
                {less.nr.toString}
              </span> <span class="lesson-title">
              {less.title}
            </span>
            </a>
        })
    })
    val rest = if (chapterFiltred.isEmpty) Nil
    else {
      <span class="list-group-item chapter">Nieprzydzielone do działów</span> ++ chapterFiltred.map(less => {
        if (less._id.toString() == currentLesson._id.toString())
          <a href={"#"} class={"list-group-item active"}>
            <span class="badge">
              {less.nr.toString}
            </span> <span class="lesson-title">
            {less.title}
          </span>
          </a>
        else
          <a href={basePath + course._id.toString + "?l=" + less._id.toString} class="list-group-item">
            <span class="badge">
              {less.nr.toString}
            </span> <span class="lesson-title">
            {less.title}
          </span>
          </a>
      })
    }

    //println("[AppINFO:::::: ]" + toReturn.flatten.toString +  "\n" + rest.toString)
    toReturn.flatten ++ rest
  }

  protected def printInfo {
    println("idCourse = " + course._id.toString + " course title = " + course.title)
  }

}