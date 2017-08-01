package Information.StrategyDetection

import Lifecycle.With
import bwapi.Race

case class FingerprintRace(race: Race) extends Fingerprint {
  override def matches: Boolean = With.enemies.exists(_.race == race)
}
