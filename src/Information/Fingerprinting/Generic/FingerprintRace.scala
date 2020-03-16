package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import bwapi.Race

class FingerprintRace(race: Race) extends Fingerprint {
  override def investigate: Boolean = With.enemies.exists(_.raceInitial == race)
}
