package Information.Fingerprinting.Generic

import Debugging.SimpleString
import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.MatchGasPump
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import Utilities.Time.FrameCount

class FingerprintGasCompleteBy(frameCount: FrameCount) extends Fingerprint {

  trait Proof
  case class PumpProof(child: Fingerprint) extends Proof
  case class UnitProof(unit: UnitInfo) extends Proof
  case class UpgradeProof(upgrade: Upgrade) extends Proof
  object NoProof extends Proof with SimpleString

  private val pumpCompleteBy = new FingerprintCompleteBy(MatchGasPump, frameCount)
  override protected val children = Seq(pumpCompleteBy)
  override protected def investigate: Boolean = proof != NoProof
  override val sticky = true
  override def reason: String = proof.toString

  private val keyFrame: Int = frameCount()

  protected def proof: Proof = {
    if (pumpCompleteBy.matches) return PumpProof(pumpCompleteBy)
    val gasUnitProof =
      With.units.enemy
        .filter(_.unitClass.gasPrice >= 25) // Amazingly, larva "cost" 1 gas
        .find(u =>
          With.frame <
            keyFrame
          + u.unitClass.buildFrames
          - u.remainingCompletionFrames
          + u.unitClass.gasPrice / With.accounting.workerIncomePerFrameGas / 3)
    if (gasUnitProof.isDefined) return UnitProof(gasUnitProof.get)
    val gasUpgradeProof = Upgrades.all
      .filter(u => With.enemies.exists(_.hasUpgrade(u)))
      .find(u =>
        With.frame <
          keyFrame
        + u.upgradeFrames.head._2
        + u.gasPrice.head._2 / With.accounting.workerIncomePerFrameGas / 3)
    if (gasUpgradeProof.isDefined) return UpgradeProof(gasUpgradeProof.get)
    NoProof
  }
}
