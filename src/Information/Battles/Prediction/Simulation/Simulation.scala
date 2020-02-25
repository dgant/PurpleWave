package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.{LocalBattleMetrics, Prediction}
import Information.Battles.Types.{BattleLocal, Team}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

class Simulation(
  val battle    : BattleLocal,
  val weAttack  : Boolean,
  val weSnipe   : Boolean) {
  
  private def buildSimulacra(team: Team) = if (With.blackboard.mcrs()) Vector.empty else team.units.filter(legalForSimulation).map(new Simulacrum(this, _))
  private def legalForSimulation(unit: UnitInfo): Boolean = (
    unit.complete&& ! unit.unitClass.isSpell
    && ! unit.invincible          // No stasised units
    && ! unit.is(if (With.performance.danger) Protoss.Interceptor else Protoss.Carrier) // Interceptors produce more accurate results but are slower
    && ! unit.is(Protoss.Scarab)
    && ! (unit.unitClass.isWorker && unit.gathering && unit.isOurs)
    && ! (unit.unitClass.isBuilding && ! unit.canAttack && ! unit.unitClass.isSpellcaster)
  )

  val estimation            : Prediction          = new Prediction
  val focus                 : Pixel               = battle.focus
  val unitsOurs             : Vector[Simulacrum]  = buildSimulacra(battle.us)
  val unitsEnemy            : Vector[Simulacrum]  = buildSimulacra(battle.enemy)
  val everyone              : Vector[Simulacrum]  = unitsOurs ++ unitsEnemy
  var updated               : Boolean             = true
  var fleeing               : Boolean             = ! weAttack
  lazy val ourWidth         : Double              = battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else unit.unitClass.dimensionMin + unit.unitClass.dimensionMax).sum
  lazy val chokeMobility    : Map[Zone, Double]   = battle.us.units.map(_.zone).distinct.map(zone => (zone, getChokeMobility(zone))).toMap
  
  val simulacra: Map[UnitInfo, Simulacrum] = if (With.blackboard.mcrs()) Map.empty else
    (unitsOurs.filter(_.canMove) ++ unitsEnemy)
      .map(simulacrum => (simulacrum.realUnit, simulacrum))
      .toMap

  def complete: Boolean = (
    estimation.frames > With.configuration.simulationFrames
    || ! updated
    || unitsOurs.forall(_.dead)
    || unitsEnemy.forall(_.dead)
    || everyone.forall(u => u.dead || ! u.fighting)
  )
  
  def run() {
    if (!With.blackboard.mcrs()) {
      while (!complete) step()
    }
    cleanup()
  }
  
  def step() {
    updated = false
    estimation.frames += 1
    snipe()
    everyone.foreach(_.step())
    everyone.foreach(_.updateDeath())
    if (estimation.frames - estimation.localBattleMetrics.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationEstimationPeriod) {
      recordMetrics()
    }
  }
  
  def snipe() {
    if (weAttack
      && weSnipe
      && ! fleeing
      && unitsEnemy.exists(e => e.dead && ! e.realUnit.unitClass.suicides)) {
      fleeing = true
      unitsOurs.foreach(_.cooldownMoving = 24)
    }
  }

  def recordMetrics() {
    estimation.localBattleMetrics += new LocalBattleMetrics(this, estimation.localBattleMetrics.lastOption)
  }
  
  def cleanup() {
    estimation.simulation         = Some(this)
    estimation.costToUs           = unitsOurs   .view.map(_.valueReceived).sum
    estimation.costToEnemy        = unitsEnemy  .view.map(_.valueReceived).sum
    estimation.damageToUs         = unitsOurs   .view.map(_.damageReceived).sum
    estimation.damageToEnemy      = unitsEnemy  .view.map(_.damageReceived).sum
    estimation.deathsUs           = unitsOurs   .count(_.dead)
    estimation.deathsEnemy        = unitsEnemy  .count(_.dead)
    estimation.totalUnitsUs       = unitsOurs   .size
    estimation.totalUnitsEnemy    = unitsEnemy  .size
    if (With.configuration.debugging) {
      estimation.debugReport ++= everyone.map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
      estimation.events             = everyone.flatMap(_.events).sortBy(_.frame)
    }
    recordMetrics()
  }
  
  private def getChokeMobility(zoneUs: Zone): Double = {
    val zoneEnemy = battle.enemy.centroid.zone
    if (zoneUs == zoneEnemy) return 1.0
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.max(32.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 10.0))
    val output    = PurpleMath.clamp(PurpleMath.nanToOne(2.5 * edgeWidth / ourWidth), 0.25, 1.0)
    output
  }
}
