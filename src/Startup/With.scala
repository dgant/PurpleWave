package Startup

import Processes.Allocation.{Banker, Recruiter}

object With {
  var game:bwapi.Game = null
  var recruiter:Recruiter = null
  var bank:Banker = null
}
