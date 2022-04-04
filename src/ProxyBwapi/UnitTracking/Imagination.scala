package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, PixelRay}
import Mathematics.Shapes.Circle
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, Orders}
import Utilities.Time.{Forever, Minutes, Seconds}

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
      || (Terran.SpiderMine(unit) && With.framesSince(unit.frameDiscovered) < 48))
    lazy val shouldUnburrow = (
      likelyBurrowed
      && Terran.SpiderMine(unit)
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
      else if (IsWarrior(unit))
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
    lazy val friendsNearby = unit.team.exists(_.units.exists(f => f.visible && f.pixelDistanceEdge(unit) < 32 * 5 + unit.effectiveRangePixels))
    lazy val outrangesVision = unit.matchups.targets.nonEmpty && unit.matchups.enemies.forall(_.sightPixels <= unit.effectiveRangePixels)
    lazy val inferProximityFrames = Seconds(10 + 20 * Maff.fromBoolean(friendsNearby) + 20 * Maff.fromBoolean(outrangesVision))()
    if ( ! unit.base.exists(_.owner.isPlayer) && With.framesSince(unit.lastSeen) > inferProximityFrames) {
      unit.changeVisibility(Visibility.InvisibleMissing)
      return
    }

    // Predict the unit's location
    // If we fail to come up with a reasonable prediction, treat the unit as missing
    val predictedPixel: Option[Pixel] = predictPixel(unit)
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
      Maff.minBy(Circle.points(i)
        .map(unit.tile.add)
        .filter(tile =>
          tile.valid
          && tile.traversableBy(unit)
          && ! tile.visibleBwapi
          && tile.lastSeen < unit.lastSeen
          && tile.tileDistanceSquared(tileLastSeen) <= maxTilesAwaySquared
          && (unit.flying || PixelRay(unit.pixel, tile.center).forall(_.traversableBy(unit)))
        ))(_.center.pixelDistanceSquared(unit.projectFrames(8))))
      .find(_.nonEmpty)
      .flatten
      .map(_.center)
    output
  }
}
