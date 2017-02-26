package Startup

import Development.Logger
import Global.Allocation._
import Global.Information.Combat.CombatSimulator
import Global.Information._
import Plans.GamePlans.PlanWinTheGame

import scala.collection.JavaConverters._
import scala.collection.immutable.{HashMap, HashSet}

object With {
  var game:bwapi.Game = null
  var architect:Architect = null
  var bank:Banker = null
  var simulator:CombatSimulator = null
  var commander:Commander = null
  var economy:Economy = null
  var history:History = null
  var geography:Geography = null
  var gameplan:PlanWinTheGame = null
  var logger:Logger = null
  var prioritizer:Prioritizer = null
  var recruiter:Recruiter = null
  var scheduler:Scheduler = null
  var intelligence:Intelligence = null
  var memory:EnemyUnitTracker = null
  
  var _ourUnits:Set[bwapi.Unit] = new HashSet
  var _enemyUnits:Set[bwapi.Unit] = new HashSet
  var _unitsById:scala.collection.Map[Int, bwapi.Unit] = new HashMap
  
  def onFrame() {
    _ourUnits = game.self.getUnits.asScala.toSet
    _enemyUnits = game.enemies.asScala.flatten(_.getUnits.asScala).toSet
    _unitsById = With.game.getAllUnits.asScala
      .filter(_.exists)
      .map(unit => unit.getID -> unit)
      .toMap
  }
  
  def unit(id:Int):Option[bwapi.Unit] = { _unitsById.get(id) }
  def ourUnits:Set[bwapi.Unit] = { _ourUnits }
  def enemyUnits:Set[bwapi.Unit] = { _enemyUnits }
}
