package net.zutha.model.constructs


trait ZTrait extends ZType{
  def compatibleItemTypes: Set[ZItemType]
}
