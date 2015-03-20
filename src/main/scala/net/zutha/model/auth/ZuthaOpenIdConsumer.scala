package net.zutha.model.auth

import net.liftweb.openid.OpenIDConsumer
import net.liftweb.http.{RedirectResponse, S, LiftResponse}

class ZuthaOpenIdConsumer[UserType] extends OpenIDConsumer[UserType]{

  override def authRequest(userSuppliedString: String, targetUrl: String): LiftResponse = {
    // configure the return_to URL where your application will receive
    // the authentication responses from the OpenID provider
    val returnToUrl = S.encodeURL(S.hostAndPath + targetUrl)

    info("Creating openId auth request.  returnToUrl: "+returnToUrl)

    // perform discovery on the user-supplied identifier
    val discoveries = manager.discover(userSuppliedString)

    // attempt to associate with the OpenID provider
    // and retrieve one service endpoint for authentication
    val discovered = manager.associate(discoveries)

    S.containerSession.foreach(_.setAttribute("openid-disc", discovered))

    // obtain a AuthRequest message to be sent to the OpenID provider
    val authReq = manager.authenticate(discovered, returnToUrl)

    beforeAuth foreach {f => f(discovered, authReq)}

    // GET HTTP-redirect to the OpenID Provider endpoint
    // The only method supported in OpenID 1.x
    // redirect-URL usually limited ~2048 bytes
    val destURL = authReq.getDestinationUrl(true)
    RedirectResponse(destURL)
  }
}
