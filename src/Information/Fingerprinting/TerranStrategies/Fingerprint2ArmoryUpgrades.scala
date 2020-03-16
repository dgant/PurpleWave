package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.Terran

class Fingerprint2ArmoryUpgrades extends Fingerprint {

  override protected def investigate: Boolean = {
    lazy val hasStarport = With.units.existsEnemy(Terran.Starport)
    lazy val hasScienceFacility = With.units.existsEnemy(Terran.ScienceFacility)
    lazy val hasTwoUpgrades = With.enemy.hasUpgrade(Terran.MechDamage) && With.enemy.hasUpgrade(Terran.MechArmor)
    With.frame < GameTime(8, 0)() &&
      With.units.countEnemy(Terran.Factory) < 4 &&
      With.units.countEnemy(u => u.is(Terran.Armory) && (u.upgrading || hasStarport || hasScienceFacility)) > (if (hasTwoUpgrades) 0 else 1)
  }
  override protected def sticky: Boolean = true
}