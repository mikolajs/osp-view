/*
 * Copyright (C) 2012   Mikołaj Sochacki mikolajsochacki AT gmail.com
 *   This file is part of VRegister (Virtual Register)
*    Apache License Version 2.0, January 2004  http://www.apache.org/licenses/
 */

package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import _root_.net.liftweb.mapper.{DB, By, ConnectionManager, ConnectionIdentifier, Schemifier, DefaultConnectionIdentifier}
import java.sql.{Connection, DriverManager}
import _root_.pl.edu.osp.model._
import _root_.pl.edu.osp.api._
import _root_.pl.edu.osp.lib.MailConfig
import _root_.net.liftweb.mongodb._
import pl.edu.osp.lib.{ConfigLoader => CL}

object DBVendor extends ConnectionManager {
  def newConnection(name: ConnectionIdentifier): Box[Connection] = {
    try {
      //Class.forName(classOf[org.postgresql.Driver].getName)
      Class.forName("org.postgresql.Driver")
      val dm = DriverManager.getConnection("jdbc:postgresql:osp", CL.sqlDB, CL.sqlPassw)
      Full(dm)
    } catch {
      case e: Exception => e.printStackTrace; Empty
    }
  }

  def releaseConnection(conn: Connection) {
    conn.close
  }
}

class Boot {
  def boot {
    CL.init

    DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)

    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(MongoHost("127.0.0.1", 27017), CL.mongoDB))

    // where to search snippet
    LiftRules.addToPackages("pl.edu.osp")
   
    Schemifier.schemify(true, Schemifier.infoF _, User, ClassModel)

    LiftRules.statelessDispatch.append({
      case Req("img" :: id :: Nil, _, GetRequest) => () => ImageLoader.image(id)
      case Req("file" :: id :: Nil, _, GetRequest) => () => FileLoader.file(id)
    })




    if (DB.runQuery("select * from users where lastname = 'Administrator'")._2.isEmpty) {
      val u = User.create
      u.lastName("Administrator").role("a").password("123qwerty").email("mail@mail.org").validated(true).save
    }


    val isAdmin = If(() => User.loggedIn_? && (User.currentUser.openOrThrowException("Not logged").role.get == "a"),
      () => RedirectResponse("/user_mgt/login?r=" + S.uri))

    // Build SiteMap::
    def sitemap() = SiteMap(
      List(
        Menu("Strona główna") / "index"  >> LocGroup("public"),
        Menu("Kurs") / "course" / ** >> LocGroup("extra"),
        Menu("Admin") / "admin" >> LocGroup("admin") >> isAdmin,
        Menu("Img") / "imgstorage" >> LocGroup("extra") >> isAdmin,
        Menu("Static") / "static" / **) :::
        // Menu entries for the User management stuff
        User.sitemap: _*)

    LiftRules.setSiteMapFunc(sitemap)

    LiftRules.statelessRewrite.prepend(NamedPF("ClassRewrite") {
      case RewriteRequest(
      ParsePath("course" :: id :: Nil, _, _, _), _, _) =>
        RewriteResponse(
          "course" :: Nil, Map("id" -> id))
    })

    //DataTable.init

    LiftRules.early.append(makeUtf8)

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    LiftRules.passNotFoundToChain = true
    LiftRules.maxMimeSize = 4 * 1024 * 1024
    LiftRules.maxMimeFileSize = 4 * 1024 * 1024

    {
      new MailConfig().autoConfigure()
    }

    LiftRules.liftRequest.append {
      case Req("extra" :: _, _, _) => false
    }



    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }
}
