package bootstrap.liftweb

import net.liftweb._
import common._
import http._
import js.jquery.JqJsCmds
import sitemap.{Menu, SiteMap}
import net.zutha.lib.uri.{RoleLoc, AssocLoc, ItemLoc, UriRedirector}
import net.zutha.model.auth.ZuthaOpenIdVendor
import util.Helpers._
import widgets.autocomplete.AutoComplete

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("net.zutha")

    //perform redirects
    LiftRules.dispatch.prepend {
      case UriRedirector(response) => () => Full(response)

    }

    //make SiteMap
    LiftRules.setSiteMap(SiteMap(
      Menu("Zutha Console") / "zutha_console",
      Menu("Home") / "index",
      Menu("Login") / "login",
      Menu(ItemLoc),
      Menu(AssocLoc),
      Menu(RoleLoc)
    ))

    //process OpenID
    LiftRules.dispatch.append(ZuthaOpenIdVendor.dispatchPF)
    
    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // make ajax calls wait longer before timing out
    LiftRules.ajaxPostTimeout = 10000

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    /*LiftRules.noticesEffects.default.set((notice: Box[NoticeType.Value],
                                          id: String) => {
      Full(JqJsCmds.FadeOut(id, 5 seconds, 2 seconds))
    })

    LiftRules.noticesAutoFadeOut.default.set( (notices: NoticeType.Value) => {
      notices match {
        case NoticeType.Notice => Full((5 seconds, 2 seconds))
        case _ => Empty
      }
    })*/

    //initialize widgets
    AutoComplete.init
  }
}

