package Planning.Plans.GamePlans.Protoss.PvZ

import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels.DefendHall
import Placement.Access.PlacementQuery
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.?
import Utilities.Time.GameTime
import Utilities.UnitFilters.{IsAll, IsComplete, IsWarrior}

class PvZ2022 extends PvZ1BaseOpenings {

  override def executeBuild(): Unit = {
    open(allowExpanding = true)
  }

  private def getStorm = With.configuration.humanMode

  override def executeMain(): Unit = {
    new MeldArchons(?(getStorm, 49, 250)).update()
    var targetMiningBases = Math.min(With.geography.maxMiningBasesOurs, ?(unitsComplete(IsWarrior) >= 8, 2, 1))
    targetMiningBases = Math.max(targetMiningBases, unitsComplete(IsWarrior) / 12)
    targetMiningBases = Maff.clamp(targetMiningBases, 1, 4)
    if (enemies(Zerg.SunkenColony) > 0 && unitsComplete(IsWarrior) >= 5 && (safePushing || (enemies(Zerg.Lair) > 0 && With.scouting.enemyProximity < 0.5))) {
      targetMiningBases = Math.max(targetMiningBases, 2)
    }
    var requiredAttackers = 3
    if (enemyStrategy(With.fingerprints.fourPool, With.fingerprints.oneHatchGas, With.fingerprints.twoHatchMain)) {
      requiredAttackers += 4
    } else if (enemyStrategy(With.fingerprints.ninePool)) {
      requiredAttackers += 2
    }
    if (anticipateSpeedlings || enemyHasUpgrade(Zerg.ZerglingSpeed)) {
      requiredAttackers += 3
      if ( ! upgradeComplete(Protoss.GroundDamage)) {
        requiredAttackers += 3
      }
      if ( ! upgradeComplete(Protoss.ZealotSpeed)) {
        requiredAttackers += 3
      }
      if ( ! upgradeComplete(Protoss.DragoonRange)) {
        requiredAttackers += 3
      }
    }
    var shouldAttack = safePushing
    shouldAttack &&= unitsEver(IsAll(IsComplete, IsWarrior)) >= requiredAttackers
    shouldAttack ||= miningBases < targetMiningBases
    shouldAttack ||= targetMiningBases > 2
    shouldAttack ||= bases > 2
    shouldAttack ||= With.geography.ourMetros.size > 1
    if (enemies(Zerg.Mutalisk) > 0 || (enemyMutalisksLikely && enemiesComplete(Zerg.Spire) > 0)) {
      shouldAttack &&= unitsComplete(Protoss.PhotonCannon) >= 2 * bases || unitsComplete(Protoss.Corsair) >= 4 || unitsComplete(Protoss.Dragoon) >= 16
    }
    if (shouldAttack) attack() else harass()
    if (miningBases < targetMiningBases) {
      expandOnce()
    }

    status(enemyHydralisksLikely, "Hydras")
    status(enemyMutalisksLikely,  "Mutas")
    status(enemyLurkersLikely,    "Lurkers")
    status(anticipateSpeedlings,  "Speedlings")
    status(f"${targetMiningBases}base")

    if (enemyMutalisksLikely || (
      safeDefending
        && miningBases > 1
        && frame > With.scouting.earliestArrival(Zerg.Mutalisk) - Protoss.Forge.buildFrames - Protoss.PhotonCannon.buildFrames
        && ! enemyHydralisksLikely
        && ! enemyLurkersLikely)) {
      get(Protoss.Forge)
      With.geography.ourMiningBases.foreach(b => {
        def query = new PlacementQuery(Protoss.PhotonCannon)
          .requireBase(b)
          .requireLabelYes(DefendHall)
        get(1, Protoss.Pylon, query.preferBuilding(Protoss.Pylon))
        get(Maff.clamp((2 + Math.max(6, enemies(Zerg.Mutalisk)) / 3), 3, 5), Protoss.PhotonCannon, query)
      })
    }

    val safeVsMutalisk = 0.75 * units(Protoss.Dragoon) + 1.5 * units(Protoss.Corsair) > Math.max(enemies(Zerg.Mutalisk), ?(enemyMutalisksLikely, 6, 0))

    if (                  unitsComplete(IsWarrior) >= ?(safeDefending, 7, 12)) techStage1()
    if (safeVsMutalisk && unitsComplete(IsWarrior) >= ?(safeDefending, 12, 18) && miningBases > 1)  techStage2()
    if (safeVsMutalisk && unitsComplete(IsWarrior) >= ?(safeDefending, 18, 24) && miningBases > 1)  techStage3()

    pump(Protoss.Observer, ?(enemyLurkersLikely || enemyHasTech(Zerg.Burrow), 3, 1))
    if (enemyMutalisksLikely)    get(Protoss.Stargate)
    pumpRatio(Protoss.Corsair, ?(enemyMutalisksLikely, 1, 5), 12, Seq(Enemy(Zerg.Mutalisk, 1.0)))
    if (enemyLurkersLikely || (safeDefending && frame > GameTime(8, 30)())) {
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      buildCannonsAtOpenings(1)
    }
    if (enemyMutalisksLikely || units(Protoss.Dragoon) > 1) get(Protoss.DragoonRange)
    if (enemyMutalisksLikely && units(Protoss.Corsair) > 1) upgradeContinuously(Protoss.AirDamage) && upgradeContinuously(Protoss.AirArmor)
    pumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Zerg.Mutalisk, 1.5), Enemy(Zerg.Lurker, 1.25), Friendly(Protoss.Corsair, -1.5), Friendly(Protoss.Archon, 2.0)))
    pumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Zerg.Mutalisk, 3.0), Enemy(Zerg.Lurker, 2.5), Enemy(Zerg.Hydralisk, -0.5), Enemy(Zerg.Zergling, -0.25)))
    pumpShuttleAndReavers(6)
    if (enemyLurkersLikely && safeDefending) {
      upgradeContinuously(Protoss.ObserverSpeed)
    }
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    if (getStorm && units(Protoss.HighTemplar) > 0) {
      get(Protoss.PsionicStorm)
      if (gasPumps > 2) get(Protoss.HighTemplarEnergy)
    }
    if (unitsComplete(Protoss.Zealot) >= 10 && miningBases > 1) {
      techStage2() // Make sure we get Zealot speed
    }
    if (getStorm) {
      pumpRatio(Protoss.HighTemplar, 0, 8, Seq(Friendly(IsWarrior, 1.0 / 6.0)))
    } else {
      pump(Protoss.HighTemplar)
    }
    pumpRatio(Protoss.DarkTemplar, ?(enemyHasUpgrade(Zerg.OverlordSpeed) || enemyHasUpgrade(Zerg.OverlordVisionRange), 0, 1), 4, Seq(Friendly(IsWarrior, 1.0 / 10.0)))
    pumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Hydralisk, 0.5), Enemy(Zerg.Zergling, 0.35)))
    pumpRatio(Protoss.Zealot, 0, 24, Seq(Flat(-8), Friendly(Protoss.Dragoon, 1.0)))
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)

    get(2, Protoss.Gateway)
    techStage1()
    get(4, Protoss.Gateway)
    if (bases == 1) pumpWorkers(oversaturate = true, maximumTotal = 39)
    requireMiningBases(2)
    get(5, Protoss.Gateway)
    techStage2()
    addProduction()
    techStage3()
    get(5 * miningBases, Protoss.Gateway)
  }

  private def techStage1(): Unit = {
    get(Protoss.Assimilator, Protoss.CyberneticsCore)
    get(4, Protoss.Gateway)
    get(Protoss.DragoonRange)
  }

  private def techStage2(): Unit = {
    get(Protoss.Forge)
    if (enemyMutalisksLikely || ! enemyHydralisksLikely) {
      get(Protoss.Stargate)
    }
    if (enemies(Zerg.Mutalisk) >= 13) get(Maff.vmin(miningBases, gasPumps, 3), Protoss.Stargate)
    if (enemies(Zerg.Mutalisk) >= 7)  get(Maff.vmin(miningBases, gasPumps, 2), Protoss.Stargate)
    else if (enemyMutalisksLikely)    get(Protoss.Stargate)
    get(Protoss.CitadelOfAdun)
    get(Protoss.ZealotSpeed)
    if (gas < 300) buildGasPumps()
  }

  private def techStage3(): Unit = {
    get(Protoss.RoboticsFacility, Protoss.Shuttle, Protoss.Observatory, Protoss.Observer, Protoss.RoboticsSupportBay)
    get(2, Protoss.Reaver)
    get(Protoss.TemplarArchives)
    once(2, Protoss.HighTemplar)
    if (getStorm) {
      get(Protoss.PsionicStorm)
    }
  }

  private def addProduction(): Unit = {
    if (enemies(Zerg.Mutalisk) > 0 || enemyMutalisksLikely) {
      get(Math.min(3, 1 + enemies(Zerg.Mutalisk) / 5), Protoss.Stargate)
      once(Protoss.Corsair)
    }
    get(?(safeDefending, 2, 3) * miningBases, Protoss.Gateway)
  }
}
