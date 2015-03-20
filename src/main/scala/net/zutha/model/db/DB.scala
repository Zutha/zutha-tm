package net.zutha.model.db

import net.zutha.model.constructs._

trait DB extends SchemaItems{
  def getNextZID: Zid;

  def getItemByZid(zid: Zid): Option[ZItem];

  //Queries
  def findAssociations(assocType: ZAssociationType, strict: Boolean, rolePlayers:(ZRole,ZItem)*): Set[ZAssociation]
  def directTypesOfItem(item: ZItem): Set[ZType]
  def ancestorsOfType(zType: ZType): Set[ZType]
  def allTypesOfItem(item: ZItem): Set[ZType]
  def itemIsA(item: ZItem, zType: ZType): Boolean
  def allInstancesOfType(zType: ZType): Set[ZItem]
  def descendantsOfType(zType: ZType): Set[ZType]
  def traverseAssociation(item: ZItem, role: ZRole, assocType: ZAssociationType, otherRole: ZRole): Set[ZItem]
  def allItems: Set[ZItem]
  def allAssociations : Set[ZAssociation]

  //Construct Creation
  def createAssociation(assocType: ZAssociationType, rolePlayers: (ZRole, ZItem)*): ZAssociation
  def createItem(itemType: ZItemType, name: String): ZItem
}

object DB {
  val topicMapDB = "net.zutha.model.topicmap.db.TopicMapDB"

  val dbClass = Class.forName(topicMapDB).asInstanceOf[Class[DB]]
  val db: DB = dbClass.newInstance()
}
