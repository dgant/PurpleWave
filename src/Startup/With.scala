package Startup

import Development.Logger
import Global.Allocation._
import Global.Information.Combat.CombatSimulator
import Global.Information.UnitAbstraction.Units
import Global.Information._
import Plans.GamePlans.WinTheGame

object With {
  var game:bwapi.Game = null
  var architect:Architect = null
  var bank:Banker = null
  var simulator:CombatSimulator = null
  var commander:Commander = null
  var economy:Economy = null
  var history:History = null
  var intelligence:Intelligence = null
  var geography:Geography = null
  var gameplan:WinTheGame = null
  var logger:Logger = null
  var prioritizer:Prioritizer = null
  var recruiter:Recruiter = null
  var scheduler:Scheduler = null
  var units: Units = null
}
