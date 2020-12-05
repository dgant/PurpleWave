package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.{LocalBattleMetrics, PredictionLocal}
import Information.Battles.Types.Team
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

class Simulation(val prediction: PredictionLocal) {
  
  private def buildSimulacra(team: Team) = if (With.blackboard.mcrs()) Vector.empty else team.units.view.filter(legalForSimulation).map(new Simulacrum(this, _)).toVector
  private def legalForSimulation(unit: UnitInfo): Boolean = (
    unit.complete&& ! unit.unitClass.isSpell
    && ! unit.invincible          // No stasised units
    && ! unit.is(if (With.performance.danger) Protoss.Interceptor else Protoss.Carrier) // Interceptors produce more accurate results but are slower
    && ! unit.is(Protoss.Scarab)
    && ! (unit.unitClass.isWorker && unit.gathering && unit.isOurs)
    && ! (unit.unitClass.isBuilding && ! unit.canAttack && ! unit.unitClass.isSpellcaster)
  )

  val focus                 : Pixel               = prediction.battle.focus
  val unitsOurs             : Vector[Simulacrum]  = buildSimulacra(prediction.battle.us)
  val unitsEnemy            : Vector[Simulacrum]  = buildSimulacra(prediction.battle.enemy)
  val everyone              : Vector[Simulacrum]  = unitsOurs ++ unitsEnemy
  var updated               : Boolean             = true
  var fleeing               : Boolean             = ! prediction.weAttack
  lazy val ourWidth         : Double              = prediction.battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else unit.unitClass.dimensionMin + unit.unitClass.dimensionMax).sum
  lazy val chokeMobility    : Map[Zone, Double]   = prediction.battle.us.units.map(_.zone).distinct.map(zone => (zone, getChokeMobility(zone))).toMap
  
  val simulacra: Map[UnitInfo, Simulacrum] = if (With.blackboard.mcrs()) Map.empty else
    (unitsOurs.filter(_.canMove) ++ unitsEnemy)
      .map(simulacrum => (simulacrum.realUnit, simulacrum))
      .toMap

  def complete: Boolean = (
    prediction.frames > With.configuration.simulationFrames
    || ! updated
    || unitsOurs.forall(_.dead)
    || unitsEnemy.forall(_.dead)
    || everyone.forall(u => u.dead || ! u.fighting)
  )
  
  def step() {
    updated = false
    prediction.frames += 1
    snipe()
    everyone.foreach(_.step())
    everyone.foreach(_.updateDeath())

    if (complete) {
      cleanup()
    } else if (prediction.frames - prediction.localBattleMetrics.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationEstimationPeriod) {
      recordMetrics()
    }
  }
  
  def snipe() {
    if (prediction.weAttack
      && prediction.weSnipe
      && ! fleeing
      && unitsEnemy.exists(e => e.dead && ! e.realUnit.unitClass.suicides)) {
      fleeing = true
      unitsOurs.foreach(_.cooldownMoving = 24)
    }
  }

  def recordMetrics() {
    prediction.localBattleMetrics += new LocalBattleMetrics(this, prediction.localBattleMetrics.lastOption)
  }
  
  def cleanup() {
    prediction.costToUs           = unitsOurs   .view.map(_.valueReceived).sum
    prediction.costToEnemy        = unitsEnemy  .view.map(_.valueReceived).sum
    prediction.damageToUs         = unitsOurs   .view.map(_.damageReceived).sum
    prediction.damageToEnemy      = unitsEnemy  .view.map(_.damageReceived).sum
    prediction.deathsUs           = unitsOurs   .count(_.dead)
    prediction.deathsEnemy        = unitsEnemy  .count(_.dead)
    prediction.totalUnitsUs       = unitsOurs   .size
    prediction.totalUnitsEnemy    = unitsEnemy  .size
    if (With.configuration.debugging) {
      prediction.debugReport ++= everyone.map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
      prediction.events = everyone.flatMap(_.events).sortBy(_.frame)
    }
    recordMetrics()
  }
  
  private def getChokeMobility(zoneUs: Zone): Double = {
    val zoneEnemy = prediction.battle.enemy.centroidAir.zone
    if (zoneUs == zoneEnemy) return 1.0
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.max(32.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 10.0))
    val output    = PurpleMath.clamp(PurpleMath.nanToOne(2.5 * edgeWidth / ourWidth), 0.25, 1.0)
    output
  }
}
