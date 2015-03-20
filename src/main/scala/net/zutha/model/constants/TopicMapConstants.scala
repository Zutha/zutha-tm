package net.zutha.model.constants

object TopicMapConstants {

  //Prefixes
  val TMCL_PREFIX = "http://psi.topicmaps.org/tmcl/"
  val TMDM_PREFIX = "http://psi.topicmaps.org/iso13250/model/"
  val XSD_PREFIX = "http://www.w3.org/2001/XMLSchema#"


  //Subject Indicators
  val TOPIC_TYPE_SI = TMCL_PREFIX + "topic-type"
  val OCCURRENCE_TYPE_SI = TMCL_PREFIX + "occurrence-type"
  val ASSOCIATION_TYPE_SI = TMCL_PREFIX + "association-type"

  //datatypes
  val INTEGER_SI = XSD_PREFIX + "integer"
}
