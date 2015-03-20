package net.zutha.lib.widgets

import net.zutha.model.constructs.{ZItem}
import net.zutha.model.builder._

import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import net.liftweb.common.{Logger, Full}
import net.liftweb.util.{Helpers}
import net.liftweb.http.{S, js, SHtml}
import js.{JsCmd, JsCmds}
import js.JsCmds._
import net.zutha.snippet.SnippetUtils

object SimpleFieldSetEdit extends Logger {
  def apply(fieldSetB: FieldSetBuilder) = {
    ".simple-field" #> Helpers.findOrCreateId{id =>
      fieldSetB match {
        case propSet: PropertySetBuilder => {
          renderPropertySet(propSet,id)
        }
        case assocSet: AssociationFieldSetBuilder => {
          renderAssocFieldSet(assocSet,id)
        }
      }
    }
  }

  //property field
  def renderPropertySet( propSet: PropertySetBuilder, id: String ): NodeSeq => NodeSeq = {ns =>
    val addFieldGuid = nextFuncName

    def outNs: NodeSeq = {
      val sel =
        ".field-type *" #> propSet.fieldType.name &
        ".field" #> propSet.properties.map(renderProp) &
        ".add-field-input [id]" #> addFieldGuid &
        ".add-field-input *" #> SHtml.ajaxText("",addProperty) &
        ".add-field-link" #> (ns => SHtml.a(ns.head.child,JsCmds.JsShowId(addFieldGuid)))
      sel(ns)
    }

    def refreshPropSet = JsCmds.Replace(id,outNs)

    def renderProp(prop: PropertyBuilder) = {
      val propGuid = nextFuncName
      ".value [id]" #> propGuid &
      "input" #> SHtml.ajaxText(prop.value.asString,setProperty(prop)) &
      ".remove-field" #> SHtml.a(Text("remove")){
        propSet.removeProperty(prop)
        refreshPropSet
      }
    }

    def addProperty(value: String): JsCmd = {
      try{
        val newProp = propSet.addProperty
        newProp.foreach(p => p.value = value)
        refreshPropSet
      } catch {
        case e: IllegalArgumentException => {
          S.error(e.getMessage) //TODO associate with relevant form element
          Noop
        }
      }
    }

    def setProperty(prop: PropertyBuilder)(value: String) = {
      try(prop.value = value)
      catch{
        case e: IllegalArgumentException => {
          S.error(e.getMessage) //TODO associate with relevant form element
        }
      }
      Noop
    }

    outNs
  }


  //association field
  def renderAssocFieldSet( assocSet: AssociationFieldSetBuilder, fsId: String ): NodeSeq => NodeSeq = {ns =>
    val otherRole = assocSet.otherRoles.head //simple assocFields have only one other Role
    val addFieldGuid = nextFuncName

    def outNs: NodeSeq = {
      val sel =
        ".field-type *" #> assocSet.fieldType.name &
        ".field" #> fieldPlayers.map(renderRolePlayer) &
        ".add-field-input [id]" #> addFieldGuid &
        ".add-field-input *" #> SHtml.ajaxText("",addRolePlayer) &
        ".add-field-link [onclick]" #> JsCmds.JsShowId(addFieldGuid).toJsCmd
      sel(ns)
    }


    def refreshFieldSet = JsCmds.Replace(fsId,outNs)

    def fieldPlayers = assocSet.associationFields
      .flatMap( af => af.getPlayers(otherRole).map((af,_)) )

    def renderRolePlayer( fieldPlayer: (AssociationFieldBuilder,ZItem) ) = fieldPlayer match
    {case (assocField, rolePlayer) => {
      val playerGuid = nextFuncName
      def removeField = {
        assocField.removeRolePlayer(otherRole,rolePlayer)
        if(assocField.rolePlayers.isEmpty)
          assocSet.removeAssociationField(assocField)
        refreshFieldSet
      }
      ".field [id]" #> playerGuid &
      ".value" #> SnippetUtils.makeItemLink(rolePlayer) &
      ".remove-field [onclick]" #> SHtml.onEvent(_ => removeField)
    }}

    def addRolePlayer( rpName: String ) = {
      try{
        val rp = assocSet.fieldType.allowedPlayersOf(otherRole).filter{p =>
          (p.name equalsIgnoreCase rpName) ||
          (p.zid equalsIgnoreCase rpName)
        }.head
        val assocField = assocSet.addAssociationField match {
          case Full(af) => af
          case _ => assocSet.associationFields.head //if no more fields are allowed, try adding role-player to the first existing field
        }
        assocField.addRolePlayer(otherRole,rp) //TODO should throw exception if not allowed
        refreshFieldSet
      } catch {
        case e:NoSuchElementException => {
          S.error(rpName + " is not a valid player of role " + otherRole) //TODO send to associated field element
          Noop
        }
      }
    }

    outNs
  }



}
