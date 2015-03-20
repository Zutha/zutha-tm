package net.zutha.model.constructs

object ZItemType{
  def apply(item: ZItem): ZItemType = item.toItemType
  def unapply(item: ZItem): Option[ZItemType] =
    if(item.isItemType) Some(item.toItemType) else None
}
trait ZItemType extends ZType {
  def getAllSuperItemTypes: Set[ZItemType]
  def compatibleTraits: Set[ZTrait]
}
