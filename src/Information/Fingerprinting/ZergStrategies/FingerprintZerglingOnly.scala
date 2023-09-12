package Information.Fingerprinting.ZergStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Zerg

class FingerprintZerglingOnly extends Fingerprint {

  private var disqualified: Boolean = false
  override protected def investigate: Boolean = {
    disqualified ||= With.units.existsEnemy(
      Zerg.Hydralisk,
      Zerg.HydraliskDen,
      Zerg.Lair,
      Zerg.Spire,
      Zerg.Mutalisk,
      Zerg.Scourge,
      Zerg.Lurker,
      Zerg.LurkerEgg,
      Zerg.Ultralisk,
      Zerg.Defiler,
      Zerg.Hive)
    ! disqualified
  }
  override val sticky: Boolean = false
}
