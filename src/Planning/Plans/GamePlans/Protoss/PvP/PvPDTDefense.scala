package Planning.Plans.GamePlans.Protoss.PvP

import Lifecycle.With
import Mathematics.Maff
import Placement.Access.PlaceLabels
import Planning.Plans.GamePlans.All.MacroActions
import Planning.Predicates.MacroCounting
import ProxyBwapi.Races.Protoss
import Utilities.Time._

object PvPDTDefense extends MacroActions with MacroCounting {

  def expectedDTArrivalFrame: Int = {
    // Super-fast DT finishes 4:40 and thus arrives at the natural around 5:15. BetaStar demonstrates this on replay.
    var projectedArrival = GameTime(5, 15)()
    if (With.fingerprints.twoGate())      projectedArrival += GameTime(1, 10)()
    if (With.fingerprints.dragoonRange()) projectedArrival += GameTime(0, 30)()
    if ( ! enemyDarkTemplarLikely)        projectedArrival += GameTime(9, 15) ()// Guesstimate for delayed 2-base DT: 15:30 off one-gate core
    val knownArrival = Maff.min(With.units.enemy.filter(Protoss.DarkTemplar).map(_.arrivalFrame() - Seconds(15)() * Maff.fromBoolean(With.geography.ourBases.size > 1))).getOrElse(Forever())
    Math.min(projectedArrival, knownArrival)
  }

  def reactToDarkTemplarEmergencies(): Unit = {
    if ( ! enemyDarkTemplarLikely) return
    // Observer: Good if faster to build than a cannon (it's more useful and we spend less)
    // Observer: Good if DTs would arrive before cannons finish (they'll just die while under construction)
    val framesUntilArrival  = expectedDTArrivalFrame - With.frame
    val framesUntilObserver = framesUntilUnit(Protoss.Observer)
    val framesUntilCannon   = framesUntilUnit(Protoss.PhotonCannon) + Seconds(5)() * Maff.fromBoolean(units(Protoss.Forge) == 0)
    var goObserver          = framesUntilObserver < framesUntilCannon
    goObserver              ||= framesUntilArrival < framesUntilCannon && framesUntilObserver + Minutes(1)() < framesUntilCannon
    goObserver              ||= units(Protoss.RoboticsFacility) > 0
    status(f"${if (goObserver)"Obs" else "Can"}4DT@${Frames(expectedDTArrivalFrame)}")
    if (goObserver) {
      get(Protoss.RoboticsFacility)
      get(Protoss.Observatory)
    }

    if (enemyHasShown(Protoss.DarkTemplar)) {
      if (goObserver) {
        pump(Protoss.Observer, 3)
      } else {
        if (With.units.enemy.exists(u => Protoss.DarkTemplar(u) && u.base.exists(_.isOurs))) {
          buildCannonsAtBases(3, PlaceLabels.DefendHall)
        } else if (framesUntilCannon < framesUntilArrival) {
          buildCannonsAtNatural(2, PlaceLabels.DefendEntrance)
          buildCannonsAtMain(1, PlaceLabels.DefendEntrance)
        }
      }
    } else {
      if (goObserver) {
        get(Protoss.Observer)
      } else {
        buildCannonsAtBases(1, PlaceLabels.DefendEntrance)
      }
    }
  }
}
