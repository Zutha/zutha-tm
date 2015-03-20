package net.zutha.lib.widgets

import net.liftweb.util.Helpers
import Helpers._
import net.zutha.model.builder.{AssociationFieldBuilder, AssociationFieldSetBuilder}
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.zutha.snippet.SnippetUtils
import net.liftweb.http.{S, SHtml}
import xml.{Text, NodeSeq}
import net.zutha.model.constructs.{ZPropertyType, ZItem, ZAssociationPropertyType, ZRole}
import net.zutha.model.datatypes.PropertyValue
import net.liftweb.http.js.jquery.JqJsCmds
import xml.NodeSeq._
import net.liftweb.common.{Full, Logger}


object AssocTableEdit {
  def apply( assocFieldSet: AssociationFieldSetBuilder ) = {
    (new AssocTableEdit(assocFieldSet)).render _
  }
}

class AssocTableEdit protected ( assocFieldSet: AssociationFieldSetBuilder ) extends Logger{
  val focusItemRole = assocFieldSet.role
  val assocType = assocFieldSet.associationType
  val otherRoles = assocFieldSet.otherRoles
  val propTypes = assocFieldSet.propertyTypes
  val memberTypes = (otherRoles ++ propTypes).toSeq.sortBy(_.name) //TODO sort memberTypes by display-precedence
  val tableBodyId = nextFuncName

  def render(ns: NodeSeq) = {
    val assocRowNs = (".association-row ^^" #> "")(ns)
    val sel =
      ".member-type" #> memberTypes.map(mt => ".member-type *" #> mt.nameF(focusItemRole)) &
      "tbody [id]" #> tableBodyId &
      ".association-row" #> assocFields.map{renderRow} &
      ".add-row [onclick]" #> SHtml.onEvent(_ => addRow(assocRowNs))
    sel(ns)
  }
  def assocFields = assocFieldSet.associationFields.toSeq

  def addRow(assocRowNs: NodeSeq) = {
    assocFieldSet.addAssociationField match {
      case Full(assocField) => {
        JqJsCmds.AppendHtml(tableBodyId,renderRow(assocField)(assocRowNs))
      }
      case _ => {
        error("Association Field Set: " + assocFieldSet + " cannot have any more fields")
        Noop
      }
    }
  }

  def renderRow( assocField: AssociationFieldBuilder ): NodeSeq => NodeSeq = {
    val rowId = nextFuncName
    def out =
      ".association-row [id]" #> rowId &
      ".cell" #> memberTypes.map{mt => mt match {
        case otherRole:ZRole => renderRoleCell(otherRole) _
        case propType:ZAssociationPropertyType => renderPropertyCell(propType) _
      }} &
      ".remove-row [onclick]" #> SHtml.onEvent(_ => removeRow)

    def removeRow = {
      if(assocFieldSet.removeAssociationField(assocField)){
        JsCmds.Replace(rowId,Nil)
      }
      else Noop
    }

    /** Render Role Cell */
    def renderRoleCell( role: ZRole )( cellNs: NodeSeq ): NodeSeq = {
      def rolePlayers = assocField.getPlayers(role).toSeq.sortBy(_.name) //TODO sort by worth
      val addMemberGuid = nextFuncName
      val cellId = nextFuncName

      def outNs: NodeSeq = {
        val sel =
          ".cell [id]" #> cellId &
          ".member" #> rolePlayers.map{renderRolePlayer} &
          ".add-member-input [id]" #> addMemberGuid &
          ".add-member-input *" #> SHtml.ajaxText("",addRolePlayer) & //AutoComplete("",matchingRolePlayers,addRolePlayer)
          ".add-member-link [onclick]" #> JsCmds.JsShowId(addMemberGuid).toJsCmd
        sel(cellNs)
      }

      def refreshCell = JsCmds.Replace(cellId,outNs)

      def addRolePlayer(rpName:String) = {
        try{
          val rp = assocField.allowedPlayers(role).filter{p =>
            (p.name equalsIgnoreCase rpName) ||
            (p.zid equalsIgnoreCase rpName)
          }.head
          assocField.addRolePlayer(role,rp)
          refreshCell
        } catch {
          case e:NoSuchElementException => {
            S.error("invalid item name was passed back from autocomplete widget")
            Noop
          }
        }
      }

      def renderRolePlayer(rp: ZItem): NodeSeq => NodeSeq = {
        val memberGuid: String = nextFuncName
        def removePlayer = {
          if(assocField.removeRolePlayer(role,rp)){
            JsCmds.Replace(memberGuid,Nil)
          }
          else Noop
        }
        ".member [id]" #> memberGuid &
        ".value *" #> SnippetUtils.makeItemLink(rp) &
        ".remove-member [onclick]" #> SHtml.onEvent(_ => removePlayer)
      }

      return outNs
    }


    /** Render Property Cell */
    def renderPropertyCell( propType: ZAssociationPropertyType )( cellNs: NodeSeq ): NodeSeq = {
      val addMemberGuid = nextFuncName
      val cellId = nextFuncName
      
      def outNs: NodeSeq = {
        val sel =
          ".cell [id]" #> cellId &
          ".member" #> propVals.map{renderPropertyVal} &
          ".add-member-input [id]" #> addMemberGuid &
          ".add-member-input *" #> SHtml.ajaxText ("",addProperty) &
          ".add-member-link [onclick]" #> JsCmds.JsShowId(addMemberGuid).toJsCmd
        sel(cellNs)
      }

      def refreshCell = JsCmds.Replace(cellId,outNs)

      def propVals = assocField.getPropertyValues(propType).toSeq.sortBy(_.asString)

      def addProperty(strVal: String) = {
        val datatype = propType.dataType
        strVal match {
          case datatype(propVal) => {
            assocField.addProperty(propType,propVal)
            refreshCell
          }
          case _ => {
            S.error("Invalid value for this property type")
            //TODO attach error message to property element
            //TODO get error message from datatype object
            Noop
          }
        }
      }

      def renderPropertyVal(pv: PropertyValue): NodeSeq => NodeSeq = {
        val memberGuid: String = nextFuncName
        def removeProperty = {
          if(assocField.removeProperty(propType,pv))
            JsCmds.Replace(memberGuid,Nil)
          else Noop
        }
        ".member [id]" #> memberGuid &
        ".value *" #> pv.asString &
        ".remove-member [onclick]" #> SHtml.onEvent(_ => removeProperty)
      }

      return outNs
    }

    return out
  }

}
