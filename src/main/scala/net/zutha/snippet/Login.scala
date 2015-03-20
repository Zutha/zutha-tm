package net.zutha.snippet

import xml.NodeSeq
import net.liftweb._
import common.{Empty, Full}
import util._
import Helpers._
import net.zutha.model.auth.{CurrentUser, ZuthaOpenIdVendor}

class Login {

  def loginLink: NodeSeq => NodeSeq = {
    CurrentUser.is match {
      case Full(id) => ".currentUser *" #> id.name &
          ".currentUser [href]" #> "" & //TODO get URL of current User's Item
          ".logout [href]" #> ZuthaOpenIdVendor.logoutUrl
      case _ => "#loginStatus *" #> <span onclick="$('#loginCurtain').show();$('#loginLayer').show()" style="cursor: pointer;">Login</span>
    }
  }

  def loginCloseButton: NodeSeq => NodeSeq = {
    ".loginClose [onclick]" #> "$('#loginCurtain').hide();$('#loginLayer').hide()"
  }
}
