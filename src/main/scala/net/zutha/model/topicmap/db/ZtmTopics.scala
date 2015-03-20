package net.zutha.model.topicmap.db

import org.tmapi.core.Topic
import net.zutha.model.constants.ApplicationConstants._

trait ZtmTopics {
  protected def getOrCreateSchemaTopic(si: String): Topic

  lazy val REIFIED_ZDM_ASSOCIATION = getOrCreateSchemaTopic(REIFIED_ZDM_ASSOCIATION_SI)
  lazy val ANONYMOUS_TOPIC = getOrCreateSchemaTopic(ANONYMOUS_TOPIC_SI)
  lazy val ANONYMOUS_TOPIC_LINK = getOrCreateSchemaTopic(ANONYMOUS_TOPIC_LINK_SI)
  lazy val ZID_TICKER = getOrCreateSchemaTopic(ZID_TICKER_SI)
  lazy val NEXT_ZID = getOrCreateSchemaTopic(NEXT_ZID_SI)
}
