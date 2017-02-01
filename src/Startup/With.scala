package Startup

import Processes.{Banker, Commander, Prioritizer, Recruiter}
import Types.Plans.Strategy.PlanWinTheGame

import scala.collection.JavaConverters._
import scala.collection.mutable

object With {
  var game:bwapi.Game = null
  var recruiter:Recruiter = null
  var bank:Banker = null
  var prioritizer:Prioritizer = null
  var gameplan:PlanWinTheGame = null
  var commander:Commander = null
  
  def ourUnits:mutable.Buffer[bwapi.Unit] = {
    game.self.getUnits.asScala
  }
}
