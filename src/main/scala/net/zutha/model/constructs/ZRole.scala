package net.zutha.model.constructs

object ZRole{
  def apply(item: ZItem): ZRole = item.toRole
  def unapply(item: ZItem): Option[ZRole] =
    if(item.isRole) Some(item.toRole) else None
}
trait ZRole extends ZItem with ZAssociationMemberType{

}
