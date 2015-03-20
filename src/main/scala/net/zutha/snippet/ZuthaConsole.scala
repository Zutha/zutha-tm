package net.zutha.snippet

import net.liftweb._
import http._
import util._
import Helpers._

class ZuthaConsole extends StatefulSnippet {

  private var queryStr = ""
  private var queryRes = ""
    
  def dispatch = {
    case "query" => runQuery
    case "buttons" => buttons
  }
    
  def runQuery =
    "textarea" #> SHtml.textarea(queryStr, (queryStr = _ )) &
    "#query_output *" #> queryRes &
    "type=submit" #> SHtml.onSubmitUnit(processQuery)
 
  def processQuery() {
      //TODO: implement my own query language
  }

  def buttons =
    "*" #> ""


}
