package pl.edu.osp.model

import _root_.net.liftweb.mongodb._
import org.bson.types.ObjectId


object PublicCourse extends MongoDocumentMeta[PublicCourse] {
  override def collectionName = "publiccourses"

  override def formats = super.formats + new ObjectIdSerializer + new DateSerializer

  def create = new PublicCourse(ObjectId.get, ObjectId.get)
}

case class PublicCourse(var _id: ObjectId, var course: ObjectId) extends MongoDocument[PublicCourse] {
  def meta = PublicCourse
}

