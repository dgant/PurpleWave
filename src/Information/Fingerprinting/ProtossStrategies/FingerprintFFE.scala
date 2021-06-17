package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import Mathematics.Maff
import Planning.UnitMatchers._
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.GameTime
import bwapi.Race

abstract class FingerprintFFE extends FingerprintAnd(
  new FingerprintRace(Race.Protoss),
  new FingerprintNot(With.fingerprints.proxyGateway),
  new FingerprintNot(With.fingerprints.cannonRush),
  new FingerprintNot(With.fingerprints.twoGate),
  new FingerprintNot(With.fingerprints.oneGateCore),
  new FingerprintNot(With.fingerprints.nexusFirst)) {
  
  private class Status {
    def couldBeWall(unit: UnitInfo) = unit.base.forall(_.isNaturalOf.isDefined) && ! unit.is(MatchProxied)
    lazy val forge                  = With.units.enemy.find(u => u.is(Protoss.Forge)    && couldBeWall(u))
    lazy val gateway                = With.units.enemy.find(u => u.is(Protoss.Gateway)  && couldBeWall(u))
    lazy val expanded               = With.units.countEnemy(Protoss.Nexus) > 1 || With.units.enemy.exists(u => u.is(Protoss.Nexus) && ! u.base.exists(_.isStartLocation))
    lazy val cannonsWalled          = With.units.enemy.view.filter(u => u.is(Protoss.PhotonCannon) && couldBeWall(u)).toVector
    lazy val buildingsProxied       = With.units.enemy.view.filter(_.isAll(MatchBuilding, MatchProxied)).toVector
    lazy val zealot                 = With.units.enemy.view.filter(Protoss.Zealot).toVector
    lazy val forgeOrCannon          = cannonsWalled ++ forge
    lazy val gatewayOrZealot        = gateway ++ zealot
    lazy val forgeCompletionFrame   = forge.map(_.completionFrame).orElse(Maff.min(cannonsWalled.map(_.frameDiscovered)))
    lazy val gatewayCompletionFrame = gateway.map(_.completionFrame).orElse(Maff.min(zealot.map(_.frameDiscovered)))
    lazy val isSomeKindOfFFE        = forgeOrCannon.nonEmpty || (gateway.isDefined && expanded)
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
      (With.fingerprints.gatewayFirst.matches && With.units.existsEnemy(Protoss.Forge))
      || (With.units.existsEnemy(MatchAnd(Protoss.Gateway, MatchComplete)) && With.units.existsEnemy(MatchAnd(Protoss.Gateway, MatchNot(MatchComplete))))
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
    if (status.isSomeKindOfFFE) {
      decidedForge = conclusivelyForge(status)
      decidedGateway = conclusivelyGateway(status)
      if (decidedForge) return isFFE
      if (decidedGateway) return !isFFE
    }
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