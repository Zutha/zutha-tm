package net.zutha.model.topicmap.constructs

import org.tmapi.core.Topic
import net.zutha.util.Cache._
import net.zutha.model.db.DB._
import net.zutha.model.constructs.{ZItemType}

object TMItemType{
  val getItem = makeCache[Topic,String,TMItemType](_.getId, topic => new TMItemType(topic))
  def apply(topic: Topic):TMItemType = getItem(topic)
}
class TMItemType protected (topic: Topic) extends TMType(topic) with ZItemType{
  lazy val getAllSuperItemTypes: Set[ZItemType] = ancestors.filter(_.isItemType).map{_.toItemType}

  def compatibleTraits = {
    getAllSuperItemTypes.flatMap{it =>
      db.traverseAssociation(it,db.ITEM_TYPE.toRole,db.ITEM_TYPE_TRAIT_DECLARATION,db.TRAIT.toRole).map(_.toTrait)
    }
  }
}
