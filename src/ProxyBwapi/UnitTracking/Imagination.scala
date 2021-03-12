package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Shapes.Circle
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, Orders}
import Utilities._

object Imagination {

  def checkVisibility(unit: ForeignUnitInfo): Unit = {
    // Speculative sanity check based on a case where an observer died while our detection was leaving the area but was flagged alive
    if ( ! unit.visible && unit.visibility == Visibility.Dead) return

    lazy val shouldBeVisible = unit.tile.visibleBwapi
    lazy val shouldBeDetected = unit.tile.friendlyDetected
    lazy val likelyBurrowed = (
      unit.visibility == Visibility.InvisibleBurrowed
      || unit.burrowed
      || Array(Orders.Burrowing, Orders.VultureMine).contains(unit.order)
      || (unit.is(Terran.SpiderMine) && With.framesSince(unit.frameDiscovered) < 48))
    lazy val shouldUnburrow = (
      likelyBurrowed
      && unit.is(Terran.SpiderMine)
      && unit.inTileRadius(3).exists(tripper =>
        tripper.unitClass.triggersSpiderMines
        && tripper.isEnemyOf(unit)
        && tripper.pixelDistanceEdge(unit) < 96
        && unit.altitude >= tripper.altitude))

    // Yay, we see the unit
    if (unit.bwapiUnit.isVisible) {
      unit.changeVisibility(Visibility.Visible)
      return
    }

    // Assume units we haven't seen in a very long time are dead
    // - Timed units can just expire
    //   https://bwapi.github.io/class_b_w_a_p_i_1_1_unit_interface.html#aab43c4ebf2bcb43986f3b4b101b79201
    // - Irradiated biological units die eventually
    // - In free-for-all settings, it's probable someone else killed them
    val expectedSurvivalFrames =
      if (unit.isAny(Zerg.Broodling, Zerg.SpelLDarkSwarm, Protoss.SpellDisruptionWeb, Terran.SpellScannerSweep))
        unit.removalFrames - With.framesSince(unit.lastSeen)
      else if (unit.unitClass.isOrganic && unit.irradiated)
        unit.totalHealth * Seconds(37)() / 250 // https://liquipedia.net/starcraft/Irradiate
      else if (unit.is(MatchWarriors))
        if (With.strategy.isFfa)
          Minutes(4)()
        else
          Minutes(8)()
      else
        Forever()

    if (With.framesSince(unit.lastSeen) > expectedSurvivalFrames) {
      unit.changeVisibility(Visibility.Dead)
      return
    }

    // If a Spider Mine should've been tripped, but hasn't, it's dead
    if (shouldBeVisible && likelyBurrowed && shouldUnburrow) {
      unit.changeVisibility(Visibility.Dead)
      return
    }

    // TODO: If a Lurker should have attacked, but hasn't, it's likely missing

    // Missing units stay missing
    if (unit.visibility == Visibility.InvisibleMissing) {
      return
    }

    // Assume other burrowing units burrowed
    if (likelyBurrowed) {
      unit.changeVisibility(Visibility.InvisibleBurrowed)
      if (shouldBeVisible && shouldBeDetected) {
        // This logic can fail if the detection grid is out of date.
        // This should be uncommon, though,
        // as it requires the unit to burrow in a tile that was recently detected but is no longer.
        unit.changeVisibility(Visibility.Dead)
      }
      return
    }

    // Presume the unit is alive but elsewhere
    unit.changeVisibility(Visibility.InvisibleNearby)

    // Missing buildings must either be floated or dead
    if (unit.unitClass.isBuilding && ! unit.unitClass.isFlyingBuilding) {
      if (shouldBeVisible) {
        unit.changeVisibility(Visibility.Dead)
      }
      return
    }

    // Invalidate long-absent units in the middle of the map
    if (With.framesSince(unit.lastSeen) > Seconds(60)() && ! unit.base.exists(_.owner.isPlayer)) {
      unit.changeVisibility(Visibility.InvisibleMissing)
      return
    }

    // Predict the unit's location
    // If we fail to come up with a reasonable prediction, treat the unit as missing
    val predictedPixel = predictPixel(unit)
    if (predictedPixel.isDefined) {
      unit.changePixel(predictedPixel.get)
    } else {
      unit.changeVisibility(Visibility.InvisibleMissing)
    }
  }

  private def predictPixel(unit: ForeignUnitInfo): Option[Pixel] = {
    if ( ! unit.tile.visible) {
      return Some(unit.pixel)
    }

    val tileLastSeen = unit.pixelObserved.tile
    val maxTilesAway = Math.min(12, With.framesSince(unit.lastSeen) * unit.topSpeed / 32)
    val maxTilesAwaySquared = 2 + maxTilesAway * maxTilesAway

    val output = (0 to 10).view.map(i =>
      ByOption.minBy(Circle.points(i)
        .map(unit.tile.add)
        .filter(tile =>
          tile.valid
          && tile.traversableBy(unit)
          && ! tile.visibleBwapi
          && tile.tileDistanceSquared(tileLastSeen) <= maxTilesAwaySquared
        ))(_.pixelCenter.pixelDistanceSquared(unit.projectFrames(8))))
      .find(_.nonEmpty)
      .flatten
      .map(_.pixelCenter)
    output
  }
}
