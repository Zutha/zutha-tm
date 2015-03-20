package net.zutha.model.builder

import net.zutha.model.constructs._
import net.zutha.model.datatypes.PropertyValue
import net.liftweb.common.Logger
import net.liftweb.util.ValueCell
import net.zutha.model.db.DB._

class AssociationFieldBuilder private[builder](val parent: ItemBuilder, val associationFieldType: ZAssociationFieldType)
    extends FieldBuilder with Logger{
  private val _rolePlayers: ValueCell[Set[(ZRole,ZItem,Boolean)]] = ValueCell(Set())
  private val _properties: ValueCell[Set[(ZPropertyType,PropertyValue)]] = ValueCell(Set())

  //TODO validate add/remove actions before executing them

  def role = associationFieldType.role
  def associationType = associationFieldType.associationType
  def otherRoles = associationFieldType.otherRoles
  
  def rolePlayers = _rolePlayers.map{ case (r,i,l) => (r,i) }
  def getPlayers(role:ZRole) = _rolePlayers.filter(_._1 == role).map(_._2)

  def allowedPlayers(role:ZRole): Set[ZItem] = {
    val allowed = associationFieldType.allowedPlayersOf(role)
    allowed
  }

  private def _addRolePlayer( role: ZRole, player: ZItem, locked: Boolean ): Boolean = {
    _rolePlayers.atomicUpdate{_ + ((role,player,locked))}
    true
  }
  private[builder] def addLockedRolePlayer( rolePlayer: (ZRole,ZItem) ): Boolean = {
    _addRolePlayer(rolePlayer._1,rolePlayer._2,true)
  }
  def addRolePlayer( role: ZRole, player: ZItem ): Boolean = {
    _addRolePlayer(role,player,false)
  }
  def removeRolePlayer( role: ZRole, player: ZItem ): Boolean = {
     _rolePlayers.atomicUpdate{_ - ((role,player,false))}
    true
  }

  def properties = _properties
  def getPropertyValues( propType: ZPropertyType ) = _properties.filter(p => p._1 == propType).map(_._2)

  def addProperty( propType: ZPropertyType, value: PropertyValue ): Boolean = {
    debug(_properties)
    _properties.atomicUpdate{_ + ((propType,value))}
    debug(_properties)
    true
  }
  def removeProperty( propType: ZPropertyType, value: PropertyValue ): Boolean = {
    _properties.atomicUpdate{_ - ((propType,value))}
    true
  }

  /** Create a concrete Association item from this AssociationFieldBuilder */
  private[builder] def build(parentItem: ZItem) = {
    val rps: Seq[(ZRole, ZItem)] = (rolePlayers + ((role,parentItem)) ).toSeq
    val assoc = db.createAssociation(associationType, rps:_*)

    //add properties to association reifier
    for((propType,propVal) <- properties){
      assoc.addProperty(propType,propVal)
    }
    assoc
  }
}
