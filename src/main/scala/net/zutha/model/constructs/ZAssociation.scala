package net.zutha.model.constructs

import net.zutha.model.datatypes.{PropertyValue}

trait ZAssociation {
  override def toString = associationType.name +
    rolePlayers.toSeq.sortBy(_._1.name).map{ case (r,p) => r.name + ": " + p.name }.mkString( "(", ", ", ")" )
  def zid: String
  def zids: Set[String]
  def associationType: ZAssociationType
  def hasType(zType: ZType): Boolean
  def associationFields: Set[ZAssociationField]
  def playedRoles: Set[ZRole]
  def players: Set[ZItem]
  def rolePlayers: Set[(ZRole,ZItem)]
  def associationProperties: Set[(ZPropertyType,PropertyValue)]
  def getPlayers(role: ZRole): Set[ZItem]
  def getProperties(propType: ZPropertyType): Set[ZProperty]
  def getPropertyValues(propType: ZPropertyType): Set[PropertyValue]
  def getProperty(propType: ZPropertyType):Option[ZProperty]
  def getPropertyValue(propType: ZPropertyType): Option[PropertyValue]

  def addProperty(propType: ZPropertyType, value: PropertyValue): ZProperty

  /** @return the schema associations that override this one.
   *  Will always return an empty set for any associations that cannot be overridden. */
  def overriddenBy:Set[ZAssociation]
}
