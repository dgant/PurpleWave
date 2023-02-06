package Planning.Plans.GamePlans.Terran.TvP

import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels
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

    trainArmy() // TODO: Bypass; avoid duping
    get(Terran.Barracks)
    get(Terran.Refinery)
    get(Terran.Factory)
    get(Terran.MachineShop)
    requireMiningBases(2)
    get(2, Terran.Factory)
    addScanOrTurrets()
    buildGasPumps()
    shopTech()
    singleUpgrades() // TODO: Avoid duping
    addScan()
    get(3, Terran.Factory)
    requireMiningBases(3)
    doubleUpgrades()
    get(6, Terran.Factory)
    get(2, Terran.MachineShop)
    get(10, Terran.Factory)
    get(3, Terran.MachineShop)
  }

  def shopTech(): Unit = {
    if (units(Terran.Vulture) >= 3) {
      get(Terran.SpiderMinePlant)
    }
    get(Terran.SiegeMode)
    get(Terran.SpiderMinePlant)
    get(Terran.VultureSpeed)
  }

  def addScanOrTurrets(): Unit = {
    if (skipTurrets) addScan() else addTurrets()
  }

  def addScan(): Unit = {
    get(Terran.Academy)
    pump(Terran.Comsat, units(Terran.CommandCenter))
  }

  def addTurrets(): Unit = {
    get(Terran.EngineeringBay)
    buildTurretsAtNatural(1, PlaceLabels.DefendEntrance)
    buildTurretsAtMain(1, PlaceLabels.DefendHall)
  }

  def singleUpgrades(): Unit = {
    get(Terran.Armory)
    upgradeContinuously(Terran.MechDamage) && upgradeContinuously(Terran.MechArmor)
    get(Terran.Starport)
    get(Terran.ScienceFacility)
  }

  def doubleUpgrades(): Unit = {
    get(Terran.Armory)
    upgradeContinuously(Terran.MechDamage)
    get(Terran.Starport)
    get(2, Terran.Armory)
    get(Terran.ScienceFacility)
    upgradeContinuously(Terran.MechArmor)
  }

  def trainArmy(): Unit = {
    pumpRatio(Terran.Goliath, ?(enemyCarriersLikely, 6, units(IsWarrior) / 12), 48, Seq(Enemy(Protoss.Carrier, 6)))
    pump(Terran.ScienceVessel, ?(enemyDarkTemplarLikely || enemyArbitersLikely, 1, 0))
    pump(Terran.ScienceVessel, Maff.vmin(3, units(IsWarrior) / 20, enemies(Protoss.Arbiter)))
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture)
  }
}
