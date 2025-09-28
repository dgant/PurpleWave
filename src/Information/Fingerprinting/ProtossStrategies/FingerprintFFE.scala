package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Time.{GameTime, Minutes, Seconds}

abstract class FingerprintFFE extends FingerprintAnd(
  new FingerprintNot(With.fingerprints.proxyGateway),
  new FingerprintNot(With.fingerprints.cannonRush),
  new FingerprintNot(With.fingerprints.twoGate),
  new FingerprintNot(With.fingerprints.oneGateCore),
  new FingerprintNot(With.fingerprints.nexusFirst)) {
  
  private val expectedFFEForge    = GameTime(1, 50)()
  private val expectedGFEForge    = GameTime(2, 26)()
  private val expectedFFEGateway  = GameTime(3, 20)()
  private val expectedGFEGateway  = GameTime(2,  3)()

  trait Conclusion
  object Undecided    extends Conclusion
  object ConcludeFFE  extends Conclusion
  object ConcludeGFE  extends Conclusion
  
  var decidedForge = false
  var decidedGateway = false

  var conclusion: Conclusion = Undecided
  override def investigate: Boolean = {
    if ( ! super.investigate) return false
    if ( With.frame > Minutes(5)()) return false
    conclusion = getConclusion
    conclusion != Undecided
  }

  private def getConclusion: Conclusion = {

    def couldBeWall(unit: UnitInfo) = ! unit.proxied && Maff.minBy(With.geography.mains.view.filterNot(_.isOurs))(_.heart.groundTiles(unit.tileTopLeft)).exists(m => m.natural.exists(n => unit.base.contains(n) || unit.proximity > n.townHallTile.proximity))
    val wallUnits = With.units.enemy.filter(couldBeWall)

    val noProxies         = ! With.units.enemy.exists(_.proxied)
    val expanded          = With.units.enemy.exists(u => Protoss.Nexus(u) && ! u.base.exists(_.isMain))
    val forge             = Maff.minBy(wallUnits.filter(Protoss.Forge))       (_.completionFrame)
    val gateway           = Maff.minBy(wallUnits.filter(Protoss.Gateway))     (_.completionFrame)
    val cannon            = Maff.minBy(wallUnits.filter(Protoss.PhotonCannon))(_.completionFrame)
    val zealot            = Maff.minBy(With.units.enemy.filter(Protoss.Zealot))(_.arrivalFrame)

    val forgeOrCannon     = forge.orElse(cannon)
    val forgeCompletion   = Maff.vmin(forge    .map(_.completionFrame), cannon.map(_.completionFrame - Protoss.Forge.buildFrames))
    val gatewayCompletion = Maff.vmin(gateway  .map(_.completionFrame), zealot.map(_.frameDiscovered - Protoss.Zealot.buildFrames), zealot.map(_.arrivalFrame - Protoss.Zealot.buildFrames - Seconds(25)()))
    val readyToDecide     = (forgeCompletion.isDefined && gatewayCompletion.isDefined) || (expanded && gatewayCompletion.isDefined) || (forge.toSeq ++ cannon).exists(u => With.framesSince(u.frameDiscovered) > Seconds(5)())
    val lossFFE           = Math.abs(forgeCompletion.getOrElse(With.frame + Protoss.Forge.buildFrames) - expectedFFEForge) + gatewayCompletion.map(f => Math.abs(f - expectedFFEGateway)).sum
    val lossGFE           = Math.abs(forgeCompletion.getOrElse(With.frame + Protoss.Forge.buildFrames) - expectedGFEForge) + gatewayCompletion.map(f => Math.abs(f - expectedGFEGateway)).sum

    var predictFFE    = gatewayCompletion.isEmpty
        predictFFE  ||= lossFFE < lossGFE
    var predictGFE    = gateway.isDefined && forge.isEmpty
        predictGFE  ||= lossFFE >= lossGFE
    
    if (readyToDecide && noProxies) {
      if (gateway .exists(_.completionFrame < GameTime(2, 18)())) return ConcludeGFE
      if (forge   .exists(_.completionFrame < GameTime(2, 5)()))  return ConcludeFFE
      if (predictFFE) return ConcludeFFE
      if (predictGFE) return ConcludeGFE
    }
    Undecided
  }
  
  val requiredConclusion: Conclusion
  
  override def sticky: Boolean = With.frame > GameTime(4, 45)()
}

class FingerprintForgeFE extends FingerprintFFE {
  override val requiredConclusion: Conclusion = ConcludeFFE
}

class FingerprintGatewayFE extends FingerprintFFE {
  override val requiredConclusion: Conclusion = ConcludeGFE
}