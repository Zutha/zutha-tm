package net.zutha.model.topicmap.constructs

import org.tmapi.core.Topic
import net.zutha.util.Cache._
import net.zutha.model.constructs.{ZItemType, ZTrait}
import net.zutha.model.db.DB.db

object TMTrait{
  val getItem = makeCache[Topic,String,TMTrait](_.getId, topic => new TMTrait(topic))
  def apply(topic: Topic):TMTrait = getItem(topic)
}
class TMTrait protected (topic: Topic) extends TMType(topic) with ZTrait{
  def compatibleItemTypes: Set[ZItemType] = {
    db.traverseAssociation(this,db.TRAIT.toRole,db.ITEM_TYPE_TRAIT_DECLARATION,db.ITEM_TYPE.toRole).map(_.toItemType)
  }
}
