package net.zutha.model.topicmap.db

import net.zutha.lib.BaseX
import net.zutha.model.constructs.Zid
import net.zutha.model.constants.ApplicationConstants._
import org.tmapi.core.{TopicMap,Occurrence}
import net.zutha.model.topicmap.TMConversions._
import net.liftweb.common.Logger

class ZIDTicker(tm: TopicMap) extends Logger{
  val base32converter = BaseX(Zid.charset)
  val tickerProp: Occurrence = getNextZIDOccurrence


  private def increment = {
    val oldTicker = tickerProp.longValue()
    val newTicker = oldTicker + 1
    tickerProp.setValue(newTicker)
    oldTicker
  }

  def getNext: Zid = Zid(THIS_HOST_ID + base32converter.encode(increment))

  def getCurrentAsLong = tickerProp.longValue
  def getCurrent = Zid(THIS_HOST_ID + base32converter.encode(getCurrentAsLong))

  def setTickerValue(newValue: Long) {tickerProp.setValue(newValue)}

  private def getNextZIDOccurrence: Occurrence = {
    //get ZID_Ticker topic
    val tickerTopic = tm.getOrCreateTopicBySI(ZID_TICKER_SI)

    //get nextZID occurrence type
    val nextZIDOccType = tm.getOrCreateTopicBySI(NEXT_ZID_SI)

    //get nextZID occurrence of ZID_Ticker
    val occs = tickerTopic.getOccurrences(nextZIDOccType)
    if (occs.size == 0) { //create nextZID occurrence with initial value of 1
      tickerTopic.createIntOccurrence(nextZIDOccType, "1")
    } else if (occs.size == 1){
      occs.iterator().next()
    } else {
      throw new Exception("ZID_Ticker has more than one nextZID occurrences")
    }

  }
}
