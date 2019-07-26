package Information.Intelligenze.Fingerprinting.TerranStrategies

import Information.Intelligenze.Fingerprinting.Fingerprint
import Lifecycle.With
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.Terran

class FingerprintBio extends Fingerprint {
  override protected def investigate: Boolean = {
    val enemyMech = (
        2 * With.units.countEnemy(UnitMatchSiegeTank)
      + 3 * With.units.countEnemy(Terran.Battlecruiser)
      + With.units.countEnemy(Terran.Vulture)
      + With.units.countEnemy(Terran.Goliath)
      + With.units.countEnemy(Terran.Wraith)
    )
    val enemyBio = ((
      With.units.countEnemy(Terran.Marine)
      + 2 * With.units.countEnemy(Terran.Firebat)
      + 2 * With.units.countEnemy(Terran.Medic)
    )
      * (if (With.enemies.exists(_.hasUpgrade(Terran.MarineRange))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasUpgrade(Terran.BioDamage))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasUpgrade(Terran.BioArmor))) 2.0 else 1.0)
      * (if (With.enemies.exists(_.hasTech(Terran.Stim))) 2.0 else 1.0)
    )
    enemyBio > Math.max(8, enemyMech)
  }

  override protected def sticky: Boolean = true
}
