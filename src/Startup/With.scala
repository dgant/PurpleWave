package Startup

import Processes.Allocation.{Banker, Recruiter}
import Processes.Planner

object With {
  var game:bwapi.Game = null
  var recruiter:Recruiter = null
  var bank:Banker = null
  var planner:Planner = null
}
