package Startup

import Processes.{Banker, Prioritizer, Recruiter}
import Types.Plans.PlanWinTheGame

object With {
  var game:bwapi.Game = null
  var recruiter:Recruiter = null
  var bank:Banker = null
  var prioritizer:Prioritizer = null
  var gameplan:PlanWinTheGame = null
  
}
