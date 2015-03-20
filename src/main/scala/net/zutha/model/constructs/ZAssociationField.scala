package net.zutha.model.constructs

trait ZAssociationField extends ZField{
  def parent: ZItem
  def role: ZRole
  def association: ZAssociation
  def companionAssociationFields: Set[ZAssociationField] = {
    val res = association.associationFields - this
    res
  }
  def otherPlayers: Set[ZItem] = association.players - parent
  def getPlayers(role: ZRole): Set[ZItem] = {
    val res = companionAssociationFields.filter{_.role == role}.map(_.parent)
    res
  }
  def otherRoles: Set[ZRole] = companionAssociationFields.map(_.role)
}
