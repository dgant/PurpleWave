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
  
  private def readyToDecide(status: Status): Boolean = (
    (status.forgeCompletionFrame.isDefined && status.gatewayCompletionFrame.isDefined)
    || status.forgeOrCannon.exists(u => u.complete && With.framesSince(u.frameDiscovered) > 24 * 5)
  )
  
  private def conclusivelyForge(status: Status): Boolean = {
    readyToDecide(status) && lossFFE(status) < lossGatewayFE(status)
  }
  
  private def conclusivelyGateway(status: Status): Boolean = {
    readyToDecide(status) && lossFFE(status) >= lossGatewayFE(status)
  }
  
  private def lossFFE(status: Status): Int = (
    Math.abs(status.forgeCompletionFrame.getOrElse(With.frame + Protoss.Forge.buildFrames) - expectedFFEForge)
    + status.gatewayCompletionFrame.map(f => Math.abs(f - expectedFFEGateway)).sum
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