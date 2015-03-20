package net.zutha.model.topicmap.constructs

import net.zutha.model.constructs.ZRole
import org.tmapi.core.Topic
import net.zutha.util.Cache._

object TMRole{
  val getItem = makeCache[Topic,String,TMRole](_.getId, topic => new TMRole(topic))
  def apply(topic: Topic):TMRole = getItem(topic)
}
class TMRole protected (topic: Topic) extends TMItem(topic) with ZRole{
  
}
