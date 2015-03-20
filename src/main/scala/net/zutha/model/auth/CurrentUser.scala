package net.zutha.model.auth

import net.liftweb.http.SessionVar
import net.zutha.model.item.ZuthaIdentity
import net.liftweb.common.{Empty, Box}


object CurrentUser extends SessionVar[Box[ZuthaIdentity]](Empty){
  def loggedIn: Boolean = is.isDefined
}
