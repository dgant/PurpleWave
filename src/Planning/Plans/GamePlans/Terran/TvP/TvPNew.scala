package Planning.Plans.GamePlans.Terran.TvP

import Lifecycle.With
import Mathematics.Maff
import Planning.Plans.Macro.Automatic.Enemy
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsWarrior

class TvPNew extends TvPNewOpeners {
  // Min factories to take 3rd base: 2
  // Factories to take 3rd base on exposed map: 3
  // You can go fac-fac-academy vs gasless expands like nexus-first or zealot expand
  // If armory before ebay/academy, always academy because gols will be your anti-air
  // 1 rax fe vs gate-core-expand-robo, you want fac-fac-armory-academy
  // If protoss tech before range, reasonable to push on 3 tanks

  var expandedBeforeFactory: Boolean = _
  var skipTurrets: Boolean = _

  override def executeMain(): Unit = {
    expandedBeforeFactory ||= bases > 1 && ! have(Terran.Factory)
    if ( ! have(Terran.Academy, Terran.EngineeringBay)) {
      skipTurrets = enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.twoGate)
      skipTurrets ||= have(Terran.Armory)
    }

    val enemyCloak = enemyHasShown(Protoss.TemplarArchives, Protoss.DarkTemplar, Protoss.ArbiterTribunal, Protoss.Arbiter)
    var shouldAttack = false
    shouldAttack ||= With.scouting.enemyExpandedFirst && With.framesSince(With.scouting.firstExpansionFrameEnemy) < Minutes(3)() && (attacking || unitsComplete(IsWarrior) >= 6)
    shouldAttack &&= safeToMoveOut
    shouldAttack &&= ! enemyCloak || have(Terran.Comsat, Terran.ScienceVessel) || (unitsComplete(Terran.Vulture) >= 6 && Terran.SpiderMinePlant())

    get(Terran.Barracks)
    get(Terran.Refinery)
    get(Terran.Factory)
    get(Terran.MachineShop)
    requireMiningBases(2)
    get(2, Terran.Factory)
    requireMiningBases(3)
  }

  def trainArmy(): Unit = {
    pumpRatio(Terran.Goliath, ?(enemyCarriersLikely, 6, units(IsWarrior) / 12), 48, Seq(Enemy(Protoss.Carrier, 6)))
    pump(Terran.ScienceVessel, ?(enemyDarkTemplarLikely || enemyArbitersLikely, 1, 0))
    pump(Terran.ScienceVessel, Maff.vmin(3, units(IsWarrior) / 20, enemies(Protoss.Arbiter)))
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture)
  }
}
