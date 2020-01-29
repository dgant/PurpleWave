package Information.Intelligenze.Fingerprinting.ProtossStrategies

import Information.Intelligenze.Fingerprinting.Generic._
import Lifecycle.With
import Planning.UnitMatchers.{UnitMatchBuilding, UnitMatchNot, UnitMatchProxied}
import ProxyBwapi.Races.Protoss
import Utilities.ByOption
import bwapi.Race

abstract class FingerprintFFE extends FingerprintAnd(
  new FingerprintRace(Race.Protoss),
  new FingerprintNot(With.fingerprints.proxyGateway),
  new FingerprintNot(With.fingerprints.cannonRush),
  new FingerprintNot(With.fingerprints.twoGate),
  new FingerprintNot(With.fingerprints.oneGateCore),
  new FingerprintNot(With.fingerprints.nexusFirst)) {
  
  private class Status {
    lazy val forge                  = With.units.enemy.find(u => u.is(Protoss.Forge) && ! u.zone.bases.exists(_.isStartLocation))
    lazy val cannonsWalled          = With.units.enemy.filter(u => u.isAll(Protoss.PhotonCannon, UnitMatchNot(UnitMatchProxied)) && ! u.zone.bases.exists(_.isStartLocation)).toVector
    lazy val buildingsProxied       = With.units.enemy.filter(_.isAll(UnitMatchBuilding, UnitMatchProxied)).toVector
    lazy val gateway                = With.units.enemy.find(_.is(Protoss.Gateway))
    lazy val zealot                 = With.units.enemy.filter(_.is(Protoss.Zealot)).toVector
    lazy val forgeOrCannon          = cannonsWalled ++ forge
    lazy val gatewayOrZealot        = gateway ++ zealot
    lazy val forgeCompletionFrame   = forge.map(_.completionFrame).orElse(ByOption.min(cannonsWalled.map(_.frameDiscovered)))
    lazy val gatewayCompletionFrame = gateway.map(_.completionFrame).orElse(ByOption.min(zealot.map(_.frameDiscovered)))
  }
  
  private val expectedFFEForge          = GameTime(2, 6)()
  private val expectedGatewayFEForge    = GameTime(2, 26)()
  private val expectedFFEGateway        = GameTime(3, 20)()
  private val expectedGatewayFEGateway  = GameTime(2, 24)()
  
  private def readyToDecide(status: Status): Boolean = (
    With.frame < GameTime(5, 0)() && (
      (status.forgeCompletionFrame.isDefined && status.gatewayCompletionFrame.isDefined)
      || status.forgeOrCannon.exists(u => u.complete && With.framesSince(u.frameDiscovered) > 24 * 5)
    )
  )
  
  private def gatewayUnlikely(status: Status): Boolean = status.gatewayOrZealot.isEmpty
  
  private def conclusivelyForge(status: Status): Boolean = (
    status.buildingsProxied.isEmpty
      && readyToDecide(status)
      && (gatewayUnlikely(status) || lossFFE(status) < lossGatewayFE(status))
  )
  
  private def conclusivelyGateway(status: Status): Boolean = (
    status.buildingsProxied.isEmpty
      && (
      (With.fingerprints.gatewayFirst.matches && With.units.enemy.exists(_.is(Protoss.Forge)))
      || (With.units.enemy.exists(u => u.is(Protoss.Gateway) && u.complete) && With.units.enemy.exists(u => u.is(Protoss.Forge) && ! u.complete))
      || (readyToDecide(status) && ! gatewayUnlikely(status) && lossFFE(status) >= lossGatewayFE(status)))
  )
  
  private def lossFFE(status: Status): Int = (
    Math.abs(status.forgeCompletionFrame.getOrElse(With.frame + Protoss.Forge.buildFrames) - expectedFFEForge)
    + status.gatewayCompletionFrame.map(f => Math.abs(f - expectedFFEGateway)).sum
  )
  
  private def lossGatewayFE(status: Status): Int = (
    Math.abs(status.forgeCompletionFrame.getOrElse(With.frame + Protoss.Forge.buildFrames) - expectedGatewayFEForge)
    + status.gatewayCompletionFrame.map(f => Math.abs(f - expectedGatewayFEGateway)).sum
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
  
  override def sticky = With.frame > GameTime(4, 45)()
}

class FingerprintForgeFE extends FingerprintFFE {
  override val isFFE: Boolean = true
}

class FingerprintGatewayFE extends FingerprintFFE {
  override val isFFE: Boolean = false
}