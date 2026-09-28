package Information.Fingerprinting.Generic

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import bwapi.Race

class FingerprintRace(race: Race) extends Fingerprint {
  override def investigate: Boolean = With.enemies.exists(_.raceCurrent == race)
  override def reason: String = f"$race: [${With.enemies.filter(_.raceCurrent == race).map(_.name).mkString(", ")}]"
}
