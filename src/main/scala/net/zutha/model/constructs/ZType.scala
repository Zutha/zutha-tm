package net.zutha.model.constructs

import net.zutha.model.db.DB.db

trait ZType extends ZItem{
  //type information
  def isAbstract: Boolean;
  def hasAncestor(ancestor: ZType): Boolean

  //hierarchy

  /** @return the set of all this type's ancestor types (including this type itself)*/
  def ancestors: Set[ZType]

  /** @return the set of all this type's descendant types (including this type itself)*/
  def descendants: Set[ZType]

  def allInstances: Set[ZItem] = {
    db.allInstancesOfType(this)
  }

  // field definition
  def declaresFields: Boolean;
  def declaredPropertySets: Set[ZPropertySetType]
  def declaredAssociationFieldSets: Set[ZAssociationFieldSetType]

  def allowedPropertySets: Set[ZPropertySetType]
  def allowedAssociationFieldSets: Set[ZAssociationFieldSetType]

  def requiredPropertySets: Set[ZPropertySetType]
  def requiredAssociationFieldSets: Set[ZAssociationFieldSetType]

}
