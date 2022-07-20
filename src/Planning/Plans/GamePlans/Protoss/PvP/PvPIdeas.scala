package Planning.Plans.GamePlans.Protoss.PvP

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Requests.RequestUnit
import Placement.Access.PlaceLabels._
import Placement.Access.PlacementQuery
import Planning.Plans.GamePlans.All.MacroActions
import Planning.Predicates.MacroCounting
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Strategies.Protoss._
import Utilities.Time.{Frames, GameTime, Minutes, Seconds}
import Utilities.UnitFilters.IsWarrior

object PvPIdeas extends MacroActions with MacroCounting {
  def enemyLowUnitStrategy: Boolean = enemyBases > 1 || enemyStrategy(
    With.fingerprints.nexusFirst,
    With.fingerprints.gatewayFe,
    With.fingerprints.forgeFe,
    With.fingerprints.robo,
    With.fingerprints.dtRush,
    With.fingerprints.cannonRush)

  def attackFirstZealot: Boolean = trackRecordLacks(With.fingerprints.twoGate, With.fingerprints.proxyGateway)

  def enemyContained: Boolean = With.geography.enemyBases.nonEmpty && With.geography.enemyBases.forall(b => (Seq(b) ++ b.natural).exists(With.scouting.weControl))

  def recentlyExpandedFirst: Boolean = With.scouting.weExpandedFirst && With.framesSince(With.scouting.firstExpansionFrameUs) < Minutes(3)()

  def dtBraveryAbroad : Boolean = unitsComplete(Protoss.DarkTemplar) > 0 && With.frame < With.scouting.earliestCompletion(Protoss.Observer)
  def dtBraveryHome   : Boolean = unitsComplete(Protoss.DarkTemplar) > 0 && With.frame < With.scouting.earliestArrival(Protoss.Observer)

  def shouldAttack: Boolean = {
    // Attack subject to global safety
    var output = enemyLowUnitStrategy
    output ||= unitsComplete(IsWarrior) > 0 && enemiesComplete(IsWarrior, Protoss.PhotonCannon) == 0 && (attackFirstZealot || With.frame > Minutes(4)() || unitsComplete(IsWarrior) > 2)
    output ||= employing(PvP1012)         && (unitsComplete(Protoss.Zealot) > 3 || ! enemyStrategy(With.fingerprints.twoGate))
    output ||= employing(PvPGateCoreGate) && unitsComplete(Protoss.Dragoon) > enemies(Protoss.Dragoon) && bases < 2
    output ||= employing(PvP3GateGoon)    && unitsComplete(Protoss.Gateway) >= 3 && unitsComplete(IsWarrior) >= 6
    output ||= employing(PvP4GateGoon)    && unitsComplete(Protoss.Gateway) >= 4 && unitsComplete(IsWarrior) >= 6
    output ||= enemyStrategy(With.fingerprints.dtRush) && unitsComplete(Protoss.Observer) > 1
    output ||= enemyStrategy(With.fingerprints.dtRush) && enemies(Protoss.DarkTemplar) == 0
    output ||= unitsComplete(Protoss.Shuttle) > 0 && unitsComplete(Protoss.Reaver) > 1 && unitsComplete(IsWarrior) >= 8
    output ||= upgradeComplete(Protoss.ZealotSpeed)
    output ||= enemyMiningBases > miningBases
    output ||= With.scouting.weExpandedFirst && ! recentlyExpandedFirst
    output ||= bases > 2
    output ||= bases > miningBases
    output &&= safeToMoveOut
    output &&= ! recentlyExpandedFirst

    // Attack disregarding global safety
    output ||= enemyBases > 1 && miningBases < 2
    output ||= dtBraveryAbroad

    // Don't let cannon rushes encroach
    output ||= With.fingerprints.cannonRush()

    // Attack when we have range advantage (and they're not hiding behind a wall)
    if ( ! enemyStrategy(With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.forgeFe)) {
      output ||= unitsComplete(Protoss.Dragoon) > 0     && ! enemyHasShown(Protoss.Dragoon)         && (enemiesShown(Protoss.Zealot) > 2 || With.fingerprints.twoGate())
      output ||= upgradeComplete(Protoss.DragoonRange)  && ! enemyHasUpgrade(Protoss.DragoonRange)
    }

    // Require Observer before attacking through a DT (unless we're going into a base trade! how exciting)
    output &&= unitsComplete(Protoss.Observer) > 0 || ! enemyHasShown(Protoss.DarkTemplar) || dtBraveryAbroad
    output
  }

