package Startup

import Debugging.{AutoCamera, Configuration, Logger, Performance}
import Information.Geography.Geography
import Information.Grids.Grids
import Information._
import Macro.Allocation._
import Macro.Architect
import Macro.Scheduling.Scheduler
import Micro.Battles.Battles
import Micro.{Executor, Commander, Paths}
import Planning.Plans.GamePlans.WinTheGame
import ProxyBwapi.UnitTracking.UnitTracker
import Performance.Latency
import bwapi.Player

object With {
  var game:bwapi.Game = null
  var architect:Architect = null
  var bank:Banker = null
  var camera:AutoCamera = null
  var configuration:Configuration = null
  var battles:Battles = null
  var executor:Executor = null
  var commander:Commander = null
  var economy:Economy = null
  var grids:Grids = null
  var intelligence:Intelligence = null
  var geography:Geography = null
  var gameplan:WinTheGame = null
  var latency:Latency = null
  var logger:Logger = null
  var paths:Paths = null
  var performance:Performance = null
  var prioritizer:Prioritizer = null
  var recruiter:Recruiter = null
  var scheduler:Scheduler = null
  var units: UnitTracker = null
  
  var self:Player = null
  var frame = 0
  var mapWidth = 0
  var mapHeight = 0
  
  def onFrame() {
    frame = With.game.getFrameCount
  }
}
