
package pl.edu.osp.model

import _root_.net.liftweb.mongodb._
import org.bson.types.ObjectId

object Slide extends MongoDocumentMeta[Slide] {
  override def collectionName = "slides"

  override def formats = super.formats + new ObjectIdSerializer + new DateSerializer

  def create = new Slide(ObjectId.get, 0L, "", "", "", new ObjectId("000000000000000000000000"))
}

case class Slide(var _id: ObjectId, var authorId: Long, var department: String,
                 var title: String, var descript: String, var slides: ObjectId)
  extends MongoDocument[Slide] {
  def meta = Slide
}
