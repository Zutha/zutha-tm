package net.zutha.model.auth

import net.zutha.model.item.ZuthaIdentity


object Users {
  private val users = Map[String,ZuthaIdentity]()
  
  def apply(id: String):ZuthaIdentity = {
    users.getOrElse(id,makeNewZuthaIdentity)
  }

  def makeNewZuthaIdentity: ZuthaIdentity = {
    new ZuthaIdentity()
  }
}
