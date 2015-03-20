package net.zutha.model.constants

object ApplicationConstants {
  val TM_DATA_PATH = "src/main/resources/tm_data/"
  val JSON_DATA_PATH = "src/main/resources/json_data/"

  val HTML_EXT = "html"
  val ZID_NAME_SEP = "::"

  val DEFAULT_ITEM_VIEW = "details"
  val DEFAULT_ASSOC_VIEW = "assoc-table"
  val DEFAULT_ROLE_VIEW = "role-player-list"

  val THIS_HOST_ID                = "0"
  val THIS_HOST_URI               = "http://" + THIS_HOST_ID + ".zutha.net/"

  //prefixes
  val ZTM_PREFIX = "http://psi.zutha.net/topicmap/"
  val THIS_HOST_PSI_PREFIX        = THIS_HOST_URI + "psi/"

  //anonymous constructs
  lazy val REIFIED_ZDM_ASSOCIATION_SI = ZTM_PREFIX + "reified-zdm-association"
  lazy val ANONYMOUS_TOPIC_SI = ZTM_PREFIX + "anonymous-topic"
  lazy val ANONYMOUS_TOPIC_LINK_SI = ZTM_PREFIX + "anonymous-topic-link"

  val ZID_TICKER_SI               = THIS_HOST_PSI_PREFIX + "zid-ticker"
  val NEXT_ZID_SI  = THIS_HOST_PSI_PREFIX + "next-zid"
}
