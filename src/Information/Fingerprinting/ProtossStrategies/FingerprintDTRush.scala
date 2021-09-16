package Information.Fingerprinting.ProtossStrategies

import Information.Fingerprinting.Generic._
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Utilities.Time.GameTime

object FingerprintDTRushConstants {
  val dtArrivalFrame = GameTime(7, 15)
  val archiveFrame = GameTime(6, 45)
}
class FingerprintDTRush extends FingerprintOr(
  new FingerprintArrivesBy(Protoss.DarkTemplar, FingerprintDTRushConstants.dtArrivalFrame),
  new FingerprintCompleteBy(Protoss.TemplarArchives, FingerprintDTRushConstants.archiveFrame),
  new FingerprintAnd(
    new FingerprintCompleteBy(Protoss.CitadelOfAdun, GameTime(6, 15)),
    new FingerprintNot(With.fingerprints.dragoonRange),
    new FingerprintNot(With.fingerprints.fourGateGoon))) {
  
  override def sticky: Boolean = (
    With.frame >= FingerprintDTRushConstants.dtArrivalFrame()
    || new FingerprintArrivesBy(Protoss.DarkTemplar, FingerprintDTRushConstants.dtArrivalFrame).matches
    || new FingerprintCompleteBy(Protoss.TemplarArchives, FingerprintDTRushConstants.archiveFrame).matches)
}
