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
  def pvpSafeToMoveOut: Boolean = {
    var output = safeToMoveOut
    output ||= dtBraveryAbroad
    output &&= enemies(Protoss.DarkTemplar) == 0 || unitsComplete(Protoss.Observer) > 0
    output
  }
  def pvpSafeAtHome: Boolean = {
    var output = safeAtHome
    output ||= dtBraveryHome
    output &&= enemies(Protoss.DarkTemplar) == 0 || unitsComplete(Protoss.Observer, Protoss.PhotonCannon) > 0
    output
  }

  val lateOneBaseDTFrame  : Int = GameTime(7, 30)()
  val twoBaseDTFrame      : Int = GameTime(15, 30)()
  val cannonSafetyFrames  : Int = Seconds(10)()

  private def makeObservers(): Unit = {
    pump(Protoss.Observer, if (enemyHasShown(Protoss.DarkTemplar)) 2 else 1)
  }
  def requireTimelyDetection(): Unit = {
    // Performance shortcut
    if (With.units.existsOurs(Protoss.Observer, Protoss.Observatory)) {
      makeObservers()
      return
    }
    lazy val dtArePossibility    = enemyDarkTemplarLikely || enemyContained || ! With.scouting.enemyMainFullyScouted || ( ! enemyRobo && ! With.fingerprints.threeGateGoon() && ! With.fingerprints.fourGateGoon()) || (With.frame > twoBaseDTFrame && safeToMoveOut)
    lazy val earliestArrival     = With.scouting.earliestArrival(Protoss.DarkTemplar)
    lazy val expectedArrival     = if (enemyDarkTemplarLikely || With.fingerprints.rampBlock()) earliestArrival else if (enemyContained || With.fingerprints.proxyGateway()) lateOneBaseDTFrame else twoBaseDTFrame
    lazy val framesUntilArrival  = expectedArrival - With.frame
    lazy val framesUntilObserver = framesUntilUnit(Protoss.Observer)
    lazy val dtPrecedesCannon    = framesUntilArrival < framesUntilUnit(Protoss.PhotonCannon) + cannonSafetyFrames
    lazy val dtPrecedesObserver  = framesUntilArrival < framesUntilObserver
    lazy val cannonsComplete     = With.units.ours.filter(Protoss.PhotonCannon).count(_.complete) >= Math.max(1, With.units.countOurs(Protoss.PhotonCannon))
    lazy val goObserver = (
      With.units.existsOurs(Protoss.RoboticsFacility) // It's part of our plan already
        || ( ! dtPrecedesObserver && ! PvPDT()) // Observers are just better if we can swing them
        || (dtPrecedesCannon && ! With.units.existsOurs(Protoss.Forge)) // Cannons are awful once DTs are already inside your base; Obs is better
        || (cannonsComplete && enemyHasShown(Protoss.DarkTemplar) && ! With.geography.ourNatural.ourUnits.exists(Protoss.PhotonCannon))) // We need to leave our base eventually

    // Performance check
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
        makeObservers()

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
