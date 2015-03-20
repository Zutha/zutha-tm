package net.zutha.snippet

import net.zutha.model.constructs._

import net.zutha.lib.uri.RoleLoc

import net.liftweb.widgets.autocomplete._

import xml.{NodeSeq,Text}
import net.liftweb.http
import http._
import js.jquery.JqJsCmds
import js.JsCmds._
import js.{JsCmds, JsCmd}
import xml.NodeSeq._
import net.liftweb.common.Logger
import net.zutha.model.builder.{AssociationFieldBuilder, AssociationFieldSetBuilder}
import net.zutha.model.datatypes.PropertyValue
import net.liftweb.util.{Helpers}
import Helpers._

object AssocTable extends Logger{
  /** Render a table to display all association Fields in assocFieldSet*/
  def makeAssocSetTable( assocFieldSet: ZAssociationFieldSet ) = {
    val item = assocFieldSet.parentItem
    val role = assocFieldSet.role
    val assocType = assocFieldSet.associationType
    val otherRoles = assocFieldSet.otherRoles.toSeq.sortBy(_.name)
    val propTypes = assocFieldSet.propertyTypes.toSeq.sortBy(_.name)
    val memberTypes = otherRoles ++ propTypes

    def makeRow( assocField: ZAssociationField ) = {
      ".role-players *" #> otherRoles.map{role =>
          val rolePlayers = assocField.getPlayers(role).toSeq.sortBy(_.zid) //TODO sort by worth
          SnippetUtils.makeItemLinkList(rolePlayers)
      } &
      ".prop-values *" #> propTypes.map{propType =>
          val propVals: Seq[NodeSeq] = assocField.association.getProperties(propType).toSeq.map(pv => Text(pv.valueString))
          SnippetUtils.makeElemList(propVals)
      }
    }

    ".role *" #> otherRoles.map{r =>
        "a *" #> r.nameF(role) &
        "a [href]" #> RoleLoc.makeUri(item,role,assocType,r)
      } &
    ".prop-type *" #> propTypes.map{p => Text(p.nameF(role)):NodeSeq} &
    ".association-row *" #> assocFieldSet.associationFields.toSeq.map(makeRow(_)) //TODO sort by name of first member
  }
}

