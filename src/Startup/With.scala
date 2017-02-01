package Startup

import Processes.{Banker, Commander, Prioritizer, Recruiter}
import Types.Plans.Strategy.PlanWinTheGame

object With {
  var game:bwapi.Game = null
  var recruiter:Recruiter = null
  var bank:Banker = null
  var prioritizer:Prioritizer = null
  var gameplan:PlanWinTheGame = null
  var commander:Commander = null
}
