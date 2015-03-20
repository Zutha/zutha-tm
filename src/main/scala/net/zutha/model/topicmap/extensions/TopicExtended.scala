package net.zutha.model.topicmap.extensions


import net.zutha.util.Cache._
import org.tmapi.core.{Occurrence, Topic}
import net.zutha.model.constants.TopicMapConstants
import net.zutha.model.db.DB._
import net.zutha.model.topicmap.db.{TopicMapDB}

object TopicExtended{
  val get = makeCache[Topic,String,TopicExtended](_.getId, topic => new TopicExtended(topic))
  def apply(topic: Topic):TopicExtended = get(topic)
}
class TopicExtended(topic: Topic) {
  val tm = tdb.tm
  lazy val tdb = db.asInstanceOf[TopicMapDB]

  def createIntOccurrence(occType: Topic, value: String): Occurrence = {
    val integerType = tm.createLocator(TopicMapConstants.INTEGER_SI)
    topic.createOccurrence(occType, value, integerType)
  }

  def isAnonymous: Boolean = tdb.topicIsAnonymous(topic)

}
