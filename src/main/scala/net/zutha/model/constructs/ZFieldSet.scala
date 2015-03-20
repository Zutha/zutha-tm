package net.zutha.model.constructs

import net.zutha.model.datatypes.{ZUnboundedNNI, ZNonNegativeInteger}


trait ZFieldSet {
  def isEmpty: Boolean
  def cardMin: ZNonNegativeInteger
  def cardMax: ZUnboundedNNI
  def fields: Set[ZField]
  def fieldType: ZFieldType
}
