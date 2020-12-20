package ProxyBwapi.UnitTracking

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Shapes.Circle
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Players.Players
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, Orders}
import Utilities._

import scala.collection.JavaConverters._
import scala.collection.mutable

class ForeignUnitTracker {
  
  private val unitsById = new mutable.HashMap[Int, ForeignUnitInfo]()

  def allyUnits     : Iterable[ForeignUnitInfo] = unitsById.values.filter(_.player.isAlly)
  def enemyUnits    : Iterable[ForeignUnitInfo] = unitsById.values.filter(_.player.isEnemy)
  def neutralUnits  : Iterable[ForeignUnitInfo] = unitsById.values.filter(_.player.isNeutral)

  def get(id: Int): Option[ForeignUnitInfo] = unitsById.get(id)

  def update() {
    // Remove any units who have changed owners
    unitsById.values.filter(u => u.baseUnit.isVisible && u.baseUnit.getPlayer.getID != u.player.id).toVector.foreach(remove)

    // Add static neutral units
    if (unitsById.isEmpty && With.frame < 24) {
       With.game.getStaticNeutralUnits.asScala.foreach(add)
    }

    Players.all.view
      .filterNot(_.isUs)
      .flatMap(_.rawUnits)
      .filter(_.exists)
      .foreach(bwapiUnit => {
        val unit = unitsById.get(bwapiUnit.getID)
        if (unit.isDefined) {
          unit.get.update()
        } else {
          add(bwapiUnit)
        }
      })

    unitsById.values.foreach(checkVisibility)

    unitsById.values.view.filterNot(_.alive).toVector.foreach(remove)
  }

  def onUnitDestroyOrRenegade(unit: bwapi.Unit) {
    unitsById.get(unit.getID).foreach(remove)
  }

  private def add(bwapiUnit: bwapi.Unit): Unit = {
    val newUnit = new ForeignUnitInfo(bwapiUnit, bwapiUnit.getID)
    newUnit.update()
    unitsById.put(bwapiUnit.getID, newUnit)
  }

  private def remove(unit: ForeignUnitInfo) {
    unit.setVisbility(Visibility.Dead)
    unitsById.remove(unit.id)
    With.units.historicalUnitTracker.add(unit)
  }

  private def checkVisibility(unit: ForeignUnitInfo) {
    lazy val shouldBeVisible = unit.tileIncludingCenter.visibleBwapi
    lazy val shouldBeDetected = unit.tileIncludingCenter.friendlyDetected
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
        && unit.altitudeBonus >= tripper.altitudeBonus))

    // Yay, we see the unit
    if (unit.baseUnit.isVisible) {
      unit.setVisbility(Visibility.Visible)
      return
    }

    // Assume units we haven't seen in a very long time are dead
    // - Timed units can just expire
    //   https://bwapi.github.io/class_b_w_a_p_i_1_1_unit_interface.html#aab43c4ebf2bcb43986f3b4b101b79201
    // - Irradiated biological units die eventually
    // - In free-for-all settings, it's probable someone else killed them
    val expectedSurvivalFrames =
      if (unit.isAny(Zerg.Broodling, Zerg.SpelLDarkSwarm, Protoss.SpellDisruptionWeb, Terran.SpellScannerSweep))
        unit.framesUntilRemoval - With.framesSince(unit.lastSeen)
      else if (unit.unitClass.isOrganic && unit.irradiated)
        unit.totalHealth * Seconds(37)() / 250 // https://liquipedia.net/starcraft/Irradiate
      else if (unit.is(UnitMatchWarriors))
        if (With.strategy.isFfa)
          Minutes(4)()
        else
          Minutes(8)()
      else
        Forever()

    if ( ! unit.lastSeenWithin(expectedSurvivalFrames)) {
      unit.setVisbility(Visibility.Dead)
      return
    }

    // If a Spider Mine should've been tripped, but hasn't, it's dead
    if (shouldBeVisible && likelyBurrowed && shouldUnburrow) {
      unit.setVisbility(Visibility.Dead)
      return
    }

    // TODO: If a Lurker should have attacked, but hasn't, it's likely missing

    // Missing units stay missing
    if (unit.visibility == Visibility.InvisibleMissing) {
      return
    }

    // Assume other burrowing units burrowed
    if (likelyBurrowed) {
      unit.setVisbility(Visibility.InvisibleBurrowed)
      if (shouldBeVisible && shouldBeDetected) {
        // This logic can fail if the detection grid is out of date.
        // This should be uncommon, though,
        // as it requires the unit to burrow in a tile that was recently detected but is no longer.
        unit.setVisbility(Visibility.Dead)
      }
      return
    }

    // Presume the unit is alive but elsewhere
    unit.setVisbility(Visibility.InvisibleNearby)

    // Missing buildings must either be floated or dead
    if (unit.unitClass.isBuilding && ! unit.unitClass.isFlyingBuilding) {
      if (shouldBeVisible) {
        unit.setVisbility(Visibility.Dead)
      }
      return
    }

    // If we haven't seen a unit in a long time, treat it as missing,
    // which indicates distrust of its predicted location
    if ( ! unit.lastSeenWithin(Seconds(60)()) && ! unit.base.exists(_.owner == unit.player)) {
      unit.setVisbility(Visibility.InvisibleMissing)
      return
    }

    // Predict the unit's location
    // If we fail to come up with a reasonable prediction, treat the unit as missing
    val predictedPixel = predictPixel(unit)
    if (predictedPixel.isDefined) {
      unit.presumePixel(predictedPixel.get)
    } else {
      unit.setVisbility(Visibility.InvisibleMissing)
    }
  }

  private def predictPixel(unit: ForeignUnitInfo): Option[Pixel] = {
    if ( ! unit.tileIncludingCenter.visible) {
      return Some(unit.pixelCenter)
    }

    val tileLastSeen = unit.pixelCenterObserved.tileIncluding
    val maxTilesAway = 1 + With.framesSince(unit.lastSeen) * unit.topSpeed / 32
    val maxTilesAwaySquared = maxTilesAway * maxTilesAway

    val output = (0 to 10).view.map(i =>
      ByOption.minBy(Circle.points(i)
        .map(unit.tileIncludingCenter.add)
        .filter(tile =>
          tile.valid
          && (unit.flying || tile.walkableUnchecked)
          && ! tile.visibleBwapi
          && tile.tileDistanceSquared(tileLastSeen) <= maxTilesAway
        ))(_.pixelCenter.pixelDistanceSquared(unit.projectFrames(8))))
      .find(_.nonEmpty)
      .flatten
      .map(_.pixelCenter)

    output
  }
}
