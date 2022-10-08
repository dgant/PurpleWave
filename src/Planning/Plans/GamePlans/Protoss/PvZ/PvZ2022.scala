package Planning.Plans.GamePlans.Protoss.PvZ

import Debugging.SimpleString
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels.DefendHall
import Placement.Access.{PlaceLabels, PlacementQuery}
import Planning.Plans.Macro.Automatic.{Enemy, Flat, Friendly}
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.?
import Utilities.Time.GameTime
import Utilities.UnitFilters.IsWarrior

class PvZ2022 extends PvZ2022Openings {

  private trait Opening extends SimpleString
  private object Open910 extends Opening
  private object Open1012 extends Opening
  private object OpenZZCoreZ extends Opening
  private object OpenGateNexus extends Opening
  private var opening: Opening = Open1012

  override def executeBuild(): Unit = {
    if (units(Protoss.Gateway) < 2) {
      if (opening == Open1012 && enemyRecentStrategy(With.fingerprints.fourPool, With.fingerprints.ninePool)) {
        opening = Open910
      } else if (With.fingerprints.twelveHatch()) {
        opening = OpenGateNexus
      } else if (With.fingerprints.overpool()) {
        opening = OpenZZCoreZ
      }
    }
    opening match {
      case Open910 => open1012()
      case Open1012 => open1012()
      case OpenZZCoreZ => openZZCoreZ()
      case _ => openGateNexus()
    }
    if (bases <= 1 && anticipateSpeedlings) {
      get(Protoss.Forge)
      get(2, Protoss.PhotonCannon, new PlacementQuery(Protoss.PhotonCannon).requireLabelYes(PlaceLabels.DefendEntrance))
    }
  }

  override def executeMain(): Unit = {
    scoutOn(Protoss.Pylon)
    new MeldArchons(49).update()

    var targetMiningBases = Math.min(With.geography.maxMiningBasesOurs, ?(unitsComplete(IsWarrior) >= 8, 2, 1))
    targetMiningBases = Math.max(targetMiningBases, unitsComplete(IsWarrior) / 12)
    targetMiningBases = Maff.clamp(targetMiningBases, 1, 4)
    if (enemies(Zerg.SunkenColony) > 0 && safeToMoveOut && unitsComplete(IsWarrior) >= 5) {
      targetMiningBases = Math.max(targetMiningBases, 2)
    }
    var shouldAttack = safeToMoveOut || miningBases < targetMiningBases
    if (enemies(Zerg.Mutalisk) > 0 || (enemyMutalisksLikely && enemiesComplete(Zerg.Spire) > 0)) {
      shouldAttack &&= unitsComplete(Protoss.PhotonCannon) >= 2 * bases || unitsComplete(Protoss.Corsair) >= 4 || unitsComplete(Protoss.Dragoon) >= 16
    }
    if (shouldAttack) attack() else harass()
    requireMiningBases(targetMiningBases)

    if (enemyHydralisksLikely) status("Hydras")
    if (enemyMutalisksLikely) status("Mutas")
    if (enemyLurkersLikely) status("Lurkers")
    if (anticipateSpeedlings) status("Speedlings")
    status(f"${targetMiningBases}base")

    if (enemyMutalisksLikely || (
      safeAtHome
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
        get(?(enemies(Zerg.Mutalisk) < 9, 2, 3), Protoss.PhotonCannon, query)
      })
    }

    if (unitsComplete(IsWarrior) >= ?(safeAtHome, 7, 12)) techStage1()
    if (unitsComplete(IsWarrior) >= ?(safeAtHome, 12, 18) && miningBases > 1)  techStage2()
    if (unitsComplete(IsWarrior) >= ?(safeAtHome, 18, 24) && miningBases > 1)  techStage3()

    pump(Protoss.Observer, ?(enemyLurkersLikely || enemyHasTech(Zerg.Burrow), 3, 1))
    if (enemyMutalisksLikely) get(Protoss.Stargate)
    pumpRatio(Protoss.Corsair, ?(enemyMutalisksLikely, 1, 5), 12, Seq(Enemy(Zerg.Mutalisk, 1.0)))
    if (enemyMutalisksLikely || units(Protoss.Dragoon) > 1) get(Protoss.DragoonRange)
    if (enemyMutalisksLikely && units(Protoss.Corsair) > 1) upgradeContinuously(Protoss.AirDamage) && upgradeContinuously(Protoss.AirArmor)
    pumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Zerg.Mutalisk, 1.0)))
    if (enemyLurkersLikely || (safeAtHome && frame > GameTime(8, 30)())) {
      get(Protoss.RoboticsFacility, Protoss.Observatory)
      buildCannonsAtOpenings(1)
    }
    if (enemyLurkersLikely && safeAtHome) {
      upgradeContinuously(Protoss.ObserverSpeed)
    }
    if (units(Protoss.HighTemplar) > 0) {
      get(Protoss.PsionicStorm)
      if (gasPumps > 2) get(Protoss.HighTemplarEnergy)
    }
    if (unitsComplete(Protoss.Zealot) >= 10 && miningBases > 1) {
      techStage2() // Make sure we get Zealot speed
    }
    pumpRatio(Protoss.HighTemplar, 0, 8, Seq(Friendly(IsWarrior, 1.0 / 6.0)))
    pumpRatio(Protoss.DarkTemplar, ?(enemyHasUpgrade(Zerg.OverlordSpeed) || enemyHasUpgrade(Zerg.OverlordVisionRange), 0, 1), 4, Seq(Friendly(IsWarrior, 1.0 / 10.0)))
    pumpRatio(Protoss.Dragoon, 8, 24, Seq(Enemy(Zerg.Mutalisk, 1.0), Enemy(Zerg.Hydralisk, 0.5), Enemy(Zerg.Zergling, 0.35)))
    pumpRatio(Protoss.Zealot, 0, 24, Seq(Flat(-8), Friendly(Protoss.Dragoon, 1.0)))
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)

    get(2, Protoss.Gateway)
    techStage1()
    get(4, Protoss.Gateway)
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
    get(Protoss.Forge, Protoss.Stargate, Protoss.CitadelOfAdun)
    get(Protoss.ZealotSpeed)
    if (gas < 300) buildGasPumps()
  }

  private def techStage3(): Unit = {
    get(Protoss.TemplarArchives)
    once(2, Protoss.HighTemplar)
    get(Protoss.PsionicStorm)
    get(Protoss.RoboticsFacility, Protoss.Observatory)
    once(Protoss.Observer)
  }

  private def addProduction(): Unit = {
    if (enemies(Zerg.Mutalisk) > 0 || enemyMutalisksLikely) {
      get(Math.min(3, 1 + enemies(Zerg.Mutalisk) / 5), Protoss.Stargate)
      once(Protoss.Corsair)
    }
    get(?(safeAtHome, 2, 3) * miningBases, Protoss.Gateway)
  }
}
