package Micro.Squads

import Information.Battles.Types.Division
import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchMobileDetector}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, CountMap}

class SquadCloakedHarass extends Squad {
  override def toString: String = "Cloak"
  override def run(): Unit = {
    // Desired targets:
    // - Detectors in progress
    // - Detector-enablers in progress
    // - Bases unprotected by detectors
    // - Divisions unprotected by detectors
    // - Likely expansions that have targets or haven't been seen for 45 seconds
    lazy val hasComsat = With.unitsShown.allEnemies(Terran.SpellScannerSweep) > 0 || With.units.enemy.exists(MatchAnd(Terran.Comsat, MatchComplete))
    val basesToConsider = With.geography.bases.filterNot(_.owner.isUs)
    val basesWithDetection = basesToConsider.map(base => (base, baseHasDetection(base))).toMap
    val basesToHarass = basesToConsider
      .sortBy(_.heart.groundPixels(With.scouting.mostBaselikeEnemyTile))
      .sortBy(_.owner.isNeutral)
      .sortBy( - baseWorkers(_))
      .sortBy(basesWithDetection)
    val basesAssigned = new CountMap[Base]
    lazy val detectionlessDivisions = With.battles.divisions.filter(d => ! d.enemies.exists(_.unitClass.isDetector))

    val sneakies = units.toVector.sortBy(-_.squadAge)
    sneakies.foreach(unit => {
      val topBases = basesToHarass.filter(base => base.owner.isEnemy && ( ! basesWithDetection.contains(base) || unit.base.contains(base)))
      lazy val lastOptionBase = basesToHarass.filterNot(basesWithDetection.contains).find(base => basesAssigned(base) <= ByOption.min(basesAssigned.values).getOrElse(0))
      if (topBases.nonEmpty) {
        val base = topBases.sortBy(base => unit.pixelDistanceTravelling(base.heart)).maxBy(baseProducingDetection)
        harassBase(unit, base)
        basesAssigned(base) += 1
      } else if (detectionlessDivisions.nonEmpty) {
        harassDivision(unit, detectionlessDivisions.minBy(d => ByOption.min(d.enemies.view.filter(unit.canAttack).map(_.pixel).map(unit.pixelDistanceTravelling)).getOrElse(With.mapPixelPerimeter.toDouble)))
      } else if (lastOptionBase.isDefined) {
        harassBase(unit, lastOptionBase.get)
        basesAssigned(lastOptionBase.get) += 1
      } else {
        unit.agent.intend(this, new Intention { toTravel = Some(With.scouting.mostBaselikeEnemyTile.center) })
      }
    })
  }

  def harassBase(unit: FriendlyUnitInfo, base: Base): Unit = {
    unit.agent.intend(this, new Intention { toTravel = Some(base.heart.center) })
  }

  def harassDivision(unit: FriendlyUnitInfo, division: Division): Unit = {
    val target = ByOption.minBy(division.enemies.view.filter(unit.canAttack))(unit.pixelsToGetInRange).map(_.pixel).getOrElse(With.scouting.mostBaselikeEnemyTile.center)
    unit.agent.intend(this, new Intention { toTravel = Some(target) })
  }

  private def baseProducingDetection(base: Base): Boolean = base.units.exists(u => ! u.complete && (u.unitClass.isDetector || u.isAny(Terran.EngineeringBay, Terran.Academy, Protoss.RoboticsFacility, Protoss.Observatory)))
  private def baseHasDetection(base: Base): Boolean = (
    base.units.exists(u => u.complete && u.unitClass.isDetector && u.isEnemy)
    || base.isNaturalOf.exists(baseHasDetection)
    || With.battles.divisions.exists(d => d.bases.contains(base) && d.enemies.exists(MatchMobileDetector)))
  private def baseWorkers(base: Base): Int= Math.max(
    base.units.count(u => u.isEnemy && u.unitClass.isWorker),
    if (base.scouted) 0 else if (base.owner.isEnemy || With.scouting.mostBaselikeEnemyTile.base.contains(base)) 21 else 0)
}
