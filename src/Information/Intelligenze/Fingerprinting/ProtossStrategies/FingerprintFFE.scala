package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import Planning.Composition.UnitMatchers.{UnitMatchNot, UnitMatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.ByOption
import bwapi.Race

abstract class FingerprintFFE extends FingerprintAnd(
  new FingerprintRace(Race.Protoss),
  new FingerprintNot(With.intelligence.fingerprints.proxyGateway),
  new FingerprintNot(With.intelligence.fingerprints.cannonRush),
  new FingerprintNot(With.intelligence.fingerprints.twoGate),
  new FingerprintNot(With.intelligence.fingerprints.nexusFirst)) {
  
  private class Status {
    lazy val forge                  = With.units.enemy.find(_.is(Protoss.Forge))
    lazy val cannon                 = With.units.enemy.filter(_.isAll(Protoss.PhotonCannon, UnitMatchNot(UnitMatchProxied))).toVector
    lazy val gateway                = With.units.enemy.find(_.is(Protoss.Gateway))
    lazy val zealot                 = With.units.enemy.filter(_.is(Protoss.Zealot)).toVector
    lazy val forgeOrCannon          = cannon ++ forge
    lazy val gatewayOrZealot        = gateway ++ zealot
    lazy val forgeCompletionFrame   = forge.map(_.completionFrame).orElse(ByOption.min(cannon.map(_.frameDiscovered)))
    lazy val gatewayCompletionFrame = gateway.map(_.completionFrame).orElse(ByOption.min(zealot.map(_.frameDiscovered)))
  }
  
  private val expectedFFEForge          = GameTime(2, 6)()
  private val expectedGatewayFEForge    = GameTime(2, 26)()
  private val expectedFFEGateway        = GameTime(3, 20)()
  private val expectedGatewayFEGateway  = GameTime(2, 24)()
  
  private def readyToDecide(status: Status): Boolean = status.forgeCompletionFrame.isDefined && status.gatewayCompletionFrame.isDefined
  
  private def conclusivelyForge(status: Status): Boolean = {
    if (status.forge.exists(_.complete) && ! status.gatewayOrZealot.exists(_.complete)) return true
    if (status.cannon.nonEmpty && status.gatewayOrZealot.isEmpty) return true
    readyToDecide(status) && lossFFE(status) < lossGatewayFE(status)
  }
  
  private def conclusivelyGateway(status: Status): Boolean = {
    if (status.gateway.exists(g => g.complete && ! g.base.exists(_.isStartLocation)) && ! status.forgeOrCannon.exists(_.complete)) return true
    if (status.zealot.size > 2 && status.forgeOrCannon.isEmpty) return true
    readyToDecide(status) && lossFFE(status) >= lossGatewayFE(status)
  }
  
  private def lossFFE(status: Status): Int = (
    Math.abs(status.forgeCompletionFrame.get - expectedFFEForge)
    + Math.abs(status.gatewayCompletionFrame.get - expectedFFEGateway)
  )
  
  private def lossGatewayFE(status: Status): Int = (
    Math.abs(status.forgeCompletionFrame.get - expectedGatewayFEForge)
    + Math.abs(status.gatewayCompletionFrame.get - expectedGatewayFEGateway)
  )
  
  var decidedForge = false
  var decidedGateway = false
  override def investigate: Boolean = {
    if ( ! super.investigate) return false
  
    val status = new Status
    decidedForge = conclusivelyForge(status)
    decidedGateway = conclusivelyGateway(status)
    if (decidedForge) return isFFE
    if (decidedGateway) return ! isFFE
    false
  }
  
  val isFFE: Boolean
  
  override val sticky = false
}

class FingerprintForgeFE extends FingerprintFFE {
  override val isFFE: Boolean = true
}

class FingerprintGatewayFE extends FingerprintFFE {
  override val isFFE: Boolean = false
}