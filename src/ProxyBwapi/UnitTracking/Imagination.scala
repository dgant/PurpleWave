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

    lazy val shouldBeVisible = unit.tile.visible
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

    // Missing buildings must either be floated or dead. Burning is a common case.
    // Critters can move but we don't care about predicting their location
    if (unit.isNeutral || (unit.unitClass.isBuilding && ! unit.unitClass.isFlyingBuilding)) {
      if (shouldBeVisible) {
        unit.changeVisibility(Visibility.Dead)
      }
      return
    }

    // Imagination is expensive; limit re-tries
    if (unit.visibility == Visibility.InvisibleMissing && (With.framesSince(unit.lastImagination) < Seconds(5)() || With.framesSince(unit.lastSeen) < Seconds(30)()))  {
      return
    }
    unit.lastImagination = With.frame

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
    val topSpeed        = ?(IsTank(unit), Terran.SiegeTankUnsieged.topSpeed, unit.topSpeedPossible)
    val framesUnseen    = With.framesSince(unit.lastSeen)
    val maxPixelsAway   = 64 + framesUnseen * topSpeed
    val maxTilesAway    = Math.min((Maff.sqrt2 * maxPixelsAway).toInt / 32,  With.mapTileHeight + With.mapTileWidth)// The sqrt2 multiplier accounts for ground distance metrics being rectilinear
    val maxTilesAwaySq  = maxTilesAway * maxTilesAway
    val observedPixel   = unit.pixelObserved
    val observedTile    = observedPixel.tile
    val predictedTile   = unit.tile // TODO: Delete
    val goalPixel       = ?(With.framesSince(unit.lastSeen) < Seconds(10)() || ! IsWarrior(unit), unit.presumptiveDestination, unit.pixel.projectUpTo(destination(), rangeMax() - unit.effectiveRangePixels))
    val goalTile        = goalPixel.tile
    val expectedPixel   = observedPixel.projectUpTo(goalPixel, maxPixelsAway).clamp()
    val expectedTile    = expectedPixel.tile

    var positionsExplored = 0
    @inline def isLegalPrediction(tile: Tile): Boolean = (
      { positionsExplored += 1; true }
      && ! tile.visible
      && tile.lastSeen <= unit.lastSeen
      && (tile == observedTile || (
        (unit.unitClass.isFlyingBuilding || tile.traversableBy(unit))
        && tile.tileDistanceSquared(observedTile) <= maxTilesAwaySq
        && (unit.flying || tile.groundTiles(observedTile) <= maxTilesAway)
        && (unit.flying || tile.units.forall(unit==)))))

    val persistence = 3
    val possiblePixels =
      (Seq(expectedPixel).filter(p => isLegalPrediction(p.tile))
      ++ ((0 to maxTilesAway by persistence).view.flatMap(d1 => (0 until persistence).view.flatMap(d2 => Ring(d1 + d2).map(unit.tile.add)))
      ++  (0 to maxTilesAway by persistence).view.flatMap(d1 => (0 until persistence).view.flatMap(d2 => Ring(d1 + d2).map(expectedTile.add)))
      ++  (0 to maxTilesAway by persistence).view.flatMap(d1 => (0 until persistence).view.flatMap(d2 => Ring(d1 + d2).map(observedTile.add))))
        .filter(isLegalPrediction)
        .map(_.center))

    val output = possiblePixels.headOption
    if (output.isEmpty) {
      positionsExplored += 1 // TODO: For debug breakpoints only; delete later
    }
    if (positionsExplored < 0) {
      return None // TODO: Debugging only
    }
    output
  }
}
