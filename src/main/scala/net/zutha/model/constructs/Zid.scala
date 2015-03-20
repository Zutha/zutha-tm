package net.zutha.model.constructs

class Zid(zid: String) {
  val correctZID = zid match{
    case Zid(repairedZID) => repairedZID
    case _ => throw new IllegalArgumentException
  }

  def getHostID = correctZID.substring(0,getHostIdLength)

  def getIdentifier = correctZID.substring(getHostIdLength+1)

  private def getHostIdLength = Zid.charset.indexOf(correctZID(0)) + 1

  override def toString = correctZID
}

object Zid {
  private val ValidCharset = """\A([0-9A-Z][0-9A-Z]+)\z""".r
  val charset = "0123456789ABCDEFGHJKLMNPQRTUVWXY"

  def apply(zid: String) = new Zid(zid)

  /*  */


  /**
   * repairs the syntax of a ZID if it is valid
   * @param maybeZID string form of a ZID to try to resolve to a valid ZID
   * @return correctedID
   */
  def repair(maybeZID: String): Option[String] = correctCharset(maybeZID) match {
    case ValidCharset(zid) => {
      val hostIdLen = charset.indexOf(zid(0)) + 1
      if(zid.length > hostIdLen)
        Some(zid)
      else { //the id is too short
        None
      }
    }
    case _ => None //the id contains invalid characters
  }

  /**
   * extracts the corrected-syntax form of a ZID if it is valid
   * @param maybeZID string form of a ZID to try to resolve to a valid ZID
   * @return correctedID
   */
  def unapply(maybeZID: String): Option[String] = repair(maybeZID)

  /**
   * extracts the string representation of a ZID object
   * @return (ZID_string)
   */
  def unapply(zid: Zid): Option[String] = Some(zid.correctZID)

  private def correctCharset(zid: String) = {
    zid.toUpperCase.replace('O', '0').replace('I','1').replace('S','5').replace('Z','2')
  }



}
