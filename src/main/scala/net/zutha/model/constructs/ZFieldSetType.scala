package net.zutha.model.constructs

import net.zutha.model.datatypes.{ZUnboundedNNI, ZNonNegativeInteger}


trait ZFieldSetType {
  def declarationAssociation: ZAssociation
  def cardMin: ZNonNegativeInteger
  def cardMax: ZUnboundedNNI
}
