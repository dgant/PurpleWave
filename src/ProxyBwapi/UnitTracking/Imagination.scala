package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Mathematics.Shapes.Ring
import Performance.Cache
import ProxyBwapi.Orders
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.ForeignUnitInfo
import Utilities.?
import Utilities.Time.{Forever, Minutes, Seconds}
import Utilities.UnitFilters.{IsTank, IsWarrior}

object Imagination {

  def checkVisibility(unit: ForeignUnitInfo): Unit = {

    // Yay, we see the unit!
    //
    // Check BOTH exists and visible because I have seen cases where one value or the other is nonsensical
    // though this may just be a symptom of ghost unit data.
    if (unit.bwapiUnit.exists && unit.bwapiUnit.isVisible) {
      unit.changeVisibility(Visibility.Visible)
      return
    }

    // Speculative sanity check based on a case where an observer died while our detection was leaving the area but was flagged alive
    if ( ! unit.visible && unit.visibility == Visibility.Dead) return

    lazy val shouldBeVisible = ?(
      unit.unitClass.isBuilding,
      unit.tiles.exists(_.visible),
      unit.tile.visible)
    lazy val shouldBeDetected = unit.tile.friendlyDetected
    lazy val likelyBurrowed = (
      unit.visibility == Visibility.InvisibleBurrowed
      || unit.burrowed
      || unit.order == Orders.Burrowing
      || unit.order == Orders.VultureMine
      || (Terran.SpiderMine(unit) && With.framesSince(unit.frameDiscovered) < 48))
    lazy val shouldUnburrow = (
      likelyBurrowed
      && Terran.SpiderMine(unit)
      && unit.inTileRadius(3).exists(tripper =>
        tripper.unitClass.triggersSpiderMines
        && tripper.isEnemyOf(unit)
        && tripper.pixelDistanceEdge(unit) < 96
        && (unit.altitude >= tripper.altitude || tripper.visibleToOpponents)))

    // Assume units we haven't seen in a very long time are dead
    // - Timed units can just expire
    //   https://bwapi.github.io/class_b_w_a_p_i_1_1_unit_interface.html#aab43c4ebf2bcb43986f3b4b101b79201
    // - Irradiated biological units die eventually
    // - In free-for-all settings, it's probable someone else killed them
    val expectedSurvivalFrames =
      if (unit.isAny(Zerg.Broodling, Zerg.SpelLDarkSwarm, Protoss.SpellDisruptionWeb, Terran.SpellScannerSweep))
        unit.removalFrames - With.framesSince(unit.lastSeen)
      else if (unit.painfullyIrradiated)  unit.totalHealth * Seconds(37)() / 250 // https://liquipedia.net/starcraft/Irradiate
      else if (IsWarrior(unit))           ?(With.strategy.isFfa, Minutes(4), Minutes(8))()
      else                                Forever()

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

    // Buildings that can't move are either in the same place or dead
    if (unit.unitClass.isBuilding && ! unit.unitClass.isFlyingBuilding) {
      unit.changeVisibility(?(shouldBeVisible, Visibility.Dead, Visibility.InvisibleNearby))
      return
    }

    // Let the trail go cold, as appropriate
    lazy val atHome     = unit.metro.exists(_.bases.exists(_.owner == unit.player))
    lazy val hasCompany = unit.matchups.allies.exists(ally => With.framesSince(ally.lastSeen) < 24 * 10 && ally.pixelDistanceEdge(unit) < 32 * 15)
    if (With.framesSince(unit.lastSeen) > 24 * 20 && ! atHome && ! hasCompany) {
      unit.changeVisibility(Visibility.InvisibleMissing)
      return
    }

    // Imagination is expensive; limit re-tries
    if (With.framesSince(Math.max(unit.lastSeen, unit.lastImagination)) < 128) {
      if (unit.visibility == Visibility.InvisibleMissing) {
        return
      }
      if ( ! shouldBeVisible) {
        unit.changeVisibility(Visibility.InvisibleNearby)
        return
      }
    }

    // Predict the unit's location
    // If we fail to come up with a reasonable prediction, treat the unit as missing
    val predictedPixel: Option[Pixel] = predictPixel(unit)
    if (predictedPixel.isDefined) {
      unit.changeVisibility(Visibility.InvisibleNearby)
      unit.changePixel(predictedPixel.get)
    } else {
      unit.changeVisibility(Visibility.InvisibleMissing)
    }
  }

  // We want to imagine units heading towards the center of their army, adjusted for their effective range
  // eg. don't assume melee units will back all the way up to ranged units
  private val rangeMax = new Cache(() => Maff.max(With.units.enemy.map(_.effectiveRangePixels)).getOrElse(0.0))
  private val destination = new Cache(() =>
    Maff.maxBy(
      With.battles.divisions
        .filter(_.count(IsWarrior) > 0))(_.count(IsWarrior))
        .map(_.attackCentroidKey)
    .getOrElse(With.scouting.enemyMuscleOrigin.center))

  private def predictPixel(unit: ForeignUnitInfo): Option[Pixel] = {
    unit.lastImagination  = With.frame
    val topSpeed          = ?(IsTank(unit), Terran.SiegeTankUnsieged.topSpeed, unit.topSpeedPossible)
    val framesUnseen      = With.framesSince(unit.lastSeen)
    val maxPixelsAway     = 64 + framesUnseen * topSpeed
    val maxTilesAway      = Math.min(Maff.div32((Maff.sqrt2 * maxPixelsAway).toInt),  With.mapTileHeight + With.mapTileWidth)// The sqrt2 multiplier accounts for ground distance metrics being rectilinear
    val maxTilesAwaySq    = maxTilesAway * maxTilesAway
    val observedPixel     = unit.pixelObserved
    val observedTile      = observedPixel.tile
    val goalPixel         = ?(With.framesSince(unit.lastSeen) < Seconds(5)() || ! IsWarrior(unit), unit.presumptiveDestinationFinal, destination())
    val goalTile          = goalPixel.tile
    val expectedPixel     = observedPixel.projectUpTo(goalPixel, maxPixelsAway).clamp()
    val expectedTile      = expectedPixel.tile
    val canFly            = unit.unitClass.canFly

    @inline def isLegalPrediction(tile: Tile): Boolean = (
      tile.valid
      && ! tile.visible
      && tile.lastSeen <= unit.lastSeen
      && (tile == observedTile || (
        tile.tileDistanceSquared(observedTile) <= maxTilesAwaySq
        && (canFly || (
          tile.walkableUnchecked
          && tile.groundTiles(observedTile) <= maxTilesAway
          && tile.units.forall(u => u.flying || u == unit))))))

    val possiblePixels =
      (Seq(expectedPixel).view.filter(p => isLegalPrediction(p.tile))
      ++  ((0 to maxTilesAway).view.flatMap(Ring(_).map(expectedTile.add))
        ++ (0 to maxTilesAway).view.flatMap(Ring(_).map(observedTile.add)))
            .filter(isLegalPrediction)
            .map(_.center))

    val output = possiblePixels.headOption
    output
  }
}
