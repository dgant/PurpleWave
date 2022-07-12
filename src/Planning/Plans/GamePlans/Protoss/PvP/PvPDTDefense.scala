package Planning.Plans.GamePlans.Protoss.PvP

import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Requests.RequestUnit
import Mathematics.Maff
import Placement.Access.PlaceLabels.{DefendEntrance, DefendGround, DefendHall, Defensive, PlaceLabel}
import Placement.Access.PlacementQuery
import Planning.Plans.GamePlans.All.MacroActions
import Planning.Predicates.MacroCounting
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvPDT}
import Utilities.Time._

object PvPDTDefense extends MacroActions with MacroCounting {

  private val twoBaseDTFrame = GameTime(15, 30)()
  private val cannonSafetyFrames = Seconds(10)()

  def requireTimelyDetection(): Unit = {
    val dtArePossibility    = enemyDarkTemplarLikely || ( ! enemyRobo && ! With.fingerprints.threeGateGoon() && ! With.fingerprints.fourGateGoon()) || (With.frame > twoBaseDTFrame && safeToMoveOut)
    val expectedArrival     = if (enemyDarkTemplarLikely) With.scouting.earliestArrival(Protoss.DarkTemplar) else twoBaseDTFrame
    val framesUntilArrival  = expectedArrival - With.frame
    val dtPrecedesCannon    = framesUntilArrival < framesUntilUnit(Protoss.PhotonCannon) + cannonSafetyFrames
    val dtPrecedesObserver  = framesUntilArrival < framesUntilUnit(Protoss.Observer)
    val cannonsComplete     = With.units.ours.filter(Protoss.PhotonCannon).count(_.complete) >= Math.max(1, With.units.countOurs(Protoss.PhotonCannon))
    val goObserver = (
      With.units.existsOurs(Protoss.RoboticsFacility) // It's part of our plan already
      || ( ! dtPrecedesObserver && ! PvPDT()) // Observers are just better if we can swing them
      || (dtPrecedesCannon && ! With.units.existsOurs(Protoss.Forge)) // Cannons are awful once DTs are already inside your base; Obs is better
      || (cannonsComplete && enemyHasShown(Protoss.DarkTemplar) && ! With.geography.ourNatural.units.exists(Protoss.PhotonCannon))) // We need to leave our base eventually

    if (enemyDarkTemplarLikely) status(f"ExpectDT@${Frames(expectedArrival)}")

    if (goObserver) {
      if (enemyDarkTemplarLikely) {
        val observerMinStartFrame     = expectedArrival          - Protoss.Observer.buildFrames         - Seconds(10)() // Some margin for error/travel time
        val observatoryMinStartFrame  = observerMinStartFrame    - Protoss.Observatory.buildFrames      - Protoss.Observatory.framesToFinishCompletion
        val roboMinStartFrame         = observatoryMinStartFrame - Protoss.RoboticsFacility.buildFrames - Protoss.RoboticsFacility.framesToFinishCompletion
        status(f"Obs4DT@${Frames(roboMinStartFrame)}-${Frames(observatoryMinStartFrame)}-${Frames(observerMinStartFrame)}")
        get(RequestUnit(Protoss.RoboticsFacility, 1, minStartFrameArg = roboMinStartFrame))
        get(RequestUnit(Protoss.Observatory,      1, minStartFrameArg = observatoryMinStartFrame))
        pump(Protoss.Observer, if (enemyHasShown(Protoss.DarkTemplar)) 2 else 1)
      }
    } else if (dtArePossibility && (PvPDT() || PvP3GateGoon())) {

      // If DTs are already here, spam cannons and pray one sticks
      if (framesUntilArrival < 120 || (With.geography.ourBases :+ With.geography.ourNatural).exists(_.units.exists(u => u.isEnemy && Protoss.DarkTemplar(u)))) {
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
      } else if (PvPDT() || enemyHasShown(Protoss.CitadelOfAdun) || ! With.fingerprints.dragoonRange()) {
        val cannonMinStartFrame = expectedArrival     - Protoss.PhotonCannon.buildFrames  - cannonSafetyFrames
        val forgeMinStartFrame  = cannonMinStartFrame - Protoss.Forge.buildFrames         - Protoss.Forge.framesToFinishCompletion
        val pylonMinStartFrame  = cannonMinStartFrame - Protoss.Pylon.buildFrames         - Protoss.Pylon.framesToFinishCompletion
        val naturalPylon        = With.units.ours.filter(Protoss.Pylon).forall(_.complete) && With.units.ours.count(Protoss.Pylon) > 4 - Maff.fromBoolean(With.scouting.weControlOurNatural)

        status(f"DTAfterCannon@${Frames(forgeMinStartFrame)}-${Frames(pylonMinStartFrame)}-${Frames(cannonMinStartFrame)}")
        if (naturalPylon) {
          status("NaturalPylon")
        }

        get(RequestUnit(Protoss.Forge, minStartFrameArg = forgeMinStartFrame))

        requestTower(Protoss.Pylon, 1, With.geography.ourNatural, DefendEntrance, if (naturalPylon) 0 else pylonMinStartFrame)

        if (dtArePossibility) {
          requestTower(Protoss.PhotonCannon, 1, With.geography.ourNatural,  DefendEntrance, cannonMinStartFrame)
          requestTower(Protoss.PhotonCannon, 1, With.geography.ourMain,     DefendEntrance, cannonMinStartFrame)
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
