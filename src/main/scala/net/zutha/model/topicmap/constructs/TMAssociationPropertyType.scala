package net.zutha.model.topicmap.constructs

import net.zutha.util.Cache._
import org.tmapi.core.Topic
import net.zutha.model.constructs.ZAssociationPropertyType

object TMAssociationPropertyType{
  val getItem = makeCache[Topic,String,TMAssociationPropertyType](_.getId, topic => new TMAssociationPropertyType(topic))
  def apply(topic: Topic):TMAssociationPropertyType = getItem(topic)
}
class TMAssociationPropertyType protected (topic: Topic) extends TMPropertyType(topic) with ZAssociationPropertyType{

}
