package Information.Fingerprinting.TerranStrategies

import Information.Fingerprinting.Fingerprint
import Lifecycle.With
import ProxyBwapi.Races.Terran
import Utilities.Time.GameTime

class Fingerprint1ArmoryUpgrades extends Fingerprint {
  override protected def investigate: Boolean = {
    With.frame < GameTime(8, 0)() &&
      With.units.countEnemy(Terran.Factory) < 5 && (
      With.units.existsEnemy(u => u.is(Terran.Armory) && u.upgrading)
      || With.enemy.hasUpgrade(Terran.MechDamage)
      || With.enemy.hasUpgrade(Terran.MechArmor)
      || With.enemy.hasUpgrade(Terran.AirDamage)
      || With.enemy.hasUpgrade(Terran.AirArmor))
  }
  override protected def sticky: Boolean = true
}