package Startup

import Development.Logger
import Processes.{Map, _}
import Plans.GamePlans.PlanWinTheGame

import scala.collection.JavaConverters._
import scala.collection.mutable

object With {
  var game:bwapi.Game = null
  var architect:Architect = null
  var bank:Banker = null
  var logger:Logger = null
  var map:Map = null
  var gameplan:PlanWinTheGame = null
  var prioritizer:Prioritizer = null
  var recruiter:Recruiter = null
  var scout:Scout = null
  
  def ourUnits:mutable.Buffer[bwapi.Unit] = {
    game.self.getUnits.asScala
  }
}