  val lateOneBaseDTFrame  : Int = GameTime(7, 30)()
  val twoBaseDTFrame      : Int = GameTime(15, 30)()
  val cannonSafetyFrames  : Int = Seconds(10)()

  def requireTimelyDetection(): Unit = {
    val dtArePossibility    = enemyDarkTemplarLikely || enemyContained || ( ! enemyRobo && ! With.fingerprints.threeGateGoon() && ! With.fingerprints.fourGateGoon()) || (With.frame > twoBaseDTFrame && safeToMoveOut)
    val earliestArrival     = With.scouting.earliestArrival(Protoss.DarkTemplar)
    val expectedArrival     = if (enemyDarkTemplarLikely) earliestArrival else if (enemyContained || With.fingerprints.proxyGateway()) lateOneBaseDTFrame else twoBaseDTFrame
    val framesUntilArrival  = expectedArrival - With.frame
    val framesUntilObserver = framesUntilUnit(Protoss.Observer)
    val dtPrecedesCannon    = framesUntilArrival < framesUntilUnit(Protoss.PhotonCannon) + cannonSafetyFrames
    val dtPrecedesObserver  = framesUntilArrival < framesUntilObserver
    val cannonsComplete     = With.units.ours.filter(Protoss.PhotonCannon).count(_.complete) >= Math.max(1, With.units.countOurs(Protoss.PhotonCannon))
    val goObserver = (
      With.units.existsOurs(Protoss.RoboticsFacility) // It's part of our plan already
        || ( ! dtPrecedesObserver && ! PvPDT()) // Observers are just better if we can swing them
        || (dtPrecedesCannon && ! With.units.existsOurs(Protoss.Forge)) // Cannons are awful once DTs are already inside your base; Obs is better
        || (cannonsComplete && enemyHasShown(Protoss.DarkTemplar) && ! With.geography.ourNatural.ourUnits.exists(Protoss.PhotonCannon))) // We need to leave our base eventually

    if (enemyContained) status(f"Containing")
    if (enemyDarkTemplarLikely) status(f"ExpectDT@${Frames(expectedArrival)}")

    if (goObserver) {
      if (enemyDarkTemplarLikely) {
        val observerMinStartFrame     = expectedArrival          - Protoss.Observer.buildFrames         - Seconds(10)() // Some margin for error/travel time
        val observatoryMinStartFrame  = observerMinStartFrame    - Protoss.Observatory.buildFramesFull
        val roboMinStartFrame         = observatoryMinStartFrame - Protoss.RoboticsFacility.buildFramesFull
        status(f"Obs4DT@${Frames(roboMinStartFrame)}-${Frames(observatoryMinStartFrame)}-${Frames(observerMinStartFrame)}")
        get(RequestUnit(Protoss.RoboticsFacility, 1, minStartFrameArg = roboMinStartFrame))
        get(RequestUnit(Protoss.Observatory,      1, minStartFrameArg = observatoryMinStartFrame))
        pump(Protoss.Observer, if (enemyHasShown(Protoss.DarkTemplar)) 2 else 1)

        if (units(Protoss.RoboticsFacility) == 0 && enemyHasShown(Protoss.DarkTemplar, Protoss.TemplarArchives, Protoss.Arbiter, Protoss.ArbiterTribunal)) {
          val framesUntilGas = With.accounting.framesToMineGas(Protoss.RoboticsFacility.gasPrice - With.self.gas)
          if (framesUntilGas + framesUntilObserver > expectedArrival) {
            cancel(Protoss.Dragoon)
          }
        }
        if (units(Protoss.Observer) == 0 && With.units.ours.filter(_.isAny(Protoss.Shuttle, Protoss.Reaver)).forall(_.remainingCompletionFrames > framesUntilUnit(Protoss.Observatory))) {
          cancel(Protoss.Shuttle, Protoss.Reaver)
        }
      }
    } else if (dtArePossibility && (PvPDT() || PvPCoreExpand() || PvP3GateGoon() || PvP4GateGoon())) {

      // If DTs are already here, spam cannons and pray one sticks
      if (framesUntilArrival < 120 || (With.geography.ourBases :+ With.geography.ourNatural).exists(_.enemies.exists(Protoss.DarkTemplar))) {
        status("DTArrived")
        get(Protoss.Forge)
        get(RequestUnit(Protoss.PhotonCannon, 4,
          placementQueryArg = Some(new PlacementQuery(Protoss.PhotonCannon)
            .preferBase(With.geography.ourMain)
            .preferTile(With.geography.ourMain.heart)
            .preferLabelYes(Defensive, DefendHall, DefendGround))))

        // If DTs will arrive before cannons, try a tiered approach to maximize our potential outcomes
      } else if (dtPrecedesCannon) {
        status("DTPrecedesCannon")
        get(Protoss.Forge)
        requestTower(Protoss.PhotonCannon, 1, With.geography.ourNatural,  DefendEntrance, 0)
        requestTower(Protoss.PhotonCannon, 1, With.geography.ourMain,     DefendEntrance, 0)
        requestTower(Protoss.PhotonCannon, 1, With.geography.ourMain,     DefendHall,     0)

        // Take reasonable precautions
      } else {
        val cannonMinStartFrame = expectedArrival - Protoss.PhotonCannon.buildFramesFull - cannonSafetyFrames
        val forgeMinStartFrame  = earliestArrival - Protoss.Forge.buildFramesFull
        val pylonMinStartFrame  = earliestArrival - Protoss.Pylon.buildFramesFull
        val naturalPylonNow     = With.units.ours.filter(Protoss.Pylon).forall(_.complete) && With.scouting.weControlOurNatural && With.units.ours.count(Protoss.Pylon) > 3
        val doForgeCannon       = dtArePossibility && (PvPDT() || enemyHasShown(Protoss.CitadelOfAdun))

        status(f"DTAfterCannon@${Frames(forgeMinStartFrame)}-${Frames(pylonMinStartFrame)}-${Frames(cannonMinStartFrame)}")
        if (naturalPylonNow) status("NaturalPylon")

        if (doForgeCannon) {
          status("DoForgeCannon")
          get(RequestUnit(Protoss.Forge, minStartFrameArg = forgeMinStartFrame))
        }
        requestTower  (Protoss.Pylon,         1, With.geography.ourNatural,   DefendEntrance, if (naturalPylonNow) 0 else pylonMinStartFrame)
        if (doForgeCannon) {
          requestTower(Protoss.PhotonCannon,  1, With.geography.ourNatural,   DefendEntrance, cannonMinStartFrame)
          requestTower(Protoss.PhotonCannon,  1, With.geography.ourMain,      DefendEntrance, cannonMinStartFrame)
        }
      }
    }
  }
  private def requestTower(unitClass: UnitClass, quantity: Int, base: Base, label: PlaceLabel, startFrame: Int): Unit = {
    get(RequestUnit(unitClass, quantity, minStartFrameArg = startFrame,
      placementQueryArg = Some(new PlacementQuery(unitClass)
        .requireBase(base)
        .preferLabelYes(Defensive, DefendGround, label))))
  }
}
