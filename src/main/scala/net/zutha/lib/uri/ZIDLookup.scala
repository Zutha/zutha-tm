package net.zutha.lib.uri

import net.zutha.model.constructs.{Zid,ZItem}
import net.zutha.model.db.DB

object ZIDLookup {
  /**
   * extractor for a ZID
   * @return (wasRepaired,item)
   */
  def unapply(maybeZid: String): Option[(Boolean,ZItem)] = maybeZid match {
    case Zid(repairedZID) => DB.db.getItemByZid(Zid(repairedZID)) match {
        case Some(item) => Some( (maybeZid!=repairedZID), item)
        case _ => None
    }
    case _ => None
  }
}
